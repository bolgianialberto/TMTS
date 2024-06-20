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

    companion object {
        private const val REQUEST_PICK_IMAGE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_review)

        val movieId = intent.getIntExtra("movieId", -1)

        btnClose = findViewById(R.id.btn_close)
        btnPost = findViewById(R.id.btn_post)
        etReview = findViewById(R.id.et_review)
        btnAddPhoto = findViewById(R.id.fab_add_photo)
        ivReviewPhoto = findViewById(R.id.iv_photo_review)

        MediaRepository.getMovieDetails(
            movieId,
            onSuccess = { movie ->
                etReview.hint = "Write your review about ${movie.title}"
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
                FirebaseInteraction.addReviewToMovie(
                    movieId.toString(),
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
            // Intent per aprire la galleria e selezionare un'immagine
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_PICK_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            if (selectedImageUri != null) {
                ivReviewPhoto.setImageURI(selectedImageUri)
                ivReviewPhoto.visibility = ImageView.VISIBLE
            }
        }
    }
}
