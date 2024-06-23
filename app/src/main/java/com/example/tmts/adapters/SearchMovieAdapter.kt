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
import com.example.tmts.activities.MovieDetailsActivity
import com.example.tmts.beans.MovieDetails

class SearchMovieAdapter(private val context: Context, private var movies: List<MovieDetails>) :
    RecyclerView.Adapter<SearchMovieAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.search_movie_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = movies[position]
        holder.bind(movie)
    }

    override fun getItemCount(): Int {
        return movies.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewPoster: ImageView = itemView.findViewById(R.id.iv_search_page_movie_backdrop)
        private val textViewTitle: TextView = itemView.findViewById(R.id.tv_search_page_movie_title)
        private val textViewTime: TextView = itemView.findViewById(R.id.tv_search_page_movie_time)
        private val textViewGenres: TextView = itemView.findViewById(R.id.tv_search_page_movie_genres)

        fun bind(movie: MovieDetails) {
            // image
            Glide.with(context)
                .load("https://image.tmdb.org/t/p/w500${movie.posterPath}")
                .placeholder(R.drawable.movie)
                .into(imageViewPoster)

            // title
            textViewTitle.text = movie.title

            // runtime
            val runtimeMinutes = movie.runtime
            val hours = runtimeMinutes / 60
            val minutes = runtimeMinutes % 60
            val formattedRuntime = "${hours}h ${minutes}m"
            textViewTime.text = formattedRuntime

            // genres
            val genresString = movie.genres.joinToString(" / ") { it.name }
            textViewGenres.text = genresString

            itemView.setOnClickListener {
                val intent = Intent(context, MovieDetailsActivity::class.java)
                intent.putExtra("movieId", movie.id)
                context.startActivity(intent)
            }
        }
    }

    fun updateMovies(newMovies: List<MovieDetails>) {
        movies = newMovies
        notifyDataSetChanged()
    }
}
