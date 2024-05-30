package com.example.tmts.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tmts.MainActivity
import com.google.firebase.auth.FirebaseAuth

class MainEmptyActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()

        val currentUser = mAuth.currentUser
        val activityIntent: Intent

        if (currentUser != null) {
            activityIntent = Intent(this, MainActivity::class.java)
        } else {
            activityIntent = Intent(this, LoginActivity::class.java)
        }

        startActivity(activityIntent)
        finish()
    }
}