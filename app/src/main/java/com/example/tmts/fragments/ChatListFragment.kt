package com.example.tmts.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmts.FirebaseInteraction
import com.example.tmts.R
import com.example.tmts.adapters.ChatListAdapter

class ChatListFragment : Fragment(){

    private lateinit var chatListAdapter: ChatListAdapter
    private lateinit var rvChatList: RecyclerView

    override fun onResume() {
        super.onResume()
        chatListAdapter.clearUsers()
        // loadChats()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list_chat, container, false)
        chatListAdapter = ChatListAdapter(requireContext())
        rvChatList = view.findViewById(R.id.rv_chat_list)
        rvChatList.layoutManager = LinearLayoutManager(requireContext())
        rvChatList.adapter = chatListAdapter
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
}