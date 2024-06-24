package com.example.tmts.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmts.FirebaseInteraction
import com.example.tmts.R
import com.example.tmts.beans.User
import com.example.tmts.interfaces.OnChatClickListener

class ChatListAdapter(
    private val context: Context,
    private val usersList: ArrayList<User> = ArrayList(),
    private val onUserChatClickListener: OnChatClickListener
) : RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>() {
    inner class ChatListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private lateinit var tvUsername: TextView
        private lateinit var ivUserImage: ImageView
        fun bind(user: User) {
            ivUserImage = itemView.findViewById(R.id.iv_chat_account_image)
            FirebaseInteraction.getUserProfileImageRef(
                user.id,
                onSuccess = {
                    it.downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(context)
                            .load(uri)
                            .into(ivUserImage)
                    }.addOnFailureListener{ exc ->
                        Log.e("STORAGE DOWNLOAD", "Error: $exc")
                    }
                }, onFailure = {
                    Log.e("IMAGE ERROR", it)
                })
            tvUsername = itemView.findViewById(R.id.tv_chat_account_username)
            tvUsername.text = user.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_chat_item, parent, false)
        return ChatListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        holder.bind(usersList[position])
    }

    fun updateUsers(user: User) {
        usersList.add(user)
        notifyItemInserted(usersList.size - 1)
    }
}