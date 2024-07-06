package com.example.tmts.adapters

import android.content.Context
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
import com.example.tmts.beans.Message
import com.example.tmts.beans.User
import com.example.tmts.interfaces.OnChatClickListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatListAdapter(
    private val context: Context,
    private val chatClickListener: OnChatClickListener,
    private val usersWithLastMessageList: ArrayList<Pair<User, Message>> = ArrayList()
) : RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>() {
    inner class ChatListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private lateinit var llUser: LinearLayout
        private lateinit var ivUserImage: ImageView
        private lateinit var tvUsername: TextView
        private lateinit var tvLastMessage: TextView
        private lateinit var tvLastMessageTime: TextView
        private lateinit var ivReadMessage: ImageView

        fun bind(userWithLastMessage: Pair<User, Message>) {
            val user = userWithLastMessage.first
            val lastMessage = userWithLastMessage.second

            llUser = itemView.findViewById(R.id.ll_user_chat_item)
            ivUserImage = itemView.findViewById(R.id.iv_chat_account_image)
            tvUsername = itemView.findViewById(R.id.tv_chat_account_username)
            tvLastMessage = itemView.findViewById(R.id.tv_chat_account_last_message)
            tvLastMessageTime = itemView.findViewById(R.id.tv_chat_account_last_message_time)
            ivReadMessage = itemView.findViewById(R.id.iv_chat_read_msg)

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
            tvUsername.text = user.name
            tvLastMessage.text = lastMessage.text
            tvLastMessageTime.text = convertTimestampToDateString(lastMessage.timestamp)
            llUser.setOnClickListener {
                chatClickListener.onChatClickListener(user.id, user.name)
                /*val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("userId", user.id)
                intent.putExtra("username", user.name)
                context.startActivity(intent)*/
            }
            if (!lastMessage.read && lastMessage.receiverId == FirebaseInteraction.user!!.uid) {
                ivReadMessage.visibility = View.VISIBLE
            } else {
                ivReadMessage.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_chat_item, parent, false)
        return ChatListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return usersWithLastMessageList.size
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        holder.bind(usersWithLastMessageList[position])
    }

    fun updateUsers(userWithLastMessage: Pair<User, Message>) {
        val position = addMessageWithTimestampOrder(userWithLastMessage)
        notifyItemInserted(position)
    }

    fun updateUser(userWithLastMessage: Pair<User, Message>) {
        val previousPair = usersWithLastMessageList.find { it.first == userWithLastMessage.first }
        var previousIndex = -1
        if (previousPair != null) {
            previousIndex = usersWithLastMessageList.indexOf(previousPair)
        }
        usersWithLastMessageList.removeAt(previousIndex)
        val newIndex = addMessageWithTimestampOrder(userWithLastMessage)
        notifyItemMoved(previousIndex, newIndex)
        notifyItemChanged(newIndex)
    }

    fun removeUser(user: User){
        val position = usersWithLastMessageList.map { it.first }.indexOf(user)
        usersWithLastMessageList.removeAt(position)
        notifyItemRemoved(position)
    }


    private fun addMessageWithTimestampOrder(userWithLastMessage: Pair<User, Message>): Int {
        val position = usersWithLastMessageList.binarySearch {
            userWithLastMessage.second.timestamp.compareTo(it.second.timestamp)
        }
        val insertionPoint = if (position < 0) -(position + 1) else position
        usersWithLastMessageList.add(insertionPoint, userWithLastMessage)
        return insertionPoint
    }

    private fun convertTimestampToDateString(timestamp: Long): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = Date(timestamp)
        return formatter.format(date).toString()
    }
}