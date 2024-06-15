package com.example.tmts.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmts.MediaRepository
import com.example.tmts.R
import com.example.tmts.TMDbApiClient
import com.example.tmts.adapters.NetworkAdapter
import com.example.tmts.adapters.SeasonAdapter
import com.example.tmts.beans.SeasonDetails
import com.example.tmts.beans.SerieDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener

class SerieDetailsActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var currentUser: FirebaseUser
    private lateinit var followingSeriesRef: DatabaseReference
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
    private lateinit var llSeasons: LinearLayout
    private lateinit var rvNetwork: RecyclerView
    private lateinit var networkAdapter: NetworkAdapter
    private var serieId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_serie_details)

        val intent = intent
        serieId = intent.getIntExtra("serieId", -1)

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser ?: run {
            Log.e("MediaDetailsActivity", "User not logged in")
            finish()
            return
        }

        mDbRef = FirebaseDatabase.getInstance().getReference()
        followingSeriesRef = mDbRef.child("users").child(currentUser.uid).child("following_series")

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
        networkAdapter = NetworkAdapter(this, emptyList())
        rvNetwork = findViewById(R.id.rv_serie_networks)
        llSeasons = findViewById(R.id.ll_seasons)

        rvNetwork.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvNetwork.adapter = networkAdapter

        if (serieId != -1) {
            MediaRepository.getSerieDetails(
                serieId,
                onSuccess = ::updateUI,
                onError = ::onError,
            )
        } else {
            Log.e("MovieDetailsActivity", "Serie ID not found")
            finish()
        }

        ivBackSearch.setOnClickListener {
            onBackPressed()
        }

        setInitialButtonState(serieId)
    }
    private fun onError(){
        Log.e("SerieDetailsActivity", "Something went wrong")
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

        // plus button
        val serieIdToCheck: String = (serie.id).toString()

        // networks
        networkAdapter.updateMedia(serie.networks)

        // seasons
        llSeasons.setOnClickListener {
            val intent = Intent(this, SeasonDetailsActivity::class.java)
            intent.putExtra("serieId", serieId)
            this.startActivity(intent)
        }

        // follow/unfollow
        btnFollowUnfollow.setOnClickListener{
            followingSeriesRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(serieIdToCheck)) {
                        // la serie è presente nei seguiti, rimuovilo
                        followingSeriesRef.child(serieIdToCheck).removeValue()
                        btnFollowUnfollow.setBackgroundResource(R.drawable.add)
                    } else {
                        // la serie non è presente nei seguiti, aggiungilo
                        addSerieToDB(serie)
                        btnFollowUnfollow.setBackgroundResource(R.drawable.remove)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Errore nel recupero dei dati: ${error.message}")
                }

            })
        }

    }

    private fun addSerieToDB(serie: SerieDetails) {
        val seriesRef = followingSeriesRef.child(serie.id.toString())
        seriesRef.child("nextToSee").setValue("1_1")
        seriesRef.child("timestamp").setValue(ServerValue.TIMESTAMP)

        for (n_season in 1..serie.number_of_seasons){
            val seasonRef = seriesRef.child("seasons").child(n_season.toString())
            seasonRef.child("status").setValue("notWatched")

            MediaRepository.getSeasonDetails(
                serieId,
                n_season,
                onSuccess = {season ->
                    season?.let {
                        val episodesRef = seasonRef.child("episodes")

                        for (n_episode in 1..it.number_of_episodes) {
                            episodesRef.child(n_episode.toString()).setValue(false)
                        }
                    }
                },
                onError = ::onError
            )
        }
    }

    private fun setInitialButtonState(serieId: Int) {
        currentUser?.let {
            val serieIdToCheck: String = serieId.toString()

            followingSeriesRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val btnFollowUnfollow: Button = findViewById(R.id.btn_follow_unfollow)
                    if (snapshot.hasChild(serieIdToCheck)) {
                        btnFollowUnfollow.setBackgroundResource(R.drawable.remove)
                    } else {
                        btnFollowUnfollow.setBackgroundResource(R.drawable.add)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Errore nel recupero dei dati: ${error.message}")
                }
            })
        }
    }
}