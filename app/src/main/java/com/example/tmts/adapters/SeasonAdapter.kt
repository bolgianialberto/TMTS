package com.example.tmts.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmts.R
import com.example.tmts.Utils
import com.example.tmts.beans.SeasonDetails

class SeasonAdapter (
    private val context: Context,
    private var mediaItems: List<SeasonDetails>,
    private val onSwipeRight: () -> Unit
) : RecyclerView.Adapter<SeasonAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.season_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaItem = mediaItems[position]
        holder.bind(mediaItem)
    }

    override fun getItemCount(): Int {
        return mediaItems.size
    }

    fun updateMedia(mediaItems: List<SeasonDetails>){
        this.mediaItems = mediaItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSeasonTitle: TextView = itemView.findViewById(R.id.tv_season_title)
        private val rvEpisode: RecyclerView = itemView.findViewById(R.id.rv_episodes)

        init {
            itemView.setOnClickListener {
                if (rvEpisode.visibility == View.GONE) {
                    rvEpisode.visibility = View.VISIBLE
                } else {
                    rvEpisode.visibility = View.GONE
                }
            }

            itemView.setOnTouchListener(Utils.detectSwipe(context){ direction ->
                when (direction) {
                    "MOVE_RIGHT" -> {
                        onSwipeRight()
                    }
                }
            })
        }
        fun bind(mediaItem: SeasonDetails) {
            tvSeasonTitle.text = mediaItem.title

            rvEpisode.layoutManager = LinearLayoutManager(itemView.context)
            rvEpisode.adapter = EpisodeAdapter(context, mediaItem.serieId, mediaItem.serieName, mediaItem.episodes){
                onSwipeRight()
            }
        }
    }
}