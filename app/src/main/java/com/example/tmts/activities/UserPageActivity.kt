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
import androidx.core.text.isDigitsOnly
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmts.FirebaseInteraction
import com.example.tmts.MediaRepository
import com.example.tmts.R
import com.example.tmts.adapters.MediaAdapter
import com.example.tmts.beans.Media

class UserPageActivity: AppCompatActivity() {

    private lateinit var tvUsername: TextView
    private lateinit var tvBio: TextView
    private lateinit var tvFollowersCount: TextView
    private lateinit var tvFollowingCount: TextView
    private lateinit var ivAccountIcon: ImageView
    private lateinit var bttBackSearch: Button
    private lateinit var bttFollowUnfollow: Button
    private lateinit var bttChat: Button
    private lateinit var followedMoviesAdapter: MediaAdapter
    private lateinit var followedSeriesAdapter: MediaAdapter
    private lateinit var llFollowedMovies: LinearLayout
    private lateinit var llFollowedSeries: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_page)

        val uid = intent.getStringExtra("uid")!!

        tvUsername = findViewById(R.id.tv_user_page_username)
        tvBio = findViewById(R.id.tv_user_page_bio)
        tvFollowersCount = findViewById(R.id.tv_follower_count)
        tvFollowingCount = findViewById(R.id.tv_following_count)
        ivAccountIcon = findViewById(R.id.iv_user_page_icon)
        bttFollowUnfollow = findViewById(R.id.btt_follow_unfollow)
        bttChat = findViewById(R.id.btt_user_page_chat)
        llFollowedMovies = findViewById(R.id.ll_followed_movies)
        llFollowedSeries = findViewById(R.id.ll_followed_series)
        bttBackSearch = findViewById(R.id.btt_user_page_arrow_back)

        followedMoviesAdapter = MediaAdapter(this, emptyList()) { movie ->
            val intent = Intent(this, MovieDetailsActivity::class.java)
            intent.putExtra("movieId", movie.id)
            startActivity(intent)
        }
        followedSeriesAdapter = MediaAdapter(this, emptyList()) { series ->
            val intent = Intent(this, SerieDetailsActivity::class.java)
            intent.putExtra("serieId", series.id)
            startActivity(intent)
        }

        val rvFollowedMovie: RecyclerView = findViewById(R.id.rv_followed_movies)
        rvFollowedMovie.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvFollowedMovie.adapter = followedMoviesAdapter
        val rvFollowedSeries: RecyclerView = findViewById(R.id.rv_followed_series)
        rvFollowedSeries.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvFollowedSeries.adapter = followedSeriesAdapter

        loadUserData(uid)
        loadUserFollowerData(uid)

        FirebaseInteraction.getFollowingMovies(uid) { movies ->
            val followingMovies = mutableListOf<String>()

            for (movieId in movies) {
                followingMovies.add(movieId.first)
            }
            val n =

            onFollowedMoviesFetched(movies.map { it.first })
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
            if (rvFollowedSeries.visibility == View.GONE) {
                rvFollowedSeries.visibility = View.VISIBLE
            } else {
                rvFollowedSeries.visibility = View.GONE
            }
        }

        // Setup button listeners and initial state
        bttFollowUnfollow.setOnClickListener{
            followUnfollowUser(uid)
        }

        bttChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            FirebaseInteraction.getUsername(uid,
                onSuccess = { username ->
                    intent.putExtra("userId", uid)
                    intent.putExtra("username", username)
                    startActivity(intent)
                },
                onFailure = {
                    Log.e("UserChat Error", it)
                })
        }

        bttBackSearch.setOnClickListener {
            onBackPressed()
        }

        setInitialButtonState(uid)
    }

    private fun loadUserData(userId: String) {
        FirebaseInteraction.getUserProfileImageRef(
            userId,
            onSuccess = {
                it.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(this)
                        .load(uri)
                        .into(ivAccountIcon)
                }.addOnFailureListener{
                    Log.e("StorageImg Err", "Image not found")
                }
            }, onFailure = {
                Log.e("Image Error", it)
            }
        )

        FirebaseInteraction.getUserInfo(
            userId,
            onSuccess = { user ->
                tvUsername.text = user.name
                if (user.biography.isNullOrBlank()) {
                    tvBio.text = "This user has no biography yet"
                } else {
                    tvBio.text = user.biography
                }
            },
            onFailure = {
                Log.e("User Error", "Error retrieving user $userId")
            }
        )
    }

    private fun loadUserFollowerData(userId: String) {

        FirebaseInteraction.getFollowersUsers(
            userId,
            onSuccess = { followers ->
                tvFollowersCount.text = followers.size.toString()
            }, onFailure = {
                Log.e("Followers Error", "Error getting $userId followers. ${it.message}")
            }
        )

        FirebaseInteraction.getFollowedUsers(
            userId,
            onSuccess = { followed ->
                tvFollowingCount.text = followed.size.toString()
            },
            onFailure = {
                Log.e("Followed Error", "Error getting $userId followed. ${it.message}")
            }
        )

    }


    private fun onFollowedMoviesFetched(movieIds: List<String>) {
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
        FirebaseInteraction.getFollowedUsers(
            onSuccess = { followedUsers ->
                Log.d("FollowingUsers", "${followedUsers.size}\n$followedUsers")
                if (followedUsers.contains(uid)) {
                    // logged user is follower of user in this page
                    FirebaseInteraction.removeTargetUserFromFollowing(
                        uid,
                        onSuccess = {
                            if (tvFollowersCount.text.isDigitsOnly()) {
                                val newFollowersValue = tvFollowersCount.text.toString().toInt() - 1
                                tvFollowersCount.text = newFollowersValue.toString()
                            }
                            bttFollowUnfollow.setBackgroundResource(R.drawable.add)
                        },
                        onFailure = {
                        Log.e("Error in follow action", "${it.message}")
                    })
                } else {
                    // logged user is not follower of user in this page
                    FirebaseInteraction.addTargetUserToFollowing(
                        uid,
                        onSuccess = {
                            if (tvFollowersCount.text.isDigitsOnly()) {
                                val newFollowersValue = tvFollowersCount.text.toString().toInt() + 1
                                tvFollowersCount.text = newFollowersValue.toString()
                            }
                            bttFollowUnfollow.setBackgroundResource(R.drawable.remove)
                        },
                        onFailure = {
                            Log.e("Error in follow action", "${it.message}")
                    })
                }
            },
            onFailure = {
                Log.e("FirebaseDB Error", "Error getting $uid followed users. ${it.message}")
            }
        )
    }

    private fun setInitialButtonState(userId: String) {
        FirebaseInteraction.checkFollowedExistance(userId) { exists ->
            if(exists) {
                bttFollowUnfollow.setBackgroundResource(R.drawable.remove)
            } else {
                bttFollowUnfollow.setBackgroundResource(R.drawable.add)
            }
        }
    }

}