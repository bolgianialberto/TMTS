package com.example.tmts.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmts.R
import com.example.tmts.beans.Provider

class ProviderAdapter (
    private val context: Context,
    private var mediaItems: List<Provider>
) : RecyclerView.Adapter<ProviderAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.provider_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaItem = mediaItems[position]
        holder.bind(mediaItem)
    }

    override fun getItemCount(): Int {
        return mediaItems.size
    }

    fun updateMedia(mediaItems: List<Provider>){
        this.mediaItems = mediaItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewNetwork: ImageView = itemView.findViewById(R.id.iv_network)
        fun bind(mediaItem: Provider) {
            Glide.with(context)
                .load("https://image.tmdb.org/t/p/w500${mediaItem.logo_path}")
                .placeholder(R.drawable.movie)
                .into(imageViewNetwork)
        }
    }
}