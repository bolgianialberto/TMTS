package com.example.tmts.adapters

import android.content.Context
import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmts.FirebaseInteraction
import com.example.tmts.R
import com.example.tmts.beans.Review
import com.google.firebase.storage.FirebaseStorage

class ReviewAdapter (
    private val context: Context,
    private var mediaItems: List<Review>
) : RecyclerView.Adapter<ReviewAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.review_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaItem = mediaItems[position]
        holder.bind(mediaItem)
    }

    override fun getItemCount(): Int {
        return mediaItems.size
    }

    fun updateMedia(mediaItems: List<Review>){
        this.mediaItems = mediaItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUserName: TextView = itemView.findViewById(R.id.tv_comment_activity_user_name)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_comment_activity_date)
        private val tvComment: TextView = itemView.findViewById(R.id.tv_comment_activity_user_comments)
        private val ivPhoto: ImageView = itemView.findViewById(R.id.iv_comment_activity_user_photo)
        private val ivUserPhoto: ImageView = itemView.findViewById(R.id.iv_comment_activity_user_profile)
        fun bind(review: Review) {
            Log.d("review", "${review}")
            tvDate.text = review.date

            tvComment.text = review.comment

            review.idUser?.let {
                FirebaseInteraction.getUsername(
                    it,
                    onSuccess = { username ->
                        tvUserName.text = username
                        Log.d("FirebaseInterraction", "Username: $username")
                    },
                    onFailure = { error ->
                        // Gestisci l'errore qui
                        Log.e("FirebaseInterraction", "Failed to get username: $error")
                    }
                )
            }

            FirebaseInteraction.getReviewRefInStorage(
                review,
                onSuccess = {reviewImageRef ->
                    reviewImageRef.downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(context)
                            .load(uri)
                            .into(ivPhoto)
                        ivPhoto.visibility = View.VISIBLE
                    }.addOnFailureListener { exception ->
                        Log.e("FirebaseStorage", "Errore durante il download dell'immagine", exception)
                        ivPhoto.visibility = View.GONE
                    }
                },
                onError = {message ->
                    Log.d("ReviewAdapter", message)
                }
            )

            FirebaseInteraction.getUserRefInStorage(
                review.idUser,
                onSuccess = {userImageRef ->
                    userImageRef.downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(context)
                            .load(uri)
                            .into(ivUserPhoto)
                    }.addOnFailureListener { exception ->
                        Log.e("FirebaseStorage", "Errore durante il download dell'immagine", exception)
                    }
                },
                onError = {message ->
                    Log.d("ReviewAdapter", message)
                }
            )
        }
    }
}