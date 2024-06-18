package com.example.tmts.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.tmts.FirebaseInteraction
import com.example.tmts.MediaRepository
import com.example.tmts.R
import com.example.tmts.TMDbApiClient
import com.example.tmts.beans.MovieDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MovieDetaisActivity : AppCompatActivity() {
    private lateinit var ivBackSearch: Button
    private lateinit var btnFollowUnfollow: Button
    private lateinit var titleTextView: TextView
    private lateinit var backdropImageView: ImageView
    private lateinit var releaseDate: TextView
    private lateinit var runtime: TextView
    private lateinit var overview: TextView
    private lateinit var genres: TextView
    private lateinit var genresImageView: ImageView
    private lateinit var originCountry: TextView
    private lateinit var originalLanguage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_details)

        val intent = intent
        val movieId = intent.getIntExtra("movieId", -1)

        ivBackSearch = findViewById(R.id.iv_arrow_back_movie_details)
        btnFollowUnfollow = findViewById(R.id.btn_follow_unfollow)
        titleTextView = findViewById(R.id.tv_movie_details_title)
        backdropImageView = findViewById(R.id.iv_movie_details_backdrop)
        releaseDate = findViewById(R.id.tv_movie_details_date)
        runtime = findViewById(R.id.tv_movie_details_time)
        overview = findViewById(R.id.tv_movie_details_overview)
        genres = findViewById(R.id.tv_movie_details_genres)
        genresImageView = findViewById(R.id.iv_genres)
        originCountry = findViewById(R.id.tv_origin_country)
        originalLanguage = findViewById(R.id.tv_origin_language)


        MediaRepository.getMovieDetails(
            movieId,
            onSuccess = ::updateUI,
            onError = ::onError
        )

        ivBackSearch.setOnClickListener {
            onBackPressed()
        }

        setInitialButtonState(movieId)
    }

    private fun onError(){
        Log.e("MovieDetailsActivity", "Something went wrong")
    }

    private fun updateUI(movie: MovieDetails) {
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

        // plus button
        //val btnFollowUnfollow: Button = findViewById(R.id.btn_follow_unfollow)
        //val followingMoviesRef = mDbRef.child("users").child(currentUser!!.uid).child("following_movies")
        val movieIdToCheck: String = (movie.id).toString()

        btnFollowUnfollow.setOnClickListener{
            FirebaseInteraction.checkMovieExistanceInFollowing(movieIdToCheck.toInt()){ exists ->
                if(exists) {
                    FirebaseInteraction.removeMovieFromFollowing(movieIdToCheck.toInt()) {
                        btnFollowUnfollow.setBackgroundResource(R.drawable.add)
                    }
                } else {
                    FirebaseInteraction.addMovieToFollowing(movieIdToCheck.toInt()) {
                        btnFollowUnfollow.setBackgroundResource(R.drawable.remove)
                    }
                }

            }
        }

    }

    private fun setInitialButtonState(movieId: Int) {
        FirebaseInteraction.checkMovieExistanceInFollowing(movieId){ exists ->
            if(exists) {
                btnFollowUnfollow.setBackgroundResource(R.drawable.remove)
            } else {
                btnFollowUnfollow.setBackgroundResource(R.drawable.add)
            }

        }
    }
}