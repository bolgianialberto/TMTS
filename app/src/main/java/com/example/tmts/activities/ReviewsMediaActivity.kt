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
import com.example.tmts.beans.MediaDetails
import com.example.tmts.beans.MovieDetails
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ReviewsMediaActivity : AppCompatActivity() {
    private lateinit var tvMediaName: TextView
    private lateinit var btnAddComment: FloatingActionButton
    private lateinit var btnArrowBack: Button
    private lateinit var rvReview: RecyclerView
    private lateinit var reviewsAdapter: ReviewAdapter
    private lateinit var mediaType: String
    private var mediaId: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_reviews)

        val intent = intent
        mediaId = intent.getIntExtra("mediaId", -1)
        mediaType = intent.getStringExtra("mediaType") ?: "movie"

        tvMediaName = findViewById(R.id.tv_comment_activity_media_title)
        btnAddComment = findViewById(R.id.fab_add_comment)
        btnArrowBack = findViewById(R.id.iv_arrow_back_comment_activity)
        rvReview = findViewById(R.id.rv_reviews)

        reviewsAdapter = ReviewAdapter(this, emptyList()) // Pass an empty list initially
        rvReview.layoutManager = LinearLayoutManager(this)
        rvReview.adapter = reviewsAdapter

        loadData()
    }

    private fun loadData(){
        MediaRepository.getMediaDetails(
            mediaId,
            mediaType,
            onSuccess = ::updateUI,
            onError = ::onError
        )
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun updateUI(media: MediaDetails){
        tvMediaName.text = media.title

        btnArrowBack.setOnClickListener{
            onBackPressed()
        }

        FirebaseInteraction.getReviewsForMedia(
            media.id.toString(),
            mediaType,
            onSuccess = { reviews ->
                reviewsAdapter.updateMedia(reviews)
            },
            onError = { error ->
                Log.e("ReviewsMovieActivity", "Error loading reviews: $error")
            }
        )

        btnAddComment.setOnClickListener{
            val intent = Intent(this, CreateReviewActivity::class.java)
            intent.putExtra("movieId", media.id)
            intent.putExtra("mediaType", if (media is MovieDetails) "movie" else "series")
            startActivity(intent)
        }
    }

    private fun onError(){
        Log.e("CommentsMovieActivity", "Something went wrong")
    }
}