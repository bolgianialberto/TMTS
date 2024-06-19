package com.example.tmts.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tmts.FirebaseInteraction
import com.example.tmts.MediaRepository
import com.example.tmts.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CreateReviewActivity : AppCompatActivity() {
    private lateinit var btnClose: Button
    private lateinit var btnPost: Button
    private lateinit var etReview: EditText
    private lateinit var btnAddPhoto: FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_review)

        val movieId = intent.getIntExtra("movieId", -1)

        btnClose = findViewById(R.id.btn_close)
        btnPost = findViewById(R.id.btn_post)
        etReview = findViewById(R.id.et_review)
        btnAddPhoto = findViewById(R.id.fab_add_photo)

        MediaRepository.getMovieDetails(
            movieId,
            onSuccess = {movie ->
                etReview.hint = "Write your review about ${movie.title}"
            },
            onError = {
                Log.e("CreateReviewActivity", "Something went wrong")
            }
        )

        btnClose.setOnClickListener {
            finish()
        }

        btnPost.setOnClickListener {
            val review = etReview.text.toString()
            if (review.isEmpty()) {
                Toast.makeText(this, "Please write a review before posting.", Toast.LENGTH_SHORT).show()
            } else {
                FirebaseInteraction.addReviewToMovie(
                    movieId.toString(),
                    review,
                    onSuccess = {
                        finish()
                    },
                    onFailure = { exception ->
                        exception.printStackTrace()
                    }
                )

            }
        }
    }
}