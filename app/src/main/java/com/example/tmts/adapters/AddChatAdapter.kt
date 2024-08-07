package com.example.tmts.adapters

import android.app.Activity
import android.content.Context
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
import com.example.tmts.beans.User
import com.example.tmts.interfaces.OnChatClickListener

class AddChatAdapter(
    private val context: Context,
    private val onChatClickListener: OnChatClickListener,
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
                        val activity = context as? Activity
                        if (activity != null && !activity.isDestroyed && !activity.isFinishing) {
                            Glide.with(context)
                                .load(uri)
                                .into(ivUserImage)
                        }
                    }.addOnFailureListener {
                        val activity = context as? Activity
                        if (activity != null && !activity.isDestroyed && !activity.isFinishing) {
                            Glide.with(context)
                                .load(R.drawable.account)
                                .into(ivUserImage)
                        }
                    }
                }, onFailure = {
                    val activity = context as? Activity
                    if (activity != null && !activity.isDestroyed && !activity.isFinishing) {
                        Glide.with(context)
                            .load(R.drawable.account)
                            .into(ivUserImage)
                    }
                }
            )

            tvUsername.text = user.name
            if (!user.biography.isNullOrBlank()) {
                tvBio.text = user.biography
            } else {
                tvBio.text = ""
            }
            tvLastMessageTime.visibility = View.GONE
            llUser.setOnClickListener {
                onChatClickListener.onChatClickListener(user.id, user.name)
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