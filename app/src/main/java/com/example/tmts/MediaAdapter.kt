package com.example.tmts

import android.content.Context
import android.media.browse.MediaBrowser.MediaItem
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MediaAdapter(
    private val context: Context,
    private var mediaItems: List<Media>,
    private val onItemClick: (Media) -> Unit
) : RecyclerView.Adapter<MediaAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.popular_media_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaItem = mediaItems[position]
        holder.bind(mediaItem)
    }

    override fun getItemCount(): Int {
        return mediaItems.size
    }

    fun updateMedia(mediaItems: List<Media>){
        this.mediaItems = mediaItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewPopular: ImageView = itemView.findViewById(R.id.iv_popular)

        fun bind(mediaItem: Media) {
            Glide.with(context)
                .load("https://image.tmdb.org/t/p/w500${mediaItem.posterPath}")
                .placeholder(R.drawable.movie)
                .into(imageViewPopular)

            itemView.setOnClickListener {
                onItemClick(mediaItem)
            }
        }
    }
}