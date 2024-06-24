package com.example.tmts.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmts.FirebaseInteraction
import com.example.tmts.MediaRepository
import com.example.tmts.R
import com.example.tmts.TMDbApiClient
import com.example.tmts.adapters.AddToWatchlistAdapter
import com.example.tmts.adapters.NetworkAdapter
import com.example.tmts.adapters.SeasonAdapter
import com.example.tmts.beans.MovieDetails
import com.example.tmts.beans.SeasonDetails
import com.example.tmts.beans.SerieDetails
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    private lateinit var llComments: LinearLayout
    private lateinit var btnRate: Button
    private lateinit var tvAverageRate: TextView
    private lateinit var ivFilledStar: ImageView
    private lateinit var btnWatchlist: Button
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
        llComments = findViewById(R.id.ll_comments)
        btnRate = findViewById(R.id.btn_rate)
        tvAverageRate = findViewById(R.id.tv_rating)
        ivFilledStar = findViewById(R.id.iv_filled_star)
        btnWatchlist = findViewById(R.id.btn_watchlist)

        val colorFilter = PorterDuffColorFilter(Color.parseColor("#80000000"), PorterDuff.Mode.SRC_ATOP)
        backdropImageView.colorFilter = colorFilter

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
        setInitialRateState(serieId)
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

        // average rate
        FirebaseInteraction.getAverageRateForMedia(
            serie.id,
            "serie",
            onSuccess = {averageRate ->
                if(averageRate != 0.0F){
                    val formattedRate = String.format("%.1f", averageRate)
                    tvAverageRate.text = "$formattedRate/5"
                    tvAverageRate.visibility = View.VISIBLE
                    ivFilledStar.visibility = View.VISIBLE
                }
            },
            onError = ::onError
        )

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

        llComments.setOnClickListener{
            val intent = Intent(this, ReviewsMediaActivity::class.java)
            intent.putExtra("mediaId", serie.id)
            intent.putExtra("mediaType", "serie")
            startActivity(intent)
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

        btnRate.setOnClickListener {
            showRatingPopover(serie)
        }

        btnWatchlist.setOnClickListener{
            showWatchlistPopover(serie)
        }
    }

    private fun showWatchlistPopover(serie: SerieDetails) {
        val builder = AlertDialog.Builder(this@SerieDetailsActivity)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.popover_watchlist, null)
        builder.setView(dialogView)

        val customTitleView = inflater.inflate(R.layout.custom_dialog_title, null)
        builder.setCustomTitle(customTitleView)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rv_add_to_watchlist)
        val fabAddWatchlist = dialogView.findViewById<FloatingActionButton>(R.id.fab_add_new_watchlist)

        val alertDialog = builder.create()
        alertDialog.show()

        fabAddWatchlist.setOnClickListener {
            showAddWatchlistDialog(serie)
            alertDialog.dismiss() // Mantieni il dialogo aperto
        }

        fun fetchWatchlists() {
            FirebaseInteraction.fetchWatchlistsWithDetails(
                onSuccess = { watchlists ->
                    val adapter = AddToWatchlistAdapter(this@SerieDetailsActivity, watchlists) { watchlist ->
                        FirebaseInteraction.checkMediaExistanceInWatchlist(
                            serie.id,
                            watchlist.name,
                            "serie",
                            onSuccess = { exists ->
                                Log.d("Exists?", "$exists")
                                if (exists) {
                                    Toast.makeText(this@SerieDetailsActivity, "${serie.title} already in ${watchlist.name}", Toast.LENGTH_SHORT).show()
                                } else {
                                    FirebaseInteraction.addMediaToWatchlist(
                                        serie.id,
                                        watchlist.name,
                                        "serie",
                                        onSuccess = {
                                            Toast.makeText(this@SerieDetailsActivity, "Serie added to ${watchlist.name}", Toast.LENGTH_SHORT).show()
                                            fetchWatchlists() // Ricarica le watchlist per aggiornarle
                                        },
                                        onError = {
                                            Log.e("FirebaseAddToWatchlist", it)
                                        }
                                    )
                                }
                            },
                            onError = {
                                Log.e("FirebaseAddToWatchlist", it)
                            }
                        )
                    }
                    recyclerView.layoutManager = LinearLayoutManager(this@SerieDetailsActivity)
                    recyclerView.adapter = adapter
                },
                onError = { errorMessage ->
                    Toast.makeText(this@SerieDetailsActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            )
        }

        fetchWatchlists()

        customTitleView.findViewById<ImageView>(R.id.iv_close_dialog)?.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun showAddWatchlistDialog(serie: SerieDetails){
        val builder = AlertDialog.Builder(this@SerieDetailsActivity)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.popover_add_watchlist, null)
        builder.setView(dialogView)

        val etWatchlistName = dialogView.findViewById<EditText>(R.id.et_watchlist_name)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnOk = dialogView.findViewById<Button>(R.id.btn_ok)

        val alertDialog = builder.create()

        alertDialog.setOnShowListener {
            etWatchlistName.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etWatchlistName, InputMethodManager.SHOW_IMPLICIT)
        }

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        btnOk.setOnClickListener {
            val watchlistName = etWatchlistName.text.toString().trim()
            if (watchlistName.isNotEmpty()) {
                FirebaseInteraction.addMediaToWatchlist(
                    serie.id,
                    watchlistName,
                    "serie",
                    onSuccess = {
                        Toast.makeText(this, "Serie added to ${watchlistName}", Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss()
                    },
                    onError = {
                        Log.e("FirebaseAddToWatchlist", it)
                    }
                )
            } else {
                Toast.makeText(this, "Watchlist name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        alertDialog.show()
    }

    private fun showRatingPopover(serie: SerieDetails){
        val builder = AlertDialog.Builder(this@SerieDetailsActivity)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.rate_layout, null)
        builder.setView(dialogView)

        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)

        // Controlla se l'utente ha già votato il film
        FirebaseInteraction.checkUserRatingExistance(
            serie.id.toInt(),
            onSuccess = { exists ->
                if (exists) {
                    // Se l'utente ha già votato, recupera il voto precedente
                    FirebaseInteraction.getUserRateOnMedia(
                        serie.id.toString(),
                        onSuccess = { oldRating ->
                            // Imposta il RatingBar al voto precedente dell'utente
                            ratingBar.rating = oldRating

                            builder.setTitle("Rate this serie")
                                .setPositiveButton("Ok") { dialog, which ->
                                    val rating = ratingBar.rating
                                    if (rating == 0.0f) {
                                        dialog.dismiss()
                                    } else {
                                        // Aggiorna la media delle valutazioni del film
                                        FirebaseInteraction.updateMediaRatingAverage(
                                            serie.id.toString(),
                                            "serie",
                                            rating,
                                            onSuccess = {
                                                // Aggiungi il voto dell'utente con successo
                                                FirebaseInteraction.addRatingToUser(
                                                    serie.id.toString(),
                                                    rating,
                                                    onSuccess = {
                                                        updateUI(serie)
                                                        Toast.makeText(
                                                            this@SerieDetailsActivity,
                                                            "Rating selected: $rating",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    },
                                                    onError = ::onError
                                                )
                                            },
                                            onError = ::onError
                                        )
                                    }
                                }
                                .setNegativeButton("Cancel") { dialog, which ->
                                    dialog.dismiss()
                                }

                            val alertDialog = builder.create()
                            alertDialog.show()
                        },
                        onError = ::onError
                    )
                } else {
                    // L'utente non ha ancora votato, usa il rating di default (0.0)
                    builder.setTitle("Rate this serie")
                        .setPositiveButton("Ok") { dialog, which ->
                            val rating = ratingBar.rating
                            if (rating == 0.0f) {
                                dialog.dismiss()
                            } else {
                                btnRate.setBackgroundResource(R.drawable.outlined_filled_star)
                                // Aggiorna la media delle valutazioni del film
                                FirebaseInteraction.updateMediaRatingAverage(
                                    serie.id.toString(),
                                    "serie",
                                    rating,
                                    onSuccess = {
                                        // Aggiungi il voto dell'utente con successo
                                        FirebaseInteraction.addRatingToUser(
                                            serie.id.toString(),
                                            rating,
                                            onSuccess = {
                                                updateUI(serie)
                                                Toast.makeText(
                                                    this@SerieDetailsActivity,
                                                    "Rating selected: $rating",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            onError = ::onError
                                        )
                                    },
                                    onError = ::onError
                                )
                            }
                        }
                        .setNegativeButton("Cancel") { dialog, which ->
                            dialog.dismiss()
                        }

                    val alertDialog = builder.create()
                    alertDialog.show()
                }
            },
            onError = ::onError
        )
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

    private fun setInitialRateState(mediaId: Int){
        FirebaseInteraction.checkUserRatingExistance(
            mediaId,
            onSuccess = {exists ->
                if(exists){
                    btnRate.setBackgroundResource(R.drawable.outlined_filled_star)
                } else {
                    btnRate.setBackgroundResource(R.drawable.star)
                }
            },
            onError = ::onError
        )
    }

    fun onError(message: String){
        Log.d("MovieDetailsActivity", message)
    }
}