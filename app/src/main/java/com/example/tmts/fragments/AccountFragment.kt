package com.example.tmts.fragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.tmts.FirebaseInteraction
import com.example.tmts.R
import com.example.tmts.activities.MainEmptyActivity
import com.example.tmts.adapters.ExpandableListAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class AccountFragment : Fragment() {//TODO Implement usage of FireBaseInteraction.kt
    private var mDbRef = FirebaseDatabase.getInstance().getReference()
    private var mAuth = FirebaseAuth.getInstance()
    private var mStorage = FirebaseStorage.getInstance().getReference()
    private lateinit var ivAccountIcon: ImageView
    private lateinit var tvBio: TextView
    private lateinit var tvUsername: TextView
    private lateinit var ibDropDown: ImageButton
    private lateinit var tvFollowerCount: TextView
    private lateinit var tvFollowingCount: TextView
    private lateinit var elvList: ExpandableListView

    val currentUser = mAuth.currentUser!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        // Fetch user id Firebase reference
        val userIdRef = mDbRef.child("users").child(currentUser.uid)

        // Fetch user id Firebase reference
        val userBioRef = mDbRef.child("users").child(currentUser.uid).child("bio")

        // Fetch user's Firebase image reference
        val userImageRef = mStorage.child("users").child(currentUser.uid).child("profileImage")

        // Initialize views
        tvUsername = view.findViewById(R.id.tv_account_username)
        tvBio = view.findViewById(R.id.tv_bio)
        ibDropDown = view.findViewById(R.id.ib_dropdown)
        ivAccountIcon = view.findViewById(R.id.account_icon)
        tvFollowerCount = view.findViewById(R.id.tv_follower_count)
        tvFollowingCount = view.findViewById(R.id.tv_following_count)
        elvList = view.findViewById(R.id.expandable_list)

        // Fetch user's display name and change the view accordingly
        userIdRef.child("name").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.getValue(String::class.java)
                tvUsername.text = "$name"
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        // Load previously uploaded image by user, if it exists, otherwise load default image
        loadUserImage(userImageRef, ivAccountIcon)

        // Load previously written user bio, if it exists, otherwise set default bio
        loadUserBio(userBioRef, tvBio)

        // Load user's follower data from Firebase
        loadUserFollowerData(userIdRef, tvFollowerCount, tvFollowingCount)

        // Setup expandable list view
        val listTitle = listOf("Watchlist", "Serie TV", "Film Visti")
        val listDetail = hashMapOf<String, List<String>>(
            "Watchlist" to listOf("Item 1", "Item 2"),
            "Serie TV" to listOf("Serie 1", "Serie 2"),  // Sarà popolato dall'adapter
            "Film Visti" to listOf()
        )

        val expandableListAdapter = ExpandableListAdapter(requireContext(), listTitle, listDetail)
        elvList.setAdapter(expandableListAdapter)

        // Set view or buttons listeners
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
                    R.id.edit_profile -> editProfile(userBioRef)
                    R.id.logout_button -> performLogout()
                }
                true
            }
        }

        return view
    }

    private fun loadUserFollowerData(
        userIdRef: DatabaseReference,
        tvFollowerCount: TextView,
        tvFollowingCount: TextView
    ) {
        // Set number of users following me from Firebase
        FirebaseInteraction.getFollowersUsers { followers ->
            tvFollowerCount.text = followers.size.toString()
        }

        // Check number of users I follow from Firebase
        FirebaseInteraction.getFollowedUsers { followed ->
            tvFollowingCount.text = followed.size.toString()
        }

    }

    private fun loadUserBio(userBioRef: DatabaseReference, tvBio: TextView) {
        userBioRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bio = snapshot.getValue(String::class.java)
                tvBio.text = (bio ?: "Write something about you...")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun editProfileIcon(): Boolean {
        selectImageFromGallery()
        return true
    }

    private fun editProfile(userBioRef: DatabaseReference): Boolean {
        showEditBioDialog(userBioRef)
        return true
    }

    private fun showEditBioDialog(userBioRef: DatabaseReference) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_bio, null)
        val editTextBio = dialogView.findViewById<EditText>(R.id.et_bio)
        val textViewError = dialogView.findViewById<TextView>(R.id.tv_bio_error)

        editTextBio.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.length >= 97) {// TODO: not hardcoded pls
                    textViewError.text = "Character limit reached!"
                    textViewError.visibility = View.VISIBLE
                } else {
                    textViewError.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        editTextBio.setText(tvBio.text.toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Edit your profile bio")
            .setView(dialogView)
            .setPositiveButton("Save") {dialog, _ ->
                val newBio = editTextBio.text.toString()
                tvBio.setText(newBio)
                saveBioToFirebase(userBioRef, newBio)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") {dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun saveBioToFirebase(userBioRef: DatabaseReference, newBio: String) {
        userBioRef.setValue(newBio)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Bio updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update bio!", Toast.LENGTH_SHORT).show()
            }
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