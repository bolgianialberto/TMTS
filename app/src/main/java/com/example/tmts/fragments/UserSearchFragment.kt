package com.example.tmts.fragments

import android.content.Intent
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
import com.example.tmts.activities.ChatActivity
import com.example.tmts.activities.UserPageActivity
import com.example.tmts.adapters.SearchUserAdapter
import com.example.tmts.beans.User
import com.example.tmts.beans.viewmodels.UsersViewModel
import com.example.tmts.interfaces.OnChatClickListener
import com.example.tmts.interfaces.OnUserClickListener

class UserSearchFragment : Fragment(), OnUserClickListener, OnChatClickListener {

    private val sharedViewModel: UsersViewModel by activityViewModels()

    private val allUsers = ArrayList<User>()
    private val actuallyShownUsers = ArrayList<User>()

    private lateinit var rvSearchUser: RecyclerView
    private lateinit var searchUserAdapter: SearchUserAdapter
    // private var loggedUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_search, container, false)
        searchUserAdapter = SearchUserAdapter(requireContext(), java.util.ArrayList(),this, this)
        rvSearchUser = view.findViewById(R.id.rv_search_user)
        rvSearchUser.layoutManager = LinearLayoutManager(requireContext())
        rvSearchUser.adapter = searchUserAdapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel.text.observe(viewLifecycleOwner, Observer { searchUserEdtText ->
            val startingChars = searchUserEdtText.toString().lowercase()
            actuallyShownUsers
                .filterNot { it.name.lowercase().startsWith(startingChars) }
                .map { searchUserAdapter.removeUser(it) }
            actuallyShownUsers.removeAll { !it.name.lowercase().startsWith(startingChars) }
            allUsers
                .filterNot { actuallyShownUsers.contains(it) }
                .filter { it.name.lowercase().startsWith(startingChars) }
                .map { searchUserAdapter.updateUsers(it) }
            allUsers
                .filterNot { actuallyShownUsers.contains(it) }
                .filter { it.name.lowercase().startsWith(startingChars) }
                .map { actuallyShownUsers.add(it) }
        })

        val writtenTxt = sharedViewModel.text.value
        if (writtenTxt != null) loadUsersStartingWith(writtenTxt) else loadUsers()
    }

    private fun loadUsers() {
        FirebaseInteraction.getUsers(
            onSuccess = { users ->
                val usersWithoutLoggedUser = users.filterNot { it.id == FirebaseInteraction.user.uid }
                allUsers.addAll(usersWithoutLoggedUser)
                actuallyShownUsers.addAll(usersWithoutLoggedUser)
                usersWithoutLoggedUser.forEach{ searchUserAdapter.updateUsers(it) }
            },
            onFailure = {
                Log.e("AddChatErr", "${it.message}")
            }
        )
    }

    private fun loadUsersStartingWith(startingChars: String) {
        FirebaseInteraction.getUsersStartingWith(
            startingChars,
            onSuccess = { users ->
                val usersWithoutLoggedUser = users.filterNot { it.id == FirebaseInteraction.user.uid }
                allUsers.addAll(usersWithoutLoggedUser)
                actuallyShownUsers.addAll(usersWithoutLoggedUser)
                usersWithoutLoggedUser.forEach{ searchUserAdapter.updateUsers(it) }
            },
            onFailure = {
                Log.e("AddChatErr", it)
            }
        )
    }

    override fun onChatClickListener(userId: String, username: String) {
        val intent = Intent(context, ChatActivity::class.java)
        intent.putExtra("userId", userId)
        intent.putExtra("username", username)
        startActivity(intent)
    }

    override fun onUserClickListener(user: User) {
        val intent = Intent(context, UserPageActivity::class.java)
        intent.putExtra("uid", user.id)
        startActivity(intent)
    }
}