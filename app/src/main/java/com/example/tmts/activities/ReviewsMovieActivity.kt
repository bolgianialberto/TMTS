package com.example.tmts.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmts.FirebaseInteraction
import com.example.tmts.MediaRepository
import com.example.tmts.R
import com.example.tmts.adapters.ReviewAdapter
import com.example.tmts.beans.MovieDetails
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ReviewsMovieActivity : AppCompatActivity() {
    private lateinit var tvMovieName: TextView
    private lateinit var btnAddComment: FloatingActionButton
    private lateinit var btnArrowBack: Button
    private lateinit var rvReview: RecyclerView
    private lateinit var reviewsAdapter: ReviewAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_reviews)

        val intent = intent
        val movieId = intent.getIntExtra("movieId", -1)

        tvMovieName = findViewById(R.id.tv_comment_activity_movie_title)
        btnAddComment = findViewById(R.id.fab_add_comment)
        btnArrowBack = findViewById(R.id.iv_arrow_back_comment_activity)
        rvReview = findViewById(R.id.rv_reviews)

        reviewsAdapter = ReviewAdapter(this, emptyList()) // Pass an empty list initially
        rvReview.layoutManager = LinearLayoutManager(this)
        rvReview.adapter = reviewsAdapter

        MediaRepository.getMovieDetails(
            movieId,
            onSuccess = ::updateUI,
            onError = ::onError
        )
    }

    private fun updateUI(movie: MovieDetails){
        tvMovieName.text = movie.title

        btnArrowBack.setOnClickListener{
            onBackPressed()
        }

        FirebaseInteraction.getReviewsForMovie(
            movie.id.toString(),
            onSuccess = { reviews ->
                reviewsAdapter.updateMedia(reviews)
            },
            onError = { error ->
                Log.e("ReviewsMovieActivity", "Error loading reviews: $error")
            }
        )

        btnAddComment.setOnClickListener{
            val intent = Intent(this, CreateReviewActivity::class.java)
            intent.putExtra("movieId", movie.id)
            startActivity(intent)
        }
    }

    private fun onError(){
        Log.e("CommentsMovieActivity", "Something went wrong")
    }
}