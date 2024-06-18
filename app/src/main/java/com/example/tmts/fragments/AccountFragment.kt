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
import com.example.tmts.beans.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AccountFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var userRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        mDbRef = FirebaseDatabase.getInstance().getReference()
        userRef = mDbRef.child("users").child((currentUser?.uid).toString())

        val greetingTextView: TextView = view.findViewById(R.id.account_fragment_saluto)
        val logoutButton: Button = view.findViewById(R.id.logout_button)

        userRef.child("name").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nome = snapshot.getValue(String::class.java)
                greetingTextView.text = "Ciao, $nome"
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

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