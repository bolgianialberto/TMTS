package com.example.tmts.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmts.R
import com.example.tmts.beans.CastMember

class CastAdapter (
    private val context: Context,
    private var mediaItems: List<CastMember>
) : RecyclerView.Adapter<CastAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.cast_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaItem = mediaItems[position]
        holder.bind(mediaItem)
    }

    override fun getItemCount(): Int {
        return mediaItems.size
    }

    fun updateMedia(mediaItems: List<CastMember>){
        this.mediaItems = mediaItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivActor: ImageView = itemView.findViewById(R.id.iv_actor_image)
        private val tvActorName: TextView = itemView.findViewById(R.id.tv_actor_name)
        private val tvActorCharacter: TextView = itemView.findViewById(R.id.tv_character_name)
        fun bind(castMember: CastMember) {
            Glide.with(context)
                .load("https://image.tmdb.org/t/p/w500${castMember.profile_path}")
                .placeholder(R.drawable.movie)
                .into(ivActor)

            tvActorCharacter.text = castMember.character

            tvActorName.text = castMember.actor_name
        }
    }
}