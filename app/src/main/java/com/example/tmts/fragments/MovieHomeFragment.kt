package com.example.tmts.fragments

import android.os.Bundle
import android.text.TextUtils.replace
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmts.FirebaseInteraction
import com.example.tmts.MediaRepository
import com.example.tmts.OnCheckButtonClickListener
import com.example.tmts.R
import com.example.tmts.activities.MainActivity
import com.example.tmts.adapters.HomeMovieAdapter
import com.example.tmts.beans.MovieDetails
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MovieHomeFragment : Fragment(), OnCheckButtonClickListener {
    private lateinit var homeMovieAdapter: HomeMovieAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var llNoMovies: LinearLayout
    private lateinit var btnSearch: Button
    private lateinit var bottomNavigationView: BottomNavigationView

    val movieDetailsList = mutableListOf<Pair<MovieDetails, Long>>()
    val followingMovies = mutableListOf<Pair<String, Long>>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_movie_home, container, false)

        homeMovieAdapter = HomeMovieAdapter(requireContext(), emptyList(), this)
        recyclerView= view.findViewById(R.id.rv_home_movie)
        llNoMovies = view.findViewById(R.id.empty_state_layout)
        btnSearch = view.findViewById(R.id.btn_search_movies)
        bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = homeMovieAdapter

        loadHomeMovies()

        return view
    }

    private fun loadHomeMovies() {
        movieDetailsList.clear()
        followingMovies.clear()

        FirebaseInteraction.getFollowingMovies { movies ->
            Log.d("Firebase", "Movies retrieved: $movies")
            Log.d("Firebase", "Movie list: $followingMovies")

            if (movies.isNotEmpty()) {
                recyclerView.visibility = View.VISIBLE
                llNoMovies.visibility = View.GONE
                followingMovies.addAll(movies)
                fetchMovieDetails()
            } else {
                llNoMovies.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
        }

        btnSearch.setOnClickListener {
            val parentFragmentManager = parentFragmentManager
            val searchFragment = SearchFragment()

            parentFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, searchFragment)
                bottomNavigationView.selectedItemId = R.id.search
                commit()
            }
        }
    }

    private fun fetchMovieDetails() {
        for (movieId in followingMovies) {
            MediaRepository.getMovieDetails(
                movieId.first.toInt(),
                onSuccess = ::onMovieDetailsFetched,
                onError = ::onError)
        }
        if (followingMovies.isEmpty()) {
            homeMovieAdapter.updateMovies(emptyList())
            Log.d("FetchMovieDetails", "Nessun film da elaborare.")
        }
    }

    private fun onMovieDetailsFetched(movie: MovieDetails){
        val pair = followingMovies.find { it.first == movie.id.toString() }
        val timestamp = pair!!.second

        movieDetailsList.add(Pair(movie, timestamp))

        if (movieDetailsList.size == followingMovies.size) {
            val sortedList = movieDetailsList.sortedBy { it.second }
            val sortedMovies = sortedList.map { it.first }

            homeMovieAdapter.updateMovies(sortedMovies)
        }
    }

    private fun onError(){
        Log.e("MovieDetailsActivity", "Something went wrong")
    }

    override fun onCheckButtonClicked(movieId: String, movieTitle: String) {
        FirebaseInteraction.removeMovieFromFollowing(movieId.toInt()) {
            loadHomeMovies()
            Toast.makeText(requireContext(), "Congratulations on finishing ${movieTitle}!", Toast.LENGTH_SHORT).show()
        }

        FirebaseInteraction.addMovieToWatched(movieId.toInt())
    }

}