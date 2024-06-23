package com.example.tmts.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmts.R
import com.example.tmts.activities.SerieDetailsActivity
import com.example.tmts.beans.SerieDetails

class SearchSerieAdapter(private val context: Context, private var series: List<SerieDetails>):
    RecyclerView.Adapter<SearchSerieAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.search_serie_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val movie = series[position]
            holder.bind(movie)
        }

        override fun getItemCount(): Int {
            return series.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val imageViewPoster: ImageView = itemView.findViewById(R.id.iv_search_page_serie_backdrop)
            private val textViewTitle: TextView = itemView.findViewById(R.id.tv_search_page_serie_title)
            private val textViewSeasons: TextView = itemView.findViewById(R.id.tv_search_page_serie_n_seasons)
            private val textViewGenres: TextView = itemView.findViewById(R.id.tv_search_page_serie_genres)

            fun bind(serie: SerieDetails){
                // image
                Glide.with(context)
                    .load("https://image.tmdb.org/t/p/w500${serie.posterPath}")
                    .placeholder(R.drawable.movie)
                    .into(imageViewPoster)

                // title
                textViewTitle.text = serie.title

                // number of seasons
                val seasonText = if (serie.number_of_seasons == 1) "season" else "seasons"
                textViewSeasons.text = "${serie.number_of_seasons} $seasonText"

                // genres
                val genresString = serie.genres.joinToString(" / ") { it.name }
                textViewGenres.text = genresString

                itemView.setOnClickListener {
                    val intent = Intent(context, SerieDetailsActivity::class.java)
                    intent.putExtra("serieId", serie.id)
                    context.startActivity(intent)
                }
            }
        }

        fun updateSeries(newSeries: List<SerieDetails>) {
            series = newSeries
            notifyDataSetChanged()
        }
}