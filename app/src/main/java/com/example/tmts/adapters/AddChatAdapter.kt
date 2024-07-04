package com.example.tmts.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmts.FirebaseInteraction
import com.example.tmts.R
import com.example.tmts.activities.ChatActivity
import com.example.tmts.beans.User

class AddChatAdapter(
    private val context: Context,
    private val userList: MutableList<User> = mutableListOf()
) : RecyclerView.Adapter<AddChatAdapter.AddChatViewHolder>() {
    inner class AddChatViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private lateinit var llUser: LinearLayout
        private lateinit var ivUserImage: ImageView
        private lateinit var tvUsername: TextView
        private lateinit var tvBio: TextView
        private lateinit var tvLastMessageTime: TextView
        fun bind(user: User) {
            llUser = itemView.findViewById(R.id.ll_user_chat_item)
            ivUserImage = itemView.findViewById(R.id.iv_chat_account_image)
            tvUsername = itemView.findViewById(R.id.tv_chat_account_username)
            tvBio = itemView.findViewById(R.id.tv_chat_account_last_message)
            tvLastMessageTime = itemView.findViewById(R.id.tv_chat_account_last_message_time)

            FirebaseInteraction.getUserProfileImageRef(
                user.id,
                onSuccess = {
                    it.downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(context)
                            .load(uri)
                            .into(ivUserImage)
                    }.addOnFailureListener{
                        Log.e("StorageImg Err", "Image not found")
                    }
                }, onFailure = {
                    Log.e("IMAGE ERROR", it)
                })
            tvUsername.text = user.name
            tvBio.text = "Bio"
            tvLastMessageTime.visibility = View.GONE
            llUser.setOnClickListener {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("userId", user.id)
                intent.putExtra("username", user.name)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddChatViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_chat_item, parent, false)
        return AddChatViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: AddChatViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    fun updateUsers(user: User) {
        val position = userList.size
        userList.add(position, user)
        notifyItemInserted(position)
    }

    fun removeUser(user: User){
        val position = userList.indexOf(user)
        userList.removeAt(position)
        notifyItemRemoved(position)
    }
}