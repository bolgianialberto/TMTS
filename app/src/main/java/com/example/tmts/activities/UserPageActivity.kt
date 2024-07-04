package com.example.tmts.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmts.FirebaseInteraction
import com.example.tmts.MediaRepository
import com.example.tmts.R
import com.example.tmts.adapters.MediaAdapter
import com.example.tmts.beans.Media

class UserPageActivity: AppCompatActivity() {
    private lateinit var tvUsername: TextView
    private lateinit var tvBio: TextView
    private lateinit var tvFollowerCount: TextView
    private lateinit var tvFollowingCount: TextView
    private lateinit var ivAccountIcon: ImageView
    private lateinit var bBackSearch: Button
    private lateinit var bFollowUnfollow: Button
    private lateinit var followedMoviesAdapter: MediaAdapter
    private lateinit var followedSeriesAdapter: MediaAdapter
    private lateinit var llFollowedMovies: LinearLayout
    private lateinit var llFollowedSeries: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_page)

        val intent = intent
        val uid = intent.getStringExtra("uid")

        // Initialize views
        tvUsername = findViewById(R.id.tv_account_username)
        tvBio = findViewById(R.id.tv_bio)
        tvFollowerCount = findViewById(R.id.tv_follower_count)
        tvFollowingCount = findViewById(R.id.tv_following_count)
        ivAccountIcon = findViewById(R.id.account_icon)
        bFollowUnfollow = findViewById(R.id.b_follow)
        llFollowedMovies = findViewById(R.id.ll_followed_movies)
        llFollowedSeries = findViewById(R.id.ll_followed_series)
        bBackSearch = findViewById(R.id.b_arrow_back_user_page)

        // Setup adapters for Recycle Views
        followedMoviesAdapter = MediaAdapter(this, emptyList()) { movie ->
            val intent = Intent(this, MovieDetailsActivity::class.java)
            intent.putExtra("movieId", movie.id)
            startActivity(intent)
        }

        followedSeriesAdapter = MediaAdapter(this, emptyList()) {serie ->
            val intent = Intent(this, SerieDetailsActivity::class.java)
            intent.putExtra("serieId", serie.id)
            startActivity(intent)
        }

        val rvFollowedMovie: RecyclerView = findViewById(R.id.rv_followed_movies)
        rvFollowedMovie.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvFollowedMovie.adapter = followedMoviesAdapter

        val rvFollowedSerie: RecyclerView = findViewById(R.id.rv_followed_series)
        rvFollowedSerie.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvFollowedSerie.adapter = followedSeriesAdapter

        // Update TextViews and other data
        FirebaseInteraction.getUsername(
            userId = uid!!,
            onSuccess = { username ->
                tvUsername.text = username
            },
            onFailure = { errorMessage ->
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            })

        FirebaseInteraction.getUserBio(
            uid!!,
            onSuccess = { bio ->
                tvBio.text = bio
            },
            onFailure = {

            }
        )

        loadUserFollowerData()

        FirebaseInteraction.getFollowingMovies(uid) { movies ->
            val followingMovies = mutableListOf<String>()

            for (movieId in movies) {
                followingMovies.add(movieId.first)
            }

            onFollowedMoviesFetched(followingMovies)
        }

        FirebaseInteraction.getFollowingSeries(uid) { series ->
            val followingSeries = mutableListOf<String>()

            for (serieId in series) {
                followingSeries.add(serieId.first)
            }

            onFollowedSeriesFetched(followingSeries)
        }

        llFollowedMovies.setOnClickListener{
            if (rvFollowedMovie.visibility == View.GONE) {
                rvFollowedMovie.visibility = View.VISIBLE
            } else {
                rvFollowedMovie.visibility = View.GONE
            }
        }

        llFollowedSeries.setOnClickListener{
            if (rvFollowedSerie.visibility == View.GONE) {
                rvFollowedSerie.visibility = View.VISIBLE
            } else {
                rvFollowedSerie.visibility = View.GONE
            }
        }

        // Setup button listeners and initial state
        bFollowUnfollow.setOnClickListener{
            followUnfollowUser(uid!!)
        }

        bBackSearch.setOnClickListener {
            finish()
        }

        setInitialButtonState(uid)
    }

    private fun onFollowedMoviesFetched(movieIds: MutableList<String>) {
        val movies = mutableListOf<Media>()
        var completedRequests = 0
        val totalRequests = movieIds.size

        movieIds.forEach {movieId ->
            MediaRepository.getMovieDetails(movieId.toInt(),
                onSuccess = {movie ->
                    val movieMedia = Media(movie.id, movie.title, "", movie.posterPath)
                    movies.add(movieMedia)
                    completedRequests++

                    if (completedRequests == totalRequests) {
                        followedMoviesAdapter.updateMedia(movies)
                    }
                },
                onError = {})
        }
    }

    private fun onFollowedSeriesFetched(serieIds: MutableList<String>) {
        val series = mutableListOf<Media>()
        var completedRequests = 0
        val totalRequests = serieIds.size

        serieIds.forEach {serieId ->
            MediaRepository.getSerieDetails(serieId.toInt(),
                onSuccess = {serie ->
                    val serieMedia = Media(serie.id, serie.title, "", serie.posterPath)
                    series.add(serieMedia)
                    completedRequests++

                    if (completedRequests == totalRequests) {
                        followedSeriesAdapter.updateMedia(series)
                    }
                },
                onError = {})
        }
    }



    private fun followUnfollowUser(uid: String) {
        FirebaseInteraction.getFollowedUsers { followedUsers ->
            if (followedUsers.contains(uid)) {
                FirebaseInteraction.removeSelfFromUserFollowers(uid) {
                    FirebaseInteraction.removeTargetUserFromFollowing(uid) {}
                    bFollowUnfollow.setBackgroundResource(R.drawable.add)
                }
            } else {
                FirebaseInteraction.addTargetUserToFollowing(uid) {
                    FirebaseInteraction.addSelfToFollowed(uid) {}
                    bFollowUnfollow.setBackgroundResource(R.drawable.remove)
                }
            }

            loadUserFollowerData()
        }
    }

    private fun loadUserFollowerData() {
        // Set number of users following me from Firebase
        FirebaseInteraction.getFollowersUsers { followers -> //TODO: add uid requirement
            tvFollowerCount.text = followers.size.toString()
        }

        // Check number of users I follow from Firebase
        FirebaseInteraction.getFollowedUsers { followed ->
            tvFollowingCount.text = followed.size.toString()
        }
    }

    private fun setInitialButtonState(userId: String) {
        FirebaseInteraction.checkFollowedExistance(userId) { exists ->
            if(exists) {
                bFollowUnfollow.setBackgroundResource(R.drawable.remove)
            } else {
                bFollowUnfollow.setBackgroundResource(R.drawable.add)
            }
        }
    }

    private fun onError(){
        Log.e("CommentsMovieActivity", "Something went wrong")
    }
}