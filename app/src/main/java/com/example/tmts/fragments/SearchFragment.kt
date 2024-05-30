package com.example.tmts.fragments

import com.example.tmts.adapters.PopularMovieAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmts.beans.Media
import com.example.tmts.beans.MediaResponse
import com.example.tmts.adapters.PopularSerieAdapter
import com.example.tmts.R
import com.example.tmts.activities.SearchActivity
import com.example.tmts.TMDbApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchFragment : Fragment() {

    private lateinit var tmdbApiClient: TMDbApiClient
    private lateinit var popularMovieAdapter: PopularMovieAdapter
    private lateinit var popularSerieAdapter: PopularSerieAdapter
    // private lateinit var etPopularSearch: EditText
    private lateinit var btnSearchPopular: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        tmdbApiClient = TMDbApiClient()
        popularMovieAdapter = PopularMovieAdapter(requireContext(), emptyList())
        popularSerieAdapter = PopularSerieAdapter(requireContext(), emptyList())

        //etPopularSearch = view.findViewById(R.id.et_search_popular)
        btnSearchPopular = view.findViewById(R.id.btn_search_popular)

        val rvPopularMovie: RecyclerView = view.findViewById(R.id.rv_popular_movies)
        rvPopularMovie.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvPopularMovie.adapter = popularMovieAdapter

        val rvPopularSerie: RecyclerView = view.findViewById(R.id.rv_popular_series)
        rvPopularSerie.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvPopularSerie.adapter = popularSerieAdapter

        loadPopularMovies()
        loadPopularSeries()

        /*
        etPopularSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val intent = Intent(requireContext(), SearchActivity::class.java)
                startActivity(intent)
            }
        }
        */

        btnSearchPopular.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun loadPopularSeries() {
        val call = tmdbApiClient.getClient().getPopularSeries(tmdbApiClient.getApiKey(), 1)

        call.enqueue(object : Callback<MediaResponse> {
            override fun onResponse(call: Call<MediaResponse>, response: Response<MediaResponse>) {
                if (response.isSuccessful) {
                    val serieResponse = response.body()
                    val series: List<Media>? = serieResponse?.results
                    if (series != null) {
                        popularSerieAdapter.updateSeries(series)
                    } else {
                        Log.e("API Call", "La risposta non contiene serie TV.")
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

    private fun loadPopularMovies() {
        val call = tmdbApiClient.getClient().getPopularMovies(tmdbApiClient.getApiKey(), 1)

        call.enqueue(object : Callback<MediaResponse> {
            override fun onResponse(call: Call<MediaResponse>, response: Response<MediaResponse>) {
                if (response.isSuccessful) {
                    val movieResponse = response.body()
                    val movies: List<Media>? = movieResponse?.results
                    if (movies != null) {
                        popularMovieAdapter.updateMovies(movies)
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
}
