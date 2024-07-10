package com.example.tmts.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmts.R
import com.example.tmts.Utils
import com.example.tmts.beans.results.ShowDetailsResult
import com.example.tmts.interfaces.OnShowDetailsClickListener

class WatchlistShowAdapter(
    private val context: Context,
    private val showDetailsClickListener: OnShowDetailsClickListener,
    private var showDetailList: ArrayList<ShowDetailsResult> = ArrayList(),
    private val imageWidth: Int? = null,
    private val imageHeight: Int? = null
) : RecyclerView.Adapter<WatchlistShowAdapter.WatchlistShowViewHolder>() {
    inner class WatchlistShowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val ivShow: ImageView = itemView.findViewById(R.id.iv_popular)

        fun bind(showDetails: ShowDetailsResult) {
            if (imageWidth != null && imageHeight != null) {
                val layoutParams = ivShow.layoutParams
                layoutParams.width = Utils.dpToPx(context, imageWidth)
                layoutParams.height = Utils.dpToPx(context, imageHeight)
                ivShow.layoutParams = layoutParams
            }
            var posterPath: String? = null
            when (showDetails.showTypeId) {
                "MOV" -> posterPath = showDetails.movieDetails!!.posterPath
                "SER" -> posterPath = showDetails.serieDetails!!.posterPath
            }
            Glide.with(context)
                .load("https://image.tmdb.org/t/p/w500${posterPath}")
                .placeholder(R.drawable.movie)
                .into(ivShow)

            itemView.setOnClickListener {
                showDetailsClickListener.onShowDetailsClickListener(showDetails)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchlistShowViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.popular_media_item, parent, false)
        return WatchlistShowViewHolder(view)
    }

    override fun getItemCount(): Int {
        return showDetailList.size
    }

    override fun onBindViewHolder(holder: WatchlistShowViewHolder, position: Int) {
        holder.bind(showDetailList[position])
    }
}