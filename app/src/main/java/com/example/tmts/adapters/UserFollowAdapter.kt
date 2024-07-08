package com.example.tmts.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmts.FirebaseInteraction
import com.example.tmts.R
import com.example.tmts.beans.User
import com.example.tmts.interfaces.OnChatClickListener
import com.example.tmts.interfaces.OnUserClickListener

class UserFollowAdapter(
    private val context: Context,
    private val userClickListener: OnUserClickListener,
    private val chatClickListener: OnChatClickListener,
    private val userFollowList: ArrayList<User> = ArrayList()
) : RecyclerView.Adapter<UserFollowAdapter.UserFollowViewHolder>() {
    inner class UserFollowViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private lateinit var ivUserImage: ImageView
        private lateinit var tvUsername: TextView
        private lateinit var tvBio: TextView
        private lateinit var chatBtt: Button
        fun bind(user: User) {
            ivUserImage = itemView.findViewById(R.id.iv_user_follow)
            tvUsername = itemView.findViewById(R.id.tv_user_follow_username)
            tvBio = itemView.findViewById(R.id.tv_user_follow_bio)
            chatBtt = itemView.findViewById(R.id.btt_follow_chat)

            FirebaseInteraction.getUserProfileImageRef(
                user.id,
                onSuccess = {
                    it.downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(context)
                            .load(uri)
                            .into(ivUserImage)
                    }.addOnFailureListener{ exc ->
                        Glide.with(context)
                            .load(R.drawable.account)
                            .into(ivUserImage)
                    }
                }, onFailure = {
                    Glide.with(context)
                        .load(R.drawable.account)
                        .into(ivUserImage)
                }
            )
            ivUserImage.setOnClickListener {
                userClickListener.onUserClickListener(user)
            }

            tvUsername.text = user.name
            if (!user.biography.isNullOrBlank()) {
                tvBio.text = user.biography
            } else {
                tvBio.text = ""
            }
            chatBtt.setOnClickListener{
                chatClickListener.onChatClickListener(user.id, user.name)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserFollowViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_follow_item, parent, false)
        return UserFollowViewHolder(view)    }

    override fun getItemCount(): Int {
        return userFollowList.size
    }

    override fun onBindViewHolder(holder: UserFollowViewHolder, position: Int) {
        holder.bind(userFollowList[position])
    }

    fun updateUsers(user: User) {
        val position = addUserWithAlphabeticOrder(user)
        notifyItemInserted(position)
    }

    private fun addUserWithAlphabeticOrder(user: User): Int {
        val position = userFollowList.binarySearch {
            it.name.compareTo(user.name)
        }
        val insertionPoint = if (position < 0) -(position + 1) else position
        userFollowList.add(insertionPoint,user)
        return insertionPoint
    }

    fun removeUser(user: User) {
        val position = userFollowList.indexOf(user)
        userFollowList.removeAt(position)
        notifyItemRemoved(position)
    }
}