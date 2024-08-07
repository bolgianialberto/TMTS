package com.example.tmts.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmts.FirebaseInteraction
import com.example.tmts.R
import com.example.tmts.beans.User
import com.example.tmts.interfaces.OnChatClickListener
import com.example.tmts.interfaces.OnUserClickListener

class SearchUserAdapter(
    private val context: Context,
    private val userList: ArrayList<User> = ArrayList(),
    private val userClickListener: OnUserClickListener,
    private val chatClickListener: OnChatClickListener
) : RecyclerView.Adapter<SearchUserAdapter.SearchUserViewHolder>() {
    inner class SearchUserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private lateinit var llUserItem: LinearLayout
        private lateinit var ivUserImage: ImageView
        private lateinit var tvUsername: TextView
        private lateinit var tvUserInfo: TextView
        private lateinit var bttChat: Button

        fun bind(user: User) {
            llUserItem = itemView.findViewById(R.id.ll_user_search_item)
            ivUserImage = itemView.findViewById(R.id.iv_search_user_image)
            tvUsername = itemView.findViewById(R.id.tv_search_user_username)
            tvUserInfo = itemView.findViewById(R.id.tv_search_user_info)
            bttChat = itemView.findViewById(R.id.btt_search_user_chat)

            llUserItem.setOnClickListener {
                userClickListener.onUserClickListener(user)
            }
            FirebaseInteraction.getUserProfileImageRef(
                user.id,
                onSuccess = {
                    it.downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(context)
                            .load(uri)
                            .into(ivUserImage)
                    }.addOnFailureListener{
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
            tvUsername.text = user.name
            if (!user.biography.isNullOrBlank()) {
                tvUserInfo.text = user.biography
            } else {
                tvUserInfo.text = ""
            }
            bttChat.setOnClickListener {
                chatClickListener.onChatClickListener(user.id, user.name)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchUserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_search_item, parent, false)
        return SearchUserViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: SearchUserViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    fun updateUsers(user: User) {
        val position = addUserWithAlphabeticOrder(user)
        notifyItemInserted(position)
    }

    private fun addUserWithAlphabeticOrder(user: User): Int {
        val position = userList.binarySearch {
            it.name.compareTo(user.name)
        }
        val insertionPoint = if (position < 0) -(position + 1) else position
        userList.add(insertionPoint,user)
        return insertionPoint
    }

    fun removeUser(user: User){
        val position = userList.indexOf(user)
        userList.removeAt(position)
        notifyItemRemoved(position)
    }
}