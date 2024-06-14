package com.example.tmts.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmts.MediaRepository
import com.example.tmts.R
import com.example.tmts.TMDbApiClient
import com.example.tmts.adapters.HomeMovieAdapter
import com.example.tmts.beans.MovieDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MovieHomeFragment : Fragment() {
    private lateinit var tmdbApiClient: TMDbApiClient
    private lateinit var homeMovieAdapter: HomeMovieAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var followingMoviesRef: DatabaseReference
    val movieDetailsList = mutableListOf<Pair<MovieDetails, Long>>()
    val followingMovies = mutableListOf<Pair<String, Long>>()
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

        followingMoviesRef = mDbRef.child("users").child(currentUser!!.uid).child("following_movies")

        homeMovieAdapter = HomeMovieAdapter(requireContext(), emptyList())

        val recyclerView: RecyclerView = view.findViewById(R.id.rv_home_movie)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = homeMovieAdapter

        loadHomeMovies(currentUser)

        return view
    }

    private fun loadHomeMovies(currentUser: FirebaseUser?) {
        followingMoviesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                followingMovies.clear() // Pulisci la lista prima di aggiungere nuovi elementi
                snapshot.children.forEach { child ->
                    val movieId = child.key
                    val timestamp = child.child("timestamp").getValue(Long::class.java)
                    if (movieId != null && timestamp != null) {
                        followingMovies.add(Pair(movieId, timestamp))
                    }
                }
                fetchMovieDetails()
            }

            override fun onCancelled(error: DatabaseError) {
                println("Errore nel recupero dei dati: ${error.message}")
            }
        })
    }

    private fun fetchMovieDetails() {
        for (movieId in followingMovies) {
            MediaRepository.getMovieDetails(
                movieId.first.toInt(),
                onSuccess = ::onMovieDetailsFetched,
                onError = ::onError)
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

}