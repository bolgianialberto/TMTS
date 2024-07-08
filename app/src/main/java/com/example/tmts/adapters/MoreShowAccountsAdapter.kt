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

class MoreShowAccountsAdapter(
    private val context: Context,
    private val usersList: ArrayList<User> = ArrayList(),
    private val userClickListener: OnUserClickListener,
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
        usersList.add(follower)
        notifyItemInserted(usersList.size - 1)
    }
    inner class MoreShowAccountsViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {

        private lateinit var ivUserImage: ImageView
        private lateinit var llUserInfo: LinearLayout
        private lateinit var tvUsername: TextView
        private lateinit var tvBio: TextView
        private lateinit var bttChat: Button

        fun bind(user: User) {
            ivUserImage = itemView.findViewById(R.id.iv_show_more_followers_image)
            llUserInfo = itemView.findViewById(R.id.ll_show_follower_user_info)
            tvUsername = itemView.findViewById(R.id.tv_show_more_followers_item_username)
            tvBio = itemView.findViewById(R.id.tv_show_more_followers_item_info)
            bttChat = itemView.findViewById(R.id.btt_chat_with_user)

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
                })
            ivUserImage.setOnClickListener {
                userClickListener.onUserClickListener(user)
            }
            llUserInfo.setOnClickListener {
                userClickListener.onUserClickListener(user)
            }
            tvUsername.text = user.name
            if (!user.biography.isNullOrBlank()) {
                tvBio.text = user.biography
            } else {
                tvBio.text = ""
            }
            bttChat.setOnClickListener { chatToUserClickListener.onChatClickListener(user.id, user.name) }
        }
    }
}