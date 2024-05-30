package com.example.tmts.activities

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.tmts.MovieDetails
import com.example.tmts.R
import com.example.tmts.TMDbApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MovieDetailsActivity : AppCompatActivity() {

    private lateinit var tmDbApiClient: TMDbApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_details)

        val intent = intent

        val movieId = intent.getIntExtra("movieId", -1)

        if (movieId != -1) {
            tmDbApiClient = TMDbApiClient()
            getMovieDetails(movieId)
        } else {
            Log.e("MovieDetailsActivity", "Movie ID not found")
            finish()
        }
    }

    private fun getMovieDetails(movieId: Int) {
        val call = tmDbApiClient.getClient().getMovieDetails(movieId, tmDbApiClient.getApiKey())

        call.enqueue(object: Callback<MovieDetails> {
            override fun onResponse(call: Call<MovieDetails>, response: Response<MovieDetails>) {
                if (response.isSuccessful) {
                    val movie = response.body()
                    if (movie != null) {
                        updateUI(movie)
                    } else {
                        Log.e("MovieDetailsActivity", "Movie details not found")
                    }
                } else {
                    Log.e("MovieDetailsActivity", "Error ${response.code()}: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<MovieDetails>, t: Throwable) {
                Log.e("MovieDetailsActivity", "Network Error: ${t.message}")
            }
        })
    }

    private fun updateUI(movie: MovieDetails) {
        val titleTextView: TextView = findViewById(R.id.tv_movie_details_title)
        val backdropImageView: ImageView = findViewById(R.id.iv_movie_details_backdrop)
        val releaseDate: TextView = findViewById(R.id.tv_movie_details_date)
        val runtime: TextView = findViewById(R.id.tv_movie_details_time)

        movie.backdropPath?.let {
            Glide.with(this)
                .load("https://image.tmdb.org/t/p/w500$it")
                .placeholder(R.drawable.movie)
                .into(backdropImageView)
        }

        titleTextView.text = movie.title

        releaseDate.text = movie.releaseDate

        val runtimeMinutes = movie.runtime
        val hours = runtimeMinutes / 60
        val minutes = runtimeMinutes % 60
        val formattedRuntime = "${hours}h ${minutes}m"
        runtime.text = formattedRuntime
    }
}