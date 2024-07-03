package com.example.tmts.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tmts.FirebaseInteraction
import com.example.tmts.R
import com.example.tmts.beans.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(
    private val context: Context,
    private val messageList: ArrayList<Message>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val SENT_MSG = 1
    private val RECEIVED_MSG = 2

    inner class SentMessageViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSentMessage: TextView = itemView.findViewById(R.id.tv_sent_chat_message)
        val tvTimeSentMessage: TextView = itemView.findViewById(R.id.tv_time_sent_chat_message)
    }

    inner class ReceivedMessageViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvReceivedMessage: TextView = itemView.findViewById(R.id.tv_received_chat_message)
        val tvTimeReceivedMessage: TextView = itemView.findViewById(R.id.tv_time_received_chat_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var result: RecyclerView.ViewHolder? = null
        when(viewType) {
            SENT_MSG -> result = SentMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.message_chat_item_sent, parent, false))
            RECEIVED_MSG -> {
                result = ReceivedMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.message_chat_item_received, parent, false))
            }
        }
        return result!!
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        if (FirebaseInteraction.user.uid == currentMessage.senderId) {
            return SENT_MSG
        }
        return RECEIVED_MSG
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
        if (holder.javaClass == SentMessageViewHolder::class.java) {
            val viewHolder = holder as SentMessageViewHolder
            viewHolder.tvSentMessage.text = message.text
            viewHolder.tvTimeSentMessage.text = convertTimestampToDateString(message.timestamp)
        } else if (holder.javaClass == ReceivedMessageViewHolder::class.java) {
            val viewHolder = holder as ReceivedMessageViewHolder
            viewHolder.tvReceivedMessage.text = message.text
            viewHolder.tvTimeReceivedMessage.text = convertTimestampToDateString(message.timestamp)
            if (!message.read){
                Log.d("Binding", "bind: $message")
                FirebaseInteraction.notifyMessageRead(
                    message,
                    onSuccess = {
                        Log.d("ReadNotify", "Complete for $message")
                    },
                    onFailure = {
                        Log.e("ReadNotify Error", it.message!!)
                    }
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    fun updateMessages(message: Message) {
        val position = addMessageWithTimestampOrder(message)
        notifyItemInserted(position)
    }

    fun clearMessages() {
        messageList.clear()
        notifyItemRangeRemoved(0, messageList.size - 1)
    }

    private fun addMessageWithTimestampOrder(message: Message): Int {
        val position = messageList.binarySearch { it.timestamp.compareTo(message.timestamp) }
        val insertionPoint = if (position < 0) -(position + 1) else position
        messageList.add(insertionPoint, message)
        return insertionPoint
    }

    private fun convertTimestampToDateString(timestamp: Long): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = Date(timestamp)
        return formatter.format(date).toString()
    }

}