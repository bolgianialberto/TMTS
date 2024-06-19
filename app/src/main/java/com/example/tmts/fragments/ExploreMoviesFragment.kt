package com.example.tmts.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmts.FirebaseInteraction
import com.example.tmts.FirebaseInteraction.onError
import com.example.tmts.MediaRepository
import com.example.tmts.R
import com.example.tmts.adapters.ExploreMovieAdapter
import com.example.tmts.beans.MovieDetails

class ExploreMoviesFragment : Fragment() {

    private lateinit var rvExplore: RecyclerView
    private lateinit var exploreMoviesAdapter: ExploreMovieAdapter
    val movieDetailsList = mutableListOf<Pair<MovieDetails, Long>>()
    val followingMovies = mutableListOf<Pair<String, Long>>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore_movies, container, false)
        exploreMoviesAdapter = ExploreMovieAdapter(requireContext(), emptyList())
        rvExplore = view.findViewById(R.id.rv_explore_movie)
        rvExplore.layoutManager = LinearLayoutManager(requireContext())
        rvExplore.adapter = exploreMoviesAdapter
        loadMovies()
        return view
    }

    private fun loadMovies() {
        movieDetailsList.clear()
        followingMovies.clear()
        FirebaseInteraction.getFollowingMovies { movies ->
            followingMovies.addAll(movies)
            fetchDetails()
        }
    }

    private fun fetchDetails() {
        for (movieId in followingMovies) {
            MediaRepository.getMovieDetails(
                movieId.first.toInt(),
                onSuccess = ::onMovieDetailsFetched,
                onError = ::onError)
        }
        if (followingMovies.isEmpty()) {
            exploreMoviesAdapter.updateMovies(emptyList())
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

            exploreMoviesAdapter.updateMovies(sortedMovies)
        }
    }
}