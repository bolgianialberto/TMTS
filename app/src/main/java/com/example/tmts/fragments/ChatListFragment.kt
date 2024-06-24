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
import com.example.tmts.interfaces.OnChatClickListener

class ChatListFragment : Fragment(), OnChatClickListener{

    private lateinit var chatListAdapter: ChatListAdapter
    private lateinit var rvChatList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list_chat, container, false)
        chatListAdapter = ChatListAdapter(requireContext(), ArrayList(), this)
        rvChatList = view.findViewById(R.id.rv_chat_list)
        rvChatList.layoutManager = LinearLayoutManager(requireContext())
        rvChatList.adapter = chatListAdapter
        loadChats()
        return view
    }

    private fun loadChats() {
        FirebaseInteraction.getUserChats(
            onSuccess = {
                users ->
                Log.d("SUCCESS", "${users.size}")
                users.forEach{ chatListAdapter.updateUsers(it) }
            },
            onFailure = {

            }
        )
    }

    override fun onChatClickListener(userId: String) {
        TODO("Not yet implemented")
    }
}