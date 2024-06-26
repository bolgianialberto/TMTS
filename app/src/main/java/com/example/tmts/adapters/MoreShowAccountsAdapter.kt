package com.example.tmts.adapters

import android.content.Context
import android.util.Log
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

class MoreShowAccountsAdapter(
    private val context: Context,
    private val usersList: ArrayList<User> = ArrayList(),
    private val chatToUserClickListener: OnChatClickListener
) : RecyclerView.Adapter<MoreShowAccountsAdapter.MoreShowAccountsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoreShowAccountsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.show_follower_item, parent, false)
        return MoreShowAccountsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    override fun onBindViewHolder(holder: MoreShowAccountsViewHolder, position: Int) {
        val user = usersList[position]
        holder.bind(user)
    }

    fun updateUsers(follower: User) {
        Log.d("UPDATED FOLLOWER", "$follower")
        usersList.add(follower)
        notifyItemInserted(usersList.size - 1)
    }
    inner class MoreShowAccountsViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {

        private lateinit var tvUsername: TextView
        private lateinit var ivUserImage: ImageView
        private lateinit var bttChat: Button
        fun bind(user: User) {
            Log.d("Binding", "$user")
            tvUsername = itemView.findViewById(R.id.tv_show_more_followers_item_username)
            ivUserImage = itemView.findViewById(R.id.iv_show_more_followers_image)
            bttChat = itemView.findViewById(R.id.btt_chat_with_user)

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
            bttChat.setOnClickListener { chatToUserClickListener.onChatClickListener(user.id, user.name) }
        }
    }
}