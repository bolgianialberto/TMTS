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
import com.example.tmts.FirebaseInteraction
import com.example.tmts.R
import com.example.tmts.activities.ChatActivity
import com.example.tmts.activities.UserPageActivity
import com.example.tmts.adapters.UserFollowAdapter
import com.example.tmts.beans.User
import com.example.tmts.interfaces.OnChatClickListener
import com.example.tmts.interfaces.OnUserClickListener

class UserFollowersFragment : Fragment(), OnUserClickListener, OnChatClickListener {

    private lateinit var userFollowAdapter: UserFollowAdapter
    private lateinit var rvFollowers: RecyclerView

    private val followerList = ArrayList<User>()
    private val actuallyShownFollowers = ArrayList<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_followers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userFollowAdapter = UserFollowAdapter(requireContext(), this, this)
        rvFollowers = view.findViewById(R.id.rv_user_followers)
        rvFollowers.layoutManager = LinearLayoutManager(context)
        rvFollowers.adapter = userFollowAdapter
        loadFollowers()
    }

    fun performFilter(filterString: String) {
        val startingChars = filterString.lowercase()
        actuallyShownFollowers
            .filterNot { it.name.lowercase().startsWith(startingChars) }
            .map { userFollowAdapter.removeUser(it) }
        actuallyShownFollowers.removeAll { !it.name.lowercase().startsWith(startingChars) }
        followerList
            .filterNot { actuallyShownFollowers.contains(it) }
            .filter { it.name.lowercase().startsWith(startingChars) }
            .map { userFollowAdapter.updateUsers(it) }
        followerList
            .filterNot { actuallyShownFollowers.contains(it) }
            .filter { it.name.lowercase().startsWith(startingChars) }
            .map { actuallyShownFollowers.add(it) }

    }

    private fun loadFollowers() {
        FirebaseInteraction.getFollowersUsers(
            onSuccess = {userIdList ->
                userIdList.forEach { userId ->
                    FirebaseInteraction.getUserInfo(
                        userId,
                        onSuccess = { user ->
                            followerList.add(user)
                            actuallyShownFollowers.add(user)
                            userFollowAdapter.updateUsers(user)
                        },
                        onFailure = {
                            Log.e("User error", "Error retrieving user $userId")
                        }
                    )
                }

            },
            onFailure = {
                Log.e("FollowersError", "${it.message}")
            }
        )
    }

    override fun onUserClickListener(user: User) {
        val intent = Intent(requireContext(), UserPageActivity::class.java)
        intent.putExtra("uid", user.id)
        startActivity(intent)
    }

    override fun onChatClickListener(userId: String, username: String) {
        val intent = Intent(requireContext(), ChatActivity::class.java)
        intent.putExtra("userId", userId)
        intent.putExtra("username", username)
        startActivity(intent)
    }
}