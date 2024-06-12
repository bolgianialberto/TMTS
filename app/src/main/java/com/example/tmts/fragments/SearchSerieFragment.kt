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
import com.example.tmts.adapters.SearchSerieAdapter
import com.example.tmts.beans.MediaResponse
import com.example.tmts.beans.SerieDetails
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchSerieFragment : Fragment() {
    private lateinit var tmdbApiClient: TMDbApiClient
    private lateinit var serieAdapter: SearchSerieAdapter
    private lateinit var rv_search_page_serie: RecyclerView
    private var isInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_serie, container, false)

        tmdbApiClient = TMDbApiClient()
        serieAdapter = SearchSerieAdapter(requireContext(), emptyList())

        rv_search_page_serie = view.findViewById(R.id.rv_search_page_serie)
        rv_search_page_serie.layoutManager = LinearLayoutManager(requireContext())
        rv_search_page_serie.adapter = serieAdapter

        isInitialized = true
        return view
    }

    fun searchSeries(query: String) {
        if (!isInitialized) return

        val call = tmdbApiClient.getClient().searchSerie(tmdbApiClient.getApiKey(), query, 1)

        call.enqueue(object: Callback<MediaResponse> {
            override fun onResponse(call: Call<MediaResponse>, response: Response<MediaResponse>) {
                if (response.isSuccessful) {
                    val series = response.body()
                    if (series!= null) {
                        fetchSerieDetails(series, query)
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

    private fun fetchSerieDetails(series: MediaResponse, query: String) {
        val serieIds = mutableListOf<Int>()
        val serieDetailsList = mutableListOf<SerieDetails>()

        for (serie in series.results) {

            if (serie.original_name.startsWith(query, ignoreCase = true)) {
                serieIds.add(serie.id)
            }
        }

        for (serieId in serieIds) {
            val call = tmdbApiClient.getClient().getSerieDetails(serieId, tmdbApiClient.getApiKey())

            call.enqueue(object: Callback<SerieDetails> {
                override fun onResponse(
                    call: Call<SerieDetails>,
                    response: Response<SerieDetails>
                ) {
                    if (response.isSuccessful) {
                        val serie = response.body()
                        if (serie != null) {
                            serieDetailsList.add(serie)
                            if (serieDetailsList.size == serieIds.size) {
                                // Tutti i dettagli dei film sono stati ottenuti
                                serieAdapter.updateSeries(serieDetailsList)
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

                override fun onFailure(call: Call<SerieDetails>, t: Throwable) {
                    Log.e("MovieDetailsActivity", "Network Error: ${t.message}")
                }
            })
        }
    }

    fun clearResults() {
        if (!isInitialized) return

        serieAdapter.updateSeries(emptyList())
    }
}