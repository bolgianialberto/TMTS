package com.example.tmts.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmts.FirebaseInteraction
import com.example.tmts.R
import com.example.tmts.adapters.ChatListAdapter
import com.example.tmts.beans.Message
import com.example.tmts.beans.User
import com.example.tmts.beans.contracts.AddChatResultContract
import com.example.tmts.beans.contracts.ChatResultContract
import com.example.tmts.beans.viewmodels.UsersViewModel
import com.example.tmts.interfaces.OnChatClickListener
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ChatListFragment : Fragment(), OnChatClickListener {

    private val sharedViewModel: UsersViewModel by activityViewModels()
    private val chatList = ArrayList<Pair<User, Message>>()
    private val actuallyShownChats = ArrayList<Pair<User, Message>>()

    private lateinit var chatListAdapter: ChatListAdapter
    private lateinit var addChatBtt: FloatingActionButton
    private lateinit var rvChatList: RecyclerView

    private val addChatActivityLauncher = registerForActivityResult(AddChatResultContract()) { _ ->
        updateChats()
    }
    private val chatActivityLauncher = registerForActivityResult(ChatResultContract()) { _ ->
        updateChats()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list_chat, container, false)
        chatListAdapter = ChatListAdapter(requireContext(), this)
        rvChatList = view.findViewById(R.id.rv_chat_list)
        rvChatList.layoutManager = LinearLayoutManager(requireContext())
        rvChatList.adapter = chatListAdapter
        addChatBtt = view.findViewById(R.id.flt_btt_add_chat)
        addChatBtt.setOnClickListener {
            addChatActivityLauncher.launch(null)
        }
        /*
        ChatMessagingService().getActualToken(
            onSuccess = {
                Log.d("TOKEN", it)
                println(it)
            },
            onFailure = {
                Log.e("ERROR", it!!.toString())
            }
        )
        */
        loadChats()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel.text.observe(viewLifecycleOwner, Observer { searchUserEdtText ->
            val startingChars = searchUserEdtText.toString().lowercase()
            actuallyShownChats
                .filterNot { it.first.name.lowercase().startsWith(startingChars) }
                .map { chatListAdapter.removeUser(it.first) }
            actuallyShownChats.removeAll { !it.first.name.lowercase().startsWith(startingChars) }
            chatList
                .filterNot { actuallyShownChats.contains(it) }
                .filter { it.first.name.lowercase().startsWith(startingChars) }
                .map { chatListAdapter.updateUsers(it) }
            chatList
                .filterNot { actuallyShownChats.contains(it) }
                .filter { it.first.name.lowercase().startsWith(startingChars) }
                .map { actuallyShownChats.add(it) }
        })
    }

    private fun loadChats() {
        FirebaseInteraction.getUserChats(
            onSuccess = { chats ->
                chats.forEach { pair ->
                    FirebaseInteraction.getUserInfo(
                        pair.first,
                        onSuccess = { user ->
                            chatList.add(Pair(user, pair.second))
                            actuallyShownChats.add(Pair(user, pair.second))
                            chatListAdapter.updateUsers(Pair(user, pair.second))
                        },
                        onFailure = {exc ->
                            Log.e("CHAT LIST ERROR", exc)
                        }
                    )
                }
            },
            onFailure = {
                Log.e("CHAT LIST ERROR", it)
            }
        )
    }

    private fun updateChats() {
        FirebaseInteraction.getUserChats(
            onSuccess = { chats ->
                chats.forEach { pair ->
                    FirebaseInteraction.getUserInfo(
                        pair.first,
                        onSuccess = { user ->
                            if (chatList.find {it.first == user} == null){
                                chatList.add(Pair(user, pair.second))
                                actuallyShownChats.add(Pair(user, pair.second))
                                chatListAdapter.updateUsers(Pair(user, pair.second))
                            } else {
                                val previousUserPair = chatList.find { it.first == user }
                                Log.d("UPDATED MSG 1", "${user.name} - ${pair.second}")
                                if (previousUserPair != null && previousUserPair.second != pair.second) {
                                    Log.d("UPDATED MSG 2", "${user.name} - ${pair.second}")
                                    val chatListPos = chatList.indexOf(previousUserPair)
                                    chatList[chatListPos] = Pair(user, pair.second)
                                    val actuallyShownChatsPos = actuallyShownChats.indexOf(previousUserPair)
                                    actuallyShownChats[actuallyShownChatsPos] = Pair(user, pair.second)
                                    // chatListAdapter.deleteUser(user)
                                    chatListAdapter.updateUser(Pair(user, pair.second))
                                }
                            }
                        },
                        onFailure = {
                            Log.e("Explore Movie Fragment", "Something went wrong")
                        }
                    )
                }
            },
            onFailure = {
                Log.e("CHAT LIST ERROR", it)
            }
        )
    }

    override fun onChatClickListener(userId: String, username: String) {
        chatActivityLauncher.launch(Pair(userId, username))
    }
}