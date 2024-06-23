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
import com.example.tmts.FirebaseInteraction
import com.example.tmts.R
import com.example.tmts.beans.Network
import com.example.tmts.beans.Watchlist
import org.w3c.dom.Text

class AddToWatchlistAdapter (
    private val context: Context,
    private var mediaItems: List<Watchlist>,
    private val onItemClick: (Watchlist) -> Unit
) : RecyclerView.Adapter<AddToWatchlistAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.add_to_watchlist_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaItem = mediaItems[position]
        holder.bind(mediaItem)
    }

    override fun getItemCount(): Int {
        return mediaItems.size
    }

    fun updateMedia(mediaItems: List<Watchlist>){
        this.mediaItems = mediaItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tv_watchlist_name: TextView = itemView.findViewById(R.id.tv_watchlist_name)
        private val rv_watchlist: RecyclerView = itemView.findViewById(R.id.rv_watchlist_contents)
        fun bind(mediaItem: Watchlist) {
            tv_watchlist_name.text = mediaItem.name

            // mettere l'adapter per i film
            val mediaAdapter = MediaAdapter(context, mediaItem.medias, 80, 120){
                onItemClick(mediaItem)
            }
            rv_watchlist.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            rv_watchlist.adapter = mediaAdapter

            itemView.setOnClickListener{
                onItemClick(mediaItem)
            }
        }
    }
}