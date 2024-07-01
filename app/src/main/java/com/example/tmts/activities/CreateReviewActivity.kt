package com.example.tmts.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.tmts.FirebaseInteraction
import com.example.tmts.MediaRepository
import com.example.tmts.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CreateReviewActivity : AppCompatActivity() {
    private lateinit var btnClose: Button
    private lateinit var btnPost: Button
    private lateinit var etReview: EditText
    private lateinit var btnAddPhoto: FloatingActionButton
    private lateinit var ivReviewPhoto: ImageView
    private var selectedImageUri: Uri? = null
    private lateinit var mediaType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_review)

        val movieId = intent.getIntExtra("movieId", -1)
        mediaType = intent.getStringExtra("mediaType") ?: "movie"

        btnClose = findViewById(R.id.btn_close)
        btnPost = findViewById(R.id.btn_post)
        etReview = findViewById(R.id.et_review)
        btnAddPhoto = findViewById(R.id.fab_add_photo)
        ivReviewPhoto = findViewById(R.id.iv_photo_review)

        MediaRepository.getMediaDetails(
            movieId,
            mediaType,
            onSuccess = { media ->
                etReview.hint = "Write your review about ${media.title}"
            },
            onError = {
                Log.e("CreateReviewActivity", "Failed to fetch movie details")
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
                FirebaseInteraction.addReviewToMedia(
                    movieId.toString(),
                    mediaType,
                    review,
                    selectedImageUri,
                    onSuccess = {
                        finish()
                    },
                    onFailure = { exception ->
                        Log.e("CreateReviewActivity", "Failed to add review", exception)
                        Toast.makeText(this, "Failed to add review", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        btnAddPhoto.setOnClickListener {
            selectImageFromGalleryResult.launch("image/*")
        }
    }

    private val selectImageFromGalleryResult = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let{uri ->
            selectedImageUri = uri
            ivReviewPhoto.setImageURI(selectedImageUri)
            ivReviewPhoto.visibility = ImageView.VISIBLE
        }
    }
}
