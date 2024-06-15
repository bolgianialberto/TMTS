package com.example.tmts.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmts.R
import com.example.tmts.beans.EpisodeDetails
import com.example.tmts.beans.Network
import com.example.tmts.beans.SeasonDetails

class EpisodeAdapter (
    private val context: Context,
    private var mediaItems: List<EpisodeDetails>
) : RecyclerView.Adapter<EpisodeAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.episode_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaItem = mediaItems[position]
        holder.bind(mediaItem)
    }

    override fun getItemCount(): Int {
        return mediaItems.size
    }

    fun updateMedia(mediaItems: List<EpisodeDetails>){
        this.mediaItems = mediaItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPoster: ImageView = itemView.findViewById(R.id.iv_single_episode_poster)
        private val tvEpisodeNumber: TextView = itemView.findViewById(R.id.tv_episode_number)
        private val tvEpisodeTitle: TextView = itemView.findViewById(R.id.tv_episode_title)
        fun bind(mediaItem: EpisodeDetails) {
            Glide.with(context)
                .load("https://image.tmdb.org/t/p/w500${mediaItem.posterPath}")
                .placeholder(R.drawable.movie)
                .into(ivPoster)

            tvEpisodeNumber.text = "S${mediaItem.season_number} | E${mediaItem.episode_number}"

            tvEpisodeTitle.text = mediaItem.title
        }
    }
}