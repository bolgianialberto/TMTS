package com.example.tmts.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmts.R
import com.example.tmts.TMDbApiClient
import com.example.tmts.adapters.HomeMovieAdapter
import com.example.tmts.beans.MovieDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MovieHomeFragment : Fragment() {
    private lateinit var tmdbApiClient: TMDbApiClient
    private lateinit var homeMovieAdapter: HomeMovieAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_movie_home, container, false)

        tmdbApiClient = TMDbApiClient()
        mDbRef = FirebaseDatabase.getInstance().getReference()
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser

        homeMovieAdapter = HomeMovieAdapter(requireContext(), emptyList())

        val recyclerView: RecyclerView = view.findViewById(R.id.rv_home_movie)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = homeMovieAdapter

        loadHomeMovies(currentUser)

        return view
    }

    private fun loadHomeMovies(currentUser: FirebaseUser?) {
        currentUser?.let {
            mDbRef.child("users").child(it.uid).child("following_shows").get().addOnSuccessListener { snapshot ->
                val followingMovies = mutableListOf<Int>()
                snapshot.children.forEach { child ->
                    child.getValue(Int::class.java)?.let { movieId ->
                        followingMovies.add(movieId)
                    }
                }
                fetchMovieDetails(followingMovies)
            }.addOnFailureListener { exception ->
                // Handle any errors
            }
        }
    }

    private fun fetchMovieDetails(movieIds: List<Int>) {
        val movieDetailsList = mutableListOf<MovieDetails>()

        for (movieId in movieIds) {
            val call = tmdbApiClient.getClient().getMovieDetails(movieId, tmdbApiClient.getApiKey())

            call.enqueue(object: Callback<MovieDetails> {
                override fun onResponse(
                    call: Call<MovieDetails>,
                    response: Response<MovieDetails>
                ) {
                    if (response.isSuccessful) {
                        val movie = response.body()
                        if (movie != null) {
                            movieDetailsList.add(movie)

                            if (movieDetailsList.size == movieIds.size) {
                                // Tutti i dettagli dei film sono stati ottenuti
                                homeMovieAdapter.updateMovies(movieDetailsList)
                            }
                        } else {
                            Log.e("MovieDetailsActivity", "Movie details not found")
                        }
                    } else {
                        Log.e("MovieDetailsActivity", "Error ${response.code()}: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<MovieDetails>, t: Throwable) {
                    Log.e("MovieDetailsActivity", "Network Error: ${t.message}")
                }
            })
        }
    }


}