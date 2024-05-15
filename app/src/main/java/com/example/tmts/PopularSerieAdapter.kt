package com.example.tmts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PopularSerieAdapter(private val context: Context, private var series: List<Media>) :
    RecyclerView.Adapter<PopularSerieAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.popular_media_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = series[position]
        holder.bind(movie)
    }

    override fun getItemCount(): Int {
        return series.size
    }

    fun updateSeries(movies: List<Media>) {
        this.series = movies
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewPopular: ImageView = itemView.findViewById(R.id.iv_popular)

        fun bind(serie: Media) {
            Glide.with(context)
                .load("https://image.tmdb.org/t/p/w500${serie.posterPath}")
                .placeholder(R.drawable.movie)
                .into(imageViewPopular)
        }
    }
}