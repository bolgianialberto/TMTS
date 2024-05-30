package com.example.tmts.fragments

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
import com.example.tmts.MediaDetaisActivity
import com.example.tmts.beans.Media
import com.example.tmts.beans.MediaResponse
import com.example.tmts.R
import com.example.tmts.activities.SearchActivity
import com.example.tmts.TMDbApiClient
import com.example.tmts.activities.MediaDetaisActivity
import com.example.tmts.adapters.MediaAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchFragment : Fragment() {

    private lateinit var tmdbApiClient: TMDbApiClient
    private lateinit var popularMovieAdapter: MediaAdapter
    private lateinit var popularSerieAdapter: MediaAdapter
    // private lateinit var etPopularSearch: EditText
    private lateinit var btnSearchPopular: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        tmdbApiClient = TMDbApiClient()
        popularMovieAdapter = MediaAdapter(requireContext(), emptyList()) { movie ->
            val intent = Intent(requireContext(), MediaDetaisActivity::class.java)
            intent.putExtra("movieId", movie.id)
            startActivity(intent)
        }
        popularSerieAdapter = MediaAdapter(requireContext(), emptyList()) {serie ->
            val intent = Intent(requireContext(), MediaDetaisActivity::class.java)
            intent.putExtra("serieId", serie.id)
            startActivity(intent)
        }

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

        loadPopularMedia(
            call = tmdbApiClient.getClient().getPopularMovies(tmdbApiClient.getApiKey(), 1),
            adapter = popularMovieAdapter
        )
        loadPopularMedia(
            call = tmdbApiClient.getClient().getPopularSeries(tmdbApiClient.getApiKey(), 1),
            adapter = popularSerieAdapter
        )

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

    private fun loadPopularMedia(call: Call<MediaResponse>, adapter: MediaAdapter){
        call.enqueue(object : Callback<MediaResponse> {
            override fun onResponse(call: Call<MediaResponse>, response: Response<MediaResponse>) {
                if (response.isSuccessful) {
                    val mediaResponse = response.body()
                    val mediaItems: List<Media>? = mediaResponse?.results
                    if (mediaItems != null) {
                        adapter.updateMedia(mediaItems)
                    } else {
                        Log.e("API Call", "La risposta non contiene media.")
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
