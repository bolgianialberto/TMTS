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
import com.example.tmts.adapters.SearchMovieAdapter
import com.example.tmts.beans.MediaResponse
import com.example.tmts.beans.MovieDetails

class SearchMovieFragment : Fragment() {
    private lateinit var movieAdapter: SearchMovieAdapter
    private lateinit var rv_search_page_movie: RecyclerView
    private var isInitialized = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_movie, container, false)

        //tmdbApiClient = TMDbApiClient()
        movieAdapter = SearchMovieAdapter(requireContext(), emptyList())

        rv_search_page_movie = view.findViewById(R.id.rv_search_page_movie)
        rv_search_page_movie.layoutManager = LinearLayoutManager(requireContext())
        rv_search_page_movie.adapter = movieAdapter

        isInitialized = true
        return view
    }

    fun searchMovies(query: String) {
        if (!isInitialized) return

        MediaRepository.searchMoviesByTitle(
            query,
            onSuccess = ::fetchMovieDetails,
            onError = ::onError
        )
    }

    private fun fetchMovieDetails(movies: MediaResponse, query: String) {
        val movieIds = mutableListOf<Int>()
        val movieDetailsList = mutableListOf<MovieDetails>()

        for (movie in movies.results) {
            if (movie.title.startsWith(query, ignoreCase = true)) { // Controlla se il titolo del film inizia con la query
                movieIds.add(movie.id)
            }
        }

        for (movieId in movieIds) {
            MediaRepository.getMovieDetails(
                movieId,
                onSuccess = {movie ->
                    movieDetailsList.add(movie)
                    if (movieDetailsList.size == movieIds.size) {
                        // Tutti i dettagli dei film sono stati ottenuti
                        movieAdapter.updateMovies(movieDetailsList)
                    }
                },
                onError = ::onError
            )
        }
    }

    fun onError(){
        Log.e("SearchMovieFragment", "Something went wrong")
    }

    fun clearResults(){
        if (!isInitialized) return

        movieAdapter.updateMovies(emptyList())
    }
}