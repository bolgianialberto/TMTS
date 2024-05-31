package com.example.tmts.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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

        //TODO: Forse qui bisogna passare anche il current user,
        // perchè sembra che non ritenga i dati dell'user

        startActivity(activityIntent)
        finish()
    }
}