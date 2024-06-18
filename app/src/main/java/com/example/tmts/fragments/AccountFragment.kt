package com.example.tmts.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.tmts.R
import com.example.tmts.activities.MainEmptyActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AccountFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var ivAccountIcon: ImageView
    lateinit var imagesRef: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        var fStorage = FirebaseStorage.getInstance()

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

        //TODO: Fare in modo che qui ivAccountIcon venga impostata a quello che è stato caricato su Firebase, se presente

        ivAccountIcon = view.findViewById(R.id.account_icon)

        ivAccountIcon.setOnClickListener{
            val popup = PopupMenu(requireContext(), it)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.context_menu, popup.menu)
            popup.show()
            popup.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.edit_profile_icon -> editProfileIcon()
                }
                true
            }
        }

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

    private fun editProfileIcon(): Boolean {
        selectImageFromGallery()
        return true
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

    private fun selectImageFromGallery() = selectImageFromGalleryResult.launch("image/*")

    private val selectImageFromGalleryResult = registerForActivityResult(ActivityResultContracts.GetContent()) {

            uri: Uri? -> uri?.let {
        val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        var now = Date()
        val filename = formatter.format(now)

        //TODO: Scommentare per garantire upload su firebase. Cambiare immagine SOLO se upload avviene con successo
        // Perchè se avviene fallimento, l'immagine nuova sarà caricata solo localmente e quindi:
        // 1. Al prossimo avvio forse non l'avrà
        // 2. Non posso rimettere l'immagine precedente perchè magari in locale non la ho più

        val uid = mAuth.uid.toString()

        imagesRef = FirebaseStorage.getInstance().getReference("users/$uid/profilePicture/$filename")
        imagesRef.putFile(uri).addOnSuccessListener {
            Toast.makeText(requireContext(), "Uploaded successfully!", Toast.LENGTH_SHORT).show()
            ivAccountIcon.setImageURI(uri)
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show()
        }
    }

    }

}