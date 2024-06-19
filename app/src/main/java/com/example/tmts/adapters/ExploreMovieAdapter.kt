package com.example.tmts.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tmts.R
import com.example.tmts.beans.MovieDetails

class ExploreMovieAdapter (
    private val context: Context,
    private var mediaItems: List<MovieDetails>
) : RecyclerView.Adapter<ExploreMovieAdapter.ExploreMovieViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreMovieViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.explore_movie_item, parent, false)
        return ExploreMovieViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mediaItems.size
    }

    override fun onBindViewHolder(holder: ExploreMovieAdapter.ExploreMovieViewHolder, position: Int) {
        val mediaItem = mediaItems[position]
        holder.bind(mediaItem)
    }

    fun updateMovies(mediaItems: List<MovieDetails>) {
        this.mediaItems = mediaItems
        notifyDataSetChanged()
    }

    inner class ExploreMovieViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private val textViewTitle: TextView = itemView.findViewById(R.id.tv_explore_movie_title)

        fun bind(movie: MovieDetails) {
            textViewTitle.text = movie.title
        }
    }

}