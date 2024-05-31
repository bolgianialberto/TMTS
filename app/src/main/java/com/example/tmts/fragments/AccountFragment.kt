package com.example.tmts.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
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

        //val greetingTextView: TextView = view.findViewById(R.id.account_fragment_saluto)
        val tvUsername: TextView = view.findViewById(R.id.tv_account_username)
        //val logoutButton: Button = view.findViewById(R.id.logout_button2)
        val ibDropDown: ImageButton = view.findViewById(R.id.ib_dropdown)

        //Quando si entra in app direttamente (senza login): name = ""
        // mentre con login, name = "null"

        //greetingTextView.text = "Ciao, ${currentUser?.displayName}"
        //tvUsername.text = "${currentUser?.displayName}"

//        logoutButton.setOnClickListener {
//            performLogout()
//        }

        ibDropDown.setOnClickListener {
            val popup = PopupMenu(requireContext(), it)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.dropdown_menu, popup.menu)
            popup.show()
            popup.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.edit_profile -> editProfile()
                    R.id.logout_button -> performLogout()
                }
                true
            }
        }

        return view
    }

    private fun editProfile(): Boolean {
        //TODO: to be implemented
        return true
    }

    private fun performLogout(): Boolean {
        mAuth.signOut()
        val intent = Intent(requireContext(), MainEmptyActivity::class.java)
        startActivity(intent)
        return true
    }


}