package com.example.tmts.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmts.OnCheckButtonClickListener
import com.example.tmts.R
import com.example.tmts.activities.MovieDetailsActivity
import com.example.tmts.beans.MovieDetails

class HomeMovieAdapter(
    private val context: Context,
    private var mediaItems: List<MovieDetails>,
    private val clickListener: OnCheckButtonClickListener
) : RecyclerView.Adapter<HomeMovieAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.home_movie_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaItem = mediaItems[position]
        holder.bind(mediaItem, clickListener)
    }

    override fun getItemCount(): Int {
        return mediaItems.size
    }

    fun updateMovies(mediaItems: List<MovieDetails>) {
        this.mediaItems = mediaItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewBackdrop: ImageView = itemView.findViewById(R.id.iv_home_movie_backdrop)
        private val textViewTitle: TextView = itemView.findViewById(R.id.tv_home_movie_title)
        private val textViewTime: TextView = itemView.findViewById(R.id.tv_home_movie_time)
        private val textViewGenres: TextView = itemView.findViewById(R.id.tv_home_movie_genres)
        private val buttonCheck: Button = itemView.findViewById(R.id.btn_home_movie_check)
        private val llMovieInfo: LinearLayout = itemView.findViewById(R.id.ll_movie_info)

        fun bind(movie: MovieDetails, clickListener: OnCheckButtonClickListener) {
            Glide.with(context)
                .load("https://image.tmdb.org/t/p/w500${movie.posterPath}")
                .placeholder(R.drawable.movie)
                .into(imageViewBackdrop)

            textViewTitle.text = movie.title

            val runtimeMinutes = movie.runtime
            val hours = runtimeMinutes / 60
            val minutes = runtimeMinutes % 60
            val formattedRuntime = "${hours}h ${minutes}m"
            textViewTime.text = formattedRuntime

            val genresString = movie.genres.joinToString(" / ") { it.name }
            textViewGenres.text = genresString

            llMovieInfo.setOnClickListener {
                val intent = Intent(context, MovieDetailsActivity::class.java)
                intent.putExtra("movieId", movie.id)
                context.startActivity(intent)
            }

            buttonCheck.setOnClickListener {
                clickListener.onCheckButtonClicked(movie.id.toString(), movie.title)
            }
        }
    }
}
