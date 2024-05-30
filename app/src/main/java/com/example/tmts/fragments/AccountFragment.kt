package com.example.tmts.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.tmts.R
import com.example.tmts.activities.MainEmptyActivity
import com.google.firebase.auth.FirebaseAuth

class AccountFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser

        val greetingTextView: TextView = view.findViewById(R.id.account_fragment_saluto)
        val logoutButton: Button = view.findViewById(R.id.logout_button)

        greetingTextView.text = "Ciao, ${currentUser?.displayName}"

        logoutButton.setOnClickListener {
            performLogout()
        }
        return view
    }

    private fun performLogout() {
        mAuth.signOut()
        val intent = Intent(requireContext(), MainEmptyActivity::class.java)
        startActivity(intent)
    }
}