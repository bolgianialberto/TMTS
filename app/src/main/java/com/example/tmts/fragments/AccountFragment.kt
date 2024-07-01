package com.example.tmts.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.tmts.FirebaseInteraction
import com.example.tmts.MediaRepository
import com.example.tmts.R
import com.example.tmts.activities.CreateReviewActivity
import com.example.tmts.activities.MainEmptyActivity
import com.example.tmts.activities.MovieDetailsActivity
import com.example.tmts.activities.SerieDetailsActivity
import com.example.tmts.adapters.AddToWatchlistAdapter
import com.example.tmts.adapters.MediaAdapter
import com.example.tmts.beans.Media
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class AccountFragment : Fragment() {
    private var mAuth = FirebaseAuth.getInstance()
    private var mStorage = FirebaseStorage.getInstance().getReference()
    private lateinit var ivAccountIcon: ImageView
    private lateinit var tvBio: TextView
    private lateinit var tvUsername: TextView
    private lateinit var ibDropDown: ImageButton
    private lateinit var tvFollowerCount: TextView
    private lateinit var tvFollowingCount: TextView
    private lateinit var watchedMoviesAdapter: MediaAdapter
    private lateinit var watchedSeriesAdapter: MediaAdapter
    private var watchlistsAdapter: AddToWatchlistAdapter? = null
    private lateinit var llWatchedMovies: LinearLayout
    private lateinit var llWatchedSeries: LinearLayout
    private lateinit var llWatchlists: LinearLayout

    val currentUser = mAuth.currentUser!!

    private lateinit var selectImageFromGalleryResult: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        selectImageFromGalleryResult = registerForActivityResult(ActivityResultContracts.GetContent()) {uri ->
            uri?.let{
                ivAccountIcon.setImageURI(uri)
                FirebaseInteraction.updateUserProfileImage(
                    uri,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Profile image correctly updated!", Toast.LENGTH_SHORT).show()
                    },
                    onError = {
                        Toast.makeText(requireContext(), "Something wrong happened", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch user's Firebase image reference
        val userImageRef = mStorage.child("users").child(currentUser.uid).child("profileImage")

        // Initialize views
        tvUsername = view.findViewById(R.id.tv_account_username)
        tvBio = view.findViewById(R.id.tv_bio)
        ibDropDown = view.findViewById(R.id.ib_dropdown)
        ivAccountIcon = view.findViewById(R.id.account_icon)
        tvFollowerCount = view.findViewById(R.id.tv_follower_count)
        tvFollowingCount = view.findViewById(R.id.tv_following_count)
        llWatchedMovies = view.findViewById(R.id.ll_watched_movies)
        llWatchedSeries = view.findViewById(R.id.ll_watched_series)
        llWatchlists = view.findViewById(R.id.ll_watchlists)

        // Setup adapters for Recycle Views
        watchedMoviesAdapter = MediaAdapter(requireContext(), emptyList()) { movie ->
            val intent = Intent(requireContext(), MovieDetailsActivity::class.java)
            intent.putExtra("movieId", movie.id)
            startActivity(intent)
        }
        watchedSeriesAdapter = MediaAdapter(requireContext(), emptyList()) {serie ->
            val intent = Intent(requireContext(), SerieDetailsActivity::class.java)
            intent.putExtra("serieId", serie.id)
            startActivity(intent)
        }

        watchlistsAdapter = AddToWatchlistAdapter(requireContext(), emptyList()) {}

        val rvWatchedMovie: RecyclerView = view.findViewById(R.id.rv_watched_movies)
        rvWatchedMovie.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvWatchedMovie.adapter = watchedMoviesAdapter

        val rvWatchedSerie: RecyclerView = view.findViewById(R.id.rv_watched_series)
        rvWatchedSerie.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvWatchedSerie.adapter = watchedSeriesAdapter

        val rvWatchlist: RecyclerView = view.findViewById(R.id.rv_watchlist_account)
        rvWatchlist.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rvWatchlist.adapter = watchlistsAdapter

        FirebaseInteraction.getWatchedMovies { movieIds ->
            onWatchedMoviesFetched(movieIds)
        }

        FirebaseInteraction.getWatchedSeries { serieIds ->
            onWatchedSeriesFetched(serieIds)
        }

        FirebaseInteraction.fetchWatchlistsWithDetails(
            onSuccess = {watchlists ->
                watchlistsAdapter!!.updateMedia(watchlists)
        },
            onError = { errorMessage ->
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        )

        // Fetch user's display name and change the view accordingly
        FirebaseInteraction.getUsername(currentUser.uid,
            onSuccess = {username ->
                tvUsername.text = username
            },
            onFailure = {errorMessage ->
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            })

        // Load previously uploaded image by user, if it exists, otherwise load default image
        loadUserImage(userImageRef, ivAccountIcon)

        // Load previously written user bio, if it exists, otherwise set default bio
        FirebaseInteraction.getUserBio(
            onSuccess = { bio ->
                tvBio.text = bio
            },
            onFailure = {
                tvBio.text = "Write something about you..."
            }
        )

        // Load user's follower data from Firebase
        loadUserFollowerData()

        // Set view or buttons listeners
        ivAccountIcon.setOnClickListener{
            val popup = PopupMenu(requireContext(), it)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.context_menu, popup.menu)
            popup.show()
            popup.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.edit_profile_icon -> selectImageFromGallery()
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
                    R.id.edit_profile -> showEditBioDialog()
                    R.id.logout_button -> performLogout()
                }
                true
            }
        }

        llWatchedMovies.setOnClickListener{
            if (rvWatchedMovie.visibility == View.GONE) {
                rvWatchedMovie.visibility = View.VISIBLE
            } else {
                rvWatchedMovie.visibility = View.GONE
            }
        }

        llWatchedSeries.setOnClickListener{
            if (rvWatchedSerie.visibility == View.GONE) {
                rvWatchedSerie.visibility = View.VISIBLE
            } else {
                rvWatchedSerie.visibility = View.GONE
            }
        }

        llWatchlists.setOnClickListener{
            if (rvWatchlist.visibility == View.GONE) {
                rvWatchlist.visibility = View.VISIBLE
            } else {
                rvWatchlist.visibility = View.GONE
            }
        }
    }

    private fun onWatchedMoviesFetched(movieIds: List<String>){
        val movies = mutableListOf<Media>()
        var completedRequests = 0
        val totalRequests = movieIds.size

        movieIds.forEach {movieId ->
            MediaRepository.getMovieDetails(movieId.toInt(),
                onSuccess = {movie ->
                    val movieMedia = Media(movie.id, movie.title, "", movie.posterPath)
                    movies.add(movieMedia)
                    completedRequests++

                    if (completedRequests == totalRequests) {
                        watchedMoviesAdapter.updateMedia(movies)
                    }
                },
                onError = {})
        }
    }

    private fun onWatchedSeriesFetched(serieIds: List<String>){
        val series = mutableListOf<Media>()
        var completedRequests = 0
        val totalRequests = serieIds.size

        serieIds.forEach {serieId ->
            MediaRepository.getSerieDetails(serieId.toInt(),
                onSuccess = {serie ->
                    val serieMedia = Media(serie.id, serie.title, "", serie.posterPath)
                    series.add(serieMedia)
                    completedRequests++

                    if (completedRequests == totalRequests) {
                        watchedSeriesAdapter.updateMedia(series)
                    }
                },
                onError = {})
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

    private fun showEditBioDialog() {
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

                FirebaseInteraction.saveBioToFirebase(newBio,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Bio updated successfully!", Toast.LENGTH_SHORT).show()
                        tvBio.setText(newBio)
                },
                    onFailure = {
                        Toast.makeText(requireContext(), "Failed to update bio!", Toast.LENGTH_SHORT).show()
                    }
                )
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") {dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
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

    /*
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

     */

    private fun loadUserImage(userImageRef: StorageReference, ivAccountIcon: ImageView) {
        userImageRef.downloadUrl.addOnSuccessListener {uri ->
            // File exists, load with Glide
            Glide.with(requireContext())
                .load(uri)
                .placeholder(R.drawable.movie)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(ivAccountIcon)
        }.addOnFailureListener{
            // File doesn't exist, load default image
            ivAccountIcon.setImageResource(R.drawable.movie) // TODO: change default image
        }
    }
}