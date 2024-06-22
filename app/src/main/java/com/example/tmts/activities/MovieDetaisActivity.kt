package com.example.tmts.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
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
import com.example.tmts.adapters.AddToWatchlistAdapter
import com.example.tmts.beans.MediaDetails
import com.example.tmts.beans.MovieDetails
import com.google.android.material.floatingactionbutton.FloatingActionButton

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
    private lateinit var llComments: LinearLayout
    private lateinit var btnRate: Button
    private lateinit var btnAddToWatchlist: Button
    private lateinit var tvAverageRate: TextView
    private lateinit var ivFilledStar: ImageView

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
        llComments = findViewById(R.id.ll_comments)
        btnRate = findViewById(R.id.btn_rate)
        btnAddToWatchlist = findViewById(R.id.btn_watchlist)
        tvAverageRate = findViewById(R.id.tv_rating)
        ivFilledStar = findViewById(R.id.iv_filled_star)

        MediaRepository.getMovieDetails(
            movieId,
            onSuccess = ::updateUI,
            onError = ::onError
        )

        ivBackSearch.setOnClickListener {
            onBackPressed()
        }

        setInitialButtonState(movieId)
        setInitialRateState(movieId)
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

        // average rate
        FirebaseInteraction.getAverageRateForMedia(
            movie.id,
            "movie",
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

        btnFollowUnfollow.setOnClickListener{
            followUnfollowMovie(movie)
        }

        llComments.setOnClickListener{
            val intent = Intent(this, ReviewsMediaActivity::class.java)
            intent.putExtra("mediaId", movie.id)
            intent.putExtra("mediaType", "movie")
            startActivity(intent)
        }

        btnRate.setOnClickListener {
            showRatingPopover(movie)
        }

        btnAddToWatchlist.setOnClickListener{
            showWatchlistPopover(movie)
        }
    }

    private fun showWatchlistPopover(movie: MovieDetails){
        val builder = AlertDialog.Builder(this@MovieDetaisActivity)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.popover_watchlist, null)
        builder.setView(dialogView)

        val customTitleView = inflater.inflate(R.layout.custom_dialog_title, null)
        builder.setCustomTitle(customTitleView)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rv_add_to_watchlist)
        val fabAddWatchlist = dialogView.findViewById<FloatingActionButton>(R.id.fab_add_new_watchlist)

        var adapter: AddToWatchlistAdapter? = null

        fabAddWatchlist.setOnClickListener{
            showAddWatchlistDialog()
        }

        val alertDialog = builder.create()
        alertDialog.show()

        FirebaseInteraction.fetchWatchlistsWithDetails(
            onSuccess = { watchlists ->
                adapter = AddToWatchlistAdapter(this@MovieDetaisActivity, watchlists) {watchlist ->
                    FirebaseInteraction.addMediaToWatchlist(
                        movie.id,
                        watchlist.name,
                        "movie",
                        onSuccess = {
                            Toast.makeText(this, "Movie added to ${watchlist.name}", Toast.LENGTH_LONG).show()
                            alertDialog.dismiss()
                        },
                        onError = {
                            Log.e("FirebaseAddToWatchlist", it)
                        }
                    )
                }
                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.adapter = adapter
            },
            onError = { errorMessage ->
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        )

        customTitleView.findViewById<ImageView>(R.id.iv_close_dialog)?.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun showAddWatchlistDialog(){

    }

    private fun followUnfollowMovie(movie: MovieDetails){
        FirebaseInteraction.checkMovieExistanceInFollowing(movie.id){ exists ->
            if(exists) {
                FirebaseInteraction.removeMovieFromFollowing(movie.id) {
                    btnFollowUnfollow.setBackgroundResource(R.drawable.add)
                    FirebaseInteraction.removeFollowerFromMovie(movie.id)
                }
            } else {
                FirebaseInteraction.addMovieToFollowing(movie.id) {
                    btnFollowUnfollow.setBackgroundResource(R.drawable.remove)
                    FirebaseInteraction.addFollowerToMovie(movie.id)
                }
            }

        }
    }
    private fun showRatingPopover(movie: MovieDetails){
        val builder = AlertDialog.Builder(this@MovieDetaisActivity)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.rate_layout, null)
        builder.setView(dialogView)

        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)

        // Controlla se l'utente ha già votato il film
        FirebaseInteraction.checkUserRatingExistance(
            movie.id.toInt(),
            onSuccess = { exists ->
                if (exists) {
                    // Se l'utente ha già votato, recupera il voto precedente
                    FirebaseInteraction.getUserRateOnMedia(
                        movie.id.toString(),
                        onSuccess = { oldRating ->
                            // Imposta il RatingBar al voto precedente dell'utente
                            ratingBar.rating = oldRating

                            builder.setTitle("Rate this movie")
                                .setPositiveButton("Ok") { dialog, which ->
                                    val rating = ratingBar.rating
                                    if (rating == 0.0f) {
                                        dialog.dismiss()
                                    } else {
                                        // Aggiorna la media delle valutazioni del film
                                        FirebaseInteraction.updateMediaRatingAverage(
                                            movie.id.toString(),
                                            "movie",
                                            rating,
                                            onSuccess = {
                                                // Aggiungi il voto dell'utente con successo
                                                FirebaseInteraction.addRatingToUser(
                                                    movie.id.toString(),
                                                    rating,
                                                    onSuccess = {
                                                        updateUI(movie)
                                                        Toast.makeText(
                                                            this@MovieDetaisActivity,
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
                    builder.setTitle("Rate this movie")
                        .setPositiveButton("Ok") { dialog, which ->
                            val rating = ratingBar.rating
                            if (rating == 0.0f) {
                                dialog.dismiss()
                            } else {
                                btnRate.setBackgroundResource(R.drawable.outlined_filled_star)
                                // Aggiorna la media delle valutazioni del film
                                FirebaseInteraction.updateMediaRatingAverage(
                                    movie.id.toString(),
                                    "movie",
                                    rating,
                                    onSuccess = {
                                        // Aggiungi il voto dell'utente con successo
                                        FirebaseInteraction.addRatingToUser(
                                            movie.id.toString(),
                                            rating,
                                            onSuccess = {
                                                updateUI(movie)
                                                Toast.makeText(
                                                    this@MovieDetaisActivity,
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

    private fun setInitialButtonState(movieId: Int) {
        FirebaseInteraction.checkMovieExistanceInFollowing(movieId){ exists ->
            if(exists) {
                btnFollowUnfollow.setBackgroundResource(R.drawable.remove)
            } else {
                btnFollowUnfollow.setBackgroundResource(R.drawable.add)
            }

        }
    }

    private fun setInitialRateState(movieId: Int){
        FirebaseInteraction.checkUserRatingExistance(
            movieId,
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