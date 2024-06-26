package com.example.tmts.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmts.ChatResultContract
import com.example.tmts.FirebaseInteraction
import com.example.tmts.R
import com.example.tmts.activities.AddChatActivity
import com.example.tmts.adapters.ChatListAdapter
import com.example.tmts.beans.Message
import com.example.tmts.beans.User
import com.example.tmts.interfaces.OnChatClickListener
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ChatListFragment : Fragment(), OnChatClickListener {

    private val chatList = ArrayList<Pair<User, Message>>()
    private lateinit var chatListAdapter: ChatListAdapter
    private lateinit var addChatBtt: FloatingActionButton
    private lateinit var rvChatList: RecyclerView
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
            val intent = Intent(context, AddChatActivity::class.java)
            requireContext().startActivity(intent)
        }
        loadChats()
        return view
    }



    private fun loadChats() {
        FirebaseInteraction.getUserChats(
            onSuccess = { chats ->
                chats.forEach {
                    FirebaseInteraction.getUserInfo(
                        it.first,
                        onSuccess = { user ->
                            chatList.add(Pair(user, it.second))
                            chatListAdapter.updateUsers(Pair(user, it.second))
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

    private fun updateChats() {
        FirebaseInteraction.getUserChats(
            onSuccess = { chats ->
                chats.forEach { pair ->
                    FirebaseInteraction.getUserInfo(
                        pair.first,
                        onSuccess = { user ->
                            if (chatList.filterNot { it.first == user }.isEmpty()) {
                                chatList.add(Pair(user, pair.second))
                                chatListAdapter.updateUsers(Pair(user, pair.second))
                            } else {
                                val previousUserPair = chatList.find { it.first == user }
                                if (previousUserPair != null && previousUserPair.second != pair.second) {
                                    // Log.d("UPDATED MSG", "${user.name} - ${pair.second}")
                                    val pos = chatList.indexOf(previousUserPair)
                                    chatList[pos] = Pair(user, pair.second)
                                    chatListAdapter.deleteUser(user)
                                    chatListAdapter.updateUsers(Pair(user, pair.second))
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

    override fun onChatClickListener(userId: String) {
        val username = chatList.find { it.first.id == userId }!!.first.name
        chatActivityLauncher.launch(Pair(userId, username))
    }
}