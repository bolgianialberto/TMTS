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
import com.example.tmts.adapters.SearchMovieAdapter
import com.example.tmts.beans.MediaResponse
import com.example.tmts.beans.MovieDetails
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchMovieFragment : Fragment() {
    private lateinit var tmdbApiClient: TMDbApiClient
    private lateinit var movieAdapter: SearchMovieAdapter
    private lateinit var rv_search_page_movie: RecyclerView
    private var isInitialized = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_movie, container, false)

        tmdbApiClient = TMDbApiClient()
        movieAdapter = SearchMovieAdapter(requireContext(), emptyList())

        rv_search_page_movie = view.findViewById(R.id.rv_search_page_movie)
        rv_search_page_movie.layoutManager = LinearLayoutManager(requireContext())
        rv_search_page_movie.adapter = movieAdapter

        isInitialized = true
        return view
    }

    fun searchMovies(query: String) {
        if (!isInitialized) return

        val call = tmdbApiClient.getClient().searchMovies(tmdbApiClient.getApiKey(), query, 1)

        call.enqueue(object: Callback<MediaResponse> {
            override fun onResponse(
                call: Call<MediaResponse>,
                response: Response<MediaResponse>
            ) {
                if (response.isSuccessful) {
                    val movies = response.body()
                    if ( movies != null ) {
                        fetchMovieDetails(movies, query)
                    } else {
                        Log.e("API Call", "La risposta non contiene film.")
                    }
                } else {
                    Log.e("API Call", "Errore nella chiamata API: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<MediaResponse>, t: Throwable) {
                Log.e("API Call", "Errore di rete: ${t.message}")
            }
        })
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
                                movieAdapter.updateMovies(movieDetailsList)
                            }
                        } else {
                            Log.e("MovieDetailsActivity", "Movie details not found")
                        }
                    } else {
                        Log.e(
                            "MovieDetailsActivity",
                            "Error ${response.code()}: ${response.message()}"
                        )
                    }
                }

                override fun onFailure(call: Call<MovieDetails>, t: Throwable) {
                    Log.e("MovieDetailsActivity", "Network Error: ${t.message}")
                }
            })
        }
    }

    fun clearResults(){
        if (!isInitialized) return

        movieAdapter.updateMovies(emptyList())
    }
}