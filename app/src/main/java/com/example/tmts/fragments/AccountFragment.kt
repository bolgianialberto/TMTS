package com.example.tmts.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.tmts.R
import com.example.tmts.activities.MainEmptyActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class AccountFragment : Fragment() {
    private var mDbRef = FirebaseDatabase.getInstance().getReference()
    private var mAuth = FirebaseAuth.getInstance()
    private var mStorage = FirebaseStorage.getInstance().getReference()
    private lateinit var ivAccountIcon: ImageView

    val currentUser = mAuth.currentUser!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)
        val userRef = mDbRef.child("users").child(currentUser.uid)

        val tvUsername: TextView = view.findViewById(R.id.tv_account_username)

        userRef.child("name").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.getValue(String::class.java)
                tvUsername.text = "$name"
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        val userImageRef = mStorage.child("users").child(currentUser.uid).child("profileImage")

        //val logoutButton: Button = view.findViewById(R.id.logout_button2)
        val ibDropDown: ImageButton = view.findViewById(R.id.ib_dropdown)

        //Quando si entra in app direttamente (senza login): name = ""
        // mentre con login, name = "null"

        //greetingTextView.text = "Ciao, ${currentUser?.displayName}"


//        logoutButton.setOnClickListener {
//            performLogout()
//        }

        //TODO: Fare in modo che qui ivAccountIcon venga impostata a quello che è stato caricato su Firebase, se presente

        ivAccountIcon = view.findViewById(R.id.account_icon)
        loadUserImage(userImageRef, ivAccountIcon)

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

    private fun selectImageFromGallery() {
        selectImageFromGalleryResult.launch("image/*")
    }

    private val selectImageFromGalleryResult = registerForActivityResult(ActivityResultContracts.GetContent()) {

            uri: Uri? -> uri?.let {
            //TODO: Se faccio upload di 1 immagine, poi switch fragment, poi ritorno a fare upload immagine, mi porta a un crash

            val userImageRef = mStorage.child("users").child(currentUser.uid).child("profileImage")

            userImageRef.putFile(uri).addOnSuccessListener {
                Toast.makeText(requireContext(), "Uploaded successfully!", Toast.LENGTH_SHORT).show()
                ivAccountIcon.setImageURI(uri)
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserImage(userImageRef: StorageReference, ivAccountIcon: ImageView) {
        userImageRef.downloadUrl.addOnSuccessListener {
            // File exists, load with Glide
            Glide.with(requireContext())
                .load(userImageRef)
                .placeholder(R.drawable.movie)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(ivAccountIcon)
        }.addOnFailureListener{
            // File doesn't exist, load default image
            ivAccountIcon.setImageResource(R.drawable.movie) // TODO: change default image
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        selectImageFromGalleryResult.unregister()
    }

}