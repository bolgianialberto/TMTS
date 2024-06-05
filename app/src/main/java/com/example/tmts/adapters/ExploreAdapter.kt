package com.example.tmts.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmts.R
import com.example.tmts.beans.Media

class ExploreAdapter(
    private val context: Context,
    private var mediaItems: List<Media>,
    private val onItemClick: (Media) -> Unit
): RecyclerView.Adapter<ExploreAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewExplore: ImageView = itemView.findViewById(R.id.iv_popular)

        fun bind(mediaItem: Media) {
            Glide.with(context)
                .load("https://image.tmdb.org/t/p/w500${mediaItem.posterPath}")
                .placeholder(R.drawable.movie)
                .into(imageViewExplore)

            itemView.setOnClickListener {
                onItemClick(mediaItem)
            }
        }
    }
}