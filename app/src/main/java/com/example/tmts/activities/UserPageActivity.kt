package com.example.tmts.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tmts.FirebaseInteraction
import com.example.tmts.R

class UserPageActivity: AppCompatActivity() {
    private lateinit var tvUsername: TextView
    private lateinit var tvBio: TextView
    private lateinit var tvFollowerCount: TextView
    private lateinit var tvFollowingCount: TextView
    private lateinit var ivAccountIcon: ImageView
    private lateinit var bFollowUnfollow: Button

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


        FirebaseInteraction.getUsername(
            userId = uid!!,
            onSuccess = { username ->
                tvUsername.text = username
            },
            onFailure = { errorMessage ->
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            })

        FirebaseInteraction.getUserBio(
            onSuccess = { bio ->
                tvBio.text = bio
            },
            onFailure = {

            }
        )

        loadUserFollowerData()

        bFollowUnfollow.setOnClickListener{
            followUnfollowUser(uid)
        }

    }

    private fun followUnfollowUser(uid: String) {
        FirebaseInteraction.getFollowedUsers { followedUsers ->
            if (followedUsers.contains(uid)) {
                FirebaseInteraction.removeSelfFromUserFollowers(uid) {
                    FirebaseInteraction.removeTargetUserFromFollowing(uid) {
                        bFollowUnfollow.setBackgroundResource(R.drawable.add)
                    }
                }
            } else {
                FirebaseInteraction.addTargetUserToFollowing(uid) {
                    FirebaseInteraction.addSelfToFollowed(uid) {
                        bFollowUnfollow.setBackgroundResource(R.drawable.remove)
                    }
                }
            }
        }
    }

    private fun loadUserFollowerData() {
        // Set number of users following me from Firebase
        FirebaseInteraction.getFollowersUsers { followers ->
            tvFollowerCount.text = followers.size.toString()
        }

        // Check number of users I follow from Firebase
        FirebaseInteraction.getFollowedUsers { followed ->
            tvFollowingCount.text = followed.size.toString()
        }
    }



    private fun onError(){
        Log.e("CommentsMovieActivity", "Something went wrong")
    }
}