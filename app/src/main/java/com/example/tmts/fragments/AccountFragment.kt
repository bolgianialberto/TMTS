package com.example.tmts.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
import com.example.tmts.FirebaseInteraction
import com.example.tmts.MediaRepository
import com.example.tmts.R
import com.example.tmts.activities.MainEmptyActivity
import com.example.tmts.activities.MovieDetailsActivity
import com.example.tmts.activities.SerieDetailsActivity
import com.example.tmts.adapters.AddToWatchlistAdapter
import com.example.tmts.adapters.MediaAdapter
import com.example.tmts.beans.Media
import com.google.firebase.auth.FirebaseAuth

class AccountFragment : Fragment() {
    private var mAuth = FirebaseAuth.getInstance()
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
    private lateinit var arrowWatchedMovies: ImageView
    private lateinit var arrowWatchedSeries: ImageView
    private lateinit var arrowWatchlist: ImageView
    private lateinit var tvWatchedMovies: TextView
    private lateinit var tvWatchedSeries: TextView
    private lateinit var tvWatchlists: TextView
    private lateinit var tvNoWatchedMovies: TextView
    private lateinit var tvNoWatchedSeries: TextView
    private lateinit var tvNoWatchlists: TextView
    private lateinit var llRvTvMovies: LinearLayout
    private lateinit var llRvTvSeries: LinearLayout
    private lateinit var llRvBtnWatchlist: LinearLayout


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
        arrowWatchedMovies = view.findViewById(R.id.arrow_watched_movies)
        arrowWatchedSeries = view.findViewById(R.id.arrow_watched_series)
        arrowWatchlist = view.findViewById(R.id.arrow_watchlist)
        tvWatchedMovies = view.findViewById(R.id.tv_watched_movies)
        tvWatchedSeries = view.findViewById(R.id.tv_watched_series)
        tvWatchlists = view.findViewById(R.id.tv_watchlists)
        tvNoWatchedMovies = view.findViewById(R.id.tv_no_watched_movies)
        tvNoWatchedSeries = view.findViewById(R.id.tv_no_watched_series)
        tvNoWatchlists = view.findViewById(R.id.tv_no_watchlists)
        llRvTvMovies = view.findViewById(R.id.ll_rv_tv_movies)
        llRvTvSeries = view.findViewById(R.id.ll_rv_tv_series)
        llRvBtnWatchlist = view.findViewById(R.id.ll_rv_btn_watchlist)

        // Setup adapters for Recycle Views
        watchedMoviesAdapter = MediaAdapter(requireContext(), emptyList(), 66, 100) { movie ->
            val intent = Intent(requireContext(), MovieDetailsActivity::class.java)
            intent.putExtra("movieId", movie.id)
            startActivity(intent)
        }
        watchedSeriesAdapter = MediaAdapter(requireContext(), emptyList(), 66, 100) {serie ->
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
            if (movieIds.isNotEmpty()){
                rvWatchedMovie.visibility = View.VISIBLE
                tvNoWatchedMovies.visibility = View.GONE
                onWatchedMoviesFetched(movieIds)
            }
        }

        FirebaseInteraction.getWatchedSeries { serieIds ->
            if (serieIds.isNotEmpty()){
                rvWatchedSerie.visibility = View.VISIBLE
                tvNoWatchedSeries.visibility = View.GONE
                onWatchedSeriesFetched(serieIds)
            }
        }

        FirebaseInteraction.fetchWatchlistsWithDetails(
            onSuccess = {watchlists ->
                if (watchlists.isNotEmpty()){
                    rvWatchlist.visibility = View.VISIBLE
                    tvNoWatchlists.visibility = View.GONE
                    watchlistsAdapter!!.updateMedia(watchlists)
                }
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
        loadUserImage()

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

        // Gestori di clic aggiornati
        llWatchedMovies.setOnClickListener {
            if (llRvTvMovies.visibility == View.GONE) {
                llRvTvMovies.visibility = View.VISIBLE
                arrowWatchedMovies.setImageResource(R.drawable.arrow_up)
            } else {
                llRvTvMovies.visibility = View.GONE
                arrowWatchedMovies.setImageResource(R.drawable.arrow_down)
            }
        }

        llWatchedSeries.setOnClickListener {
            if (llRvTvSeries.visibility == View.GONE) {
                llRvTvSeries.visibility = View.VISIBLE
                arrowWatchedSeries.setImageResource(R.drawable.arrow_up)
            } else {
                llRvTvSeries.visibility = View.GONE
                arrowWatchedSeries.setImageResource(R.drawable.arrow_down)
            }
        }

        llWatchlists.setOnClickListener {
            if (llRvBtnWatchlist.visibility == View.GONE) {
                llRvBtnWatchlist.visibility = View.VISIBLE
                arrowWatchlist.setImageResource(R.drawable.arrow_up)
            } else {
                llRvBtnWatchlist.visibility = View.GONE
                arrowWatchlist.setImageResource(R.drawable.arrow_down)
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

        editTextBio.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.length >= 97) { // TODO: not hardcoded pls
                    textViewError.text = "Character limit reached!"
                    textViewError.visibility = View.VISIBLE
                } else {
                    textViewError.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        editTextBio.setText(tvBio.text.toString())

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit your profile bio")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
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
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()

        // Give focus to the EditText and show the keyboard after the dialog has been shown
        editTextBio.requestFocus()
        Handler(Looper.getMainLooper()).post {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editTextBio, InputMethodManager.SHOW_IMPLICIT)
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

    private fun loadUserImage() {
        FirebaseInteraction.getUserRefInStorage(
            onSuccess = {userImageRef ->
                userImageRef.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(requireContext())
                        .load(uri)
                        .into(ivAccountIcon)
                }.addOnFailureListener { exception ->
                    Log.e("FirebaseStorage", "Errore durante il download dell'immagine", exception)
                }
            },
            onError = {message ->
                Log.d("AccountFragment", message)
            }
        )
    }
}