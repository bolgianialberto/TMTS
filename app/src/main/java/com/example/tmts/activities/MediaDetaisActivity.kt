package com.example.tmts.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.tmts.R
import com.example.tmts.TMDbApiClient
import com.example.tmts.beans.MovieDetails
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MediaDetaisActivity : AppCompatActivity() {

    private lateinit var tmDbApiClient: TMDbApiClient
    private lateinit var ivBackSearch: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_details)

        val intent = intent
        val movieId = intent.getIntExtra("movieId", -1)
        ivBackSearch = findViewById(R.id.iv_arrow_back_media_details)

        if (movieId != -1) {
            tmDbApiClient = TMDbApiClient()
            getMovieDetails(movieId)
        } else {
            Log.e("MovieDetailsActivity", "Movie ID not found")
            finish()
        }

        ivBackSearch.setOnClickListener {
            onBackPressed()
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
        val overview: TextView = findViewById(R.id.tv_movie_details_overview)
        val genres: TextView = findViewById(R.id.tv_movie_details_genres)
        val genresImageView: ImageView = findViewById(R.id.iv_genres)
        val originCountry: TextView = findViewById(R.id.tv_origin_country)
        val originalLanguage: TextView = findViewById(R.id.tv_origin_language)

        movie.backdropPath?.let {
            Glide.with(this)
                .load("https://image.tmdb.org/t/p/w500$it")
                .placeholder(R.drawable.movie)
                .into(backdropImageView)
        }

        // title
        titleTextView.text = movie.title

        // release date
        releaseDate.text = movie.releaseDate

        // runtime
        val runtimeMinutes = movie.runtime
        val hours = runtimeMinutes / 60
        val minutes = runtimeMinutes % 60
        val formattedRuntime = "${hours}h ${minutes}m"
        runtime.text = formattedRuntime

        // overview
        overview.text = movie.overview

        // genres
        val genresList = movie.genres
        if (genresList.isNotEmpty()) {
            val genresString = genresList.joinToString(" / ") { it.name }
            genres.text = genresString
            genresImageView.visibility = View.VISIBLE
        } else {
            genresImageView.visibility = View.GONE
        }

        // origin country
        val originCountryList = movie.origin_country
        val originCountryString = originCountryList.joinToString(" / ")
        originCountry.text = originCountryString

        // origin language
        originalLanguage.text = movie.original_language
    }
}