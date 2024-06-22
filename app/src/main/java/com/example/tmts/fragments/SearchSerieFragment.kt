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
import com.example.tmts.adapters.SearchSerieAdapter
import com.example.tmts.beans.MediaResponse
import com.example.tmts.beans.SerieDetails
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchSerieFragment : Fragment() {
    //private lateinit var tmdbApiClient: TMDbApiClient
    private lateinit var serieAdapter: SearchSerieAdapter
    private lateinit var rv_search_page_serie: RecyclerView
    private var isInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_serie, container, false)

        //tmdbApiClient = TMDbApiClient()
        serieAdapter = SearchSerieAdapter(requireContext(), emptyList())

        rv_search_page_serie = view.findViewById(R.id.rv_search_page_serie)
        rv_search_page_serie.layoutManager = LinearLayoutManager(requireContext())
        rv_search_page_serie.adapter = serieAdapter

        isInitialized = true
        return view
    }

    fun searchSeries(query: String) {
        if (!isInitialized) return

        MediaRepository.searchSeriesByTitle(
            query,
            onSuccess = ::fetchSerieDetails,
            onError = ::onError
        )
    }

    private fun fetchSerieDetails(series: MediaResponse, query: String) {
        val serieIds = mutableListOf<Int>()
        val serieDetailsList = mutableListOf<SerieDetails>()

        for (serie in series.results) {

            if (serie.title.startsWith(query, ignoreCase = true)) {
                serieIds.add(serie.id)
            }
        }

        for (serieId in serieIds) {
            MediaRepository.getSerieDetails(
                serieId,
                onSuccess = {serie ->
                    serieDetailsList.add(serie)
                    if (serieDetailsList.size == serieIds.size) {
                        // Tutti i dettagli dei film sono stati ottenuti
                        serieAdapter.updateSeries(serieDetailsList)
                    }
                },
                onError = ::onError
            )
        }
    }

    fun clearResults() {
        if (!isInitialized) return

        serieAdapter.updateSeries(emptyList())
    }

    fun onError(){
        Log.e("SearchSerieFragment", "Something went wrong")
    }
}