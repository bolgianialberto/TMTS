package com.example.tmts.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.tmts.R
import com.example.tmts.TMDbApiClient
import com.example.tmts.beans.MovieDetails
import com.example.tmts.beans.SerieDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SerieDetailsActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var tmDbApiClient: TMDbApiClient
    private lateinit var currentUser: FirebaseUser
    private lateinit var followingMoviesRef: DatabaseReference
    private lateinit var ivBackSearch: Button
    private lateinit var btnFollowUnfollow: Button
    private lateinit var titleTextView: TextView
    private lateinit var backdropImageView: ImageView
    private lateinit var nSeasons: TextView
    private lateinit var overview: TextView
    private lateinit var genres: TextView
    private lateinit var genresImageView: ImageView
    private lateinit var originCountry: TextView
    private lateinit var originalLanguage: TextView
    private lateinit var firstReleaseDate: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_serie_details)

        val intent = intent
        val serieId = intent.getIntExtra("serieId", -1)

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser ?: run {
            Log.e("MediaDetailsActivity", "User not logged in")
            finish()
            return
        }

        mDbRef = FirebaseDatabase.getInstance().getReference()

        ivBackSearch = findViewById(R.id.iv_arrow_back_serie_details)
        btnFollowUnfollow = findViewById(R.id.btn_follow_unfollow)
        titleTextView = findViewById(R.id.tv_serie_details_title)
        backdropImageView = findViewById(R.id.iv_serie_details_backdrop)
        nSeasons = findViewById(R.id.tv_serie_details_n_seasons)
        overview = findViewById(R.id.tv_serie_details_overview)
        genres = findViewById(R.id.tv_serie_details_genres)
        genresImageView = findViewById(R.id.iv_genres)
        originCountry = findViewById(R.id.tv_serie_details_origin_country)
        originalLanguage = findViewById(R.id.tv_serie_details_origin_language)
        firstReleaseDate = findViewById(R.id.tv_serie_details_release_date)

        if (serieId != -1) {
            tmDbApiClient = TMDbApiClient()
            getSerieDetails(serieId)
        } else {
            Log.e("MovieDetailsActivity", "Movie ID not found")
            finish()
        }

        ivBackSearch.setOnClickListener {
            onBackPressed()
        }

    }

    private fun getSerieDetails(serieId: Int) {
        val call = tmDbApiClient.getClient().getSerieDetails(serieId, tmDbApiClient.getApiKey())

        call.enqueue(object: Callback<SerieDetails> {
            override fun onResponse(call: Call<SerieDetails>, response: Response<SerieDetails>) {
                if (response.isSuccessful) {
                    val serie = response.body()
                    if (serie != null) {
                        updateUI(serie)
                    } else {
                        Log.e("MovieDetailsActivity", "Movie details not found")
                    }
                } else {
                    Log.e("MovieDetailsActivity", "Error ${response.code()}: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<SerieDetails>, t: Throwable) {
                Log.e("MovieDetailsActivity", "Network Error: ${t.message}")
            }
        })
    }

    private fun updateUI(serie: SerieDetails) {
        serie.backdropPath?.let {
            Glide.with(this)
                .load("https://image.tmdb.org/t/p/w500$it")
                .placeholder(R.drawable.movie)
                .into(backdropImageView)
        }

        // title
        titleTextView.text = serie.title

        // n seasons
        val seasonText = if (serie.number_of_seasons == 1) "season" else "seasons"
        nSeasons.text = "${serie.number_of_seasons} $seasonText"

        // overview
        overview.text = serie.overview

        // genres
        val genresList = serie.genres
        if (genresList.isNotEmpty()) {
            val genresString = genresList.joinToString(" / ") { it.name }
            genres.text = genresString
            genresImageView.visibility = View.VISIBLE
        } else {
            genresImageView.visibility = View.GONE
        }

        // release date
        firstReleaseDate.text = serie.first_air_date

        // origin country
        val originCountryList = serie.origin_country
        val originCountryString = originCountryList.joinToString(" / ")
        originCountry.text = originCountryString

        // origin language
        originalLanguage.text = serie.original_language

        /* plus button
        val btnFollowUnfollow: Button = findViewById(R.id.btn_follow_unfollow)
        val followingMoviesRef = mDbRef.child("users").child(currentUser!!.uid).child("following_movies")
        val movieIdToCheck: String = (movie.id).toString()



        btnFollowUnfollow.setOnClickListener{
            followingMoviesRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(movieIdToCheck)) {
                        // Il film è presente nei seguiti, rimuovilo
                        followingMoviesRef.child(movieIdToCheck).removeValue()
                        btnFollowUnfollow.setBackgroundResource(R.drawable.add)
                    } else {
                        // Il film non è presente nei seguiti, aggiungilo
                        followingMoviesRef.child(movieIdToCheck).setValue(true)
                        btnFollowUnfollow.setBackgroundResource(R.drawable.remove)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Errore nel recupero dei dati: ${error.message}")
                }

            })
        }

         */

    }
}