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
import com.example.tmts.FirebaseInteraction
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
            intent.putExtra("serieTitle", serie.title)
            this.startActivity(intent)
        }

        // follow/unfollow
        btnFollowUnfollow.setOnClickListener{
            FirebaseInteraction.checkSerieExistanceInFollowing(
                serieId ) {exists ->
                if(exists) {
                    FirebaseInteraction.removeSerieFromFollowing(serieId) {
                        btnFollowUnfollow.setBackgroundResource(R.drawable.add)
                        FirebaseInteraction.removeFollowerFromSeries(serieId)
                    }

                } else {
                    FirebaseInteraction.addSerieToFollowing(serieId) {
                        btnFollowUnfollow.setBackgroundResource(R.drawable.remove)
                        FirebaseInteraction.addFollowerToSeries(serieId)
                    }
                }
            }
        }
    }

    private fun setInitialButtonState(serieId: Int) {
        FirebaseInteraction.checkSerieExistanceInFollowing(serieId){ exists ->
            if(exists) {
                btnFollowUnfollow.setBackgroundResource(R.drawable.remove)
            } else {
                btnFollowUnfollow.setBackgroundResource(R.drawable.add)
            }

        }
    }
}