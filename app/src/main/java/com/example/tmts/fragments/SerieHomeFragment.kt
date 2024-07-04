package com.example.tmts.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmts.FirebaseInteraction
import com.example.tmts.MediaRepository
import com.example.tmts.OnCheckButtonClickListener
import com.example.tmts.R
import com.example.tmts.adapters.HomeSerieAdapter
import com.example.tmts.beans.EpisodeDetails
import com.example.tmts.beans.SerieDetails
import com.google.android.material.bottomnavigation.BottomNavigationView

class SerieHomeFragment : Fragment(), OnCheckButtonClickListener {
    private lateinit var homeSerieAdapter: HomeSerieAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var llNoSeries: LinearLayout
    private lateinit var btnSearch: Button
    private lateinit var bottomNavigationView: BottomNavigationView

    val episodeDetailsList = mutableListOf<Pair<EpisodeDetails, Long>>()
    val followingSeries = mutableListOf<Triple<String, String, Long>>() // (serieId, nextToSee, timestamp)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_serie_home, container, false)

        homeSerieAdapter = HomeSerieAdapter(requireContext(), emptyList(), this)
        recyclerView= view.findViewById(R.id.rv_home_serie)
        llNoSeries = view.findViewById(R.id.empty_state_layout)
        btnSearch = view.findViewById(R.id.btn_search_series)
        bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = homeSerieAdapter

        //loadHomeSerie()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadHomeSerie()
    }

    private fun loadHomeSerie() {
        episodeDetailsList.clear()
        followingSeries.clear()

        FirebaseInteraction.getFollowingSeries { series ->
            if (series.isNotEmpty()) {
                recyclerView.visibility = View.VISIBLE
                llNoSeries.visibility = View.GONE

                followingSeries.addAll(series)
                fetchNextEpisodes()
            } else {
                llNoSeries.visibility = View.VISIBLE
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

    private fun fetchNextEpisodes() {
        episodeDetailsList.clear()

        for ((serieId, nextToSee, _) in followingSeries) {
            val (seasonNumber, episodeNumber) = nextToSee.split("_").map { it.toInt() }

            MediaRepository.getEpisodeDetails(
                serieId.toInt(),
                seasonNumber,
                episodeNumber,
                onSuccess = ::onFetchedEpisodeDetails,
                onError = ::onError
            )
        }
    }

    private fun onFetchedEpisodeDetails(episode: EpisodeDetails) {
        MediaRepository.getSerieDetails(
            episode.serieId,
            onSuccess = { serie: SerieDetails ->
                episode.serieName = serie.title
                episode.seriePosterPath = serie.posterPath

                // aggiungo il timestamp
                val triple = followingSeries.find { it.first == episode.serieId.toString() }
                val timestamp = triple!!.third

                episodeDetailsList.add(Pair(episode, timestamp))
                if (episodeDetailsList.size == followingSeries.size) {
                    //ordino secondo il timestamp
                    val sortedList = episodeDetailsList.sortedBy { it.second }
                    val sortedEpisodes = sortedList.map { it.first }

                    homeSerieAdapter.updateEpisodes(sortedEpisodes)
                }
            },
            onError = ::onError
        )
    }

    private fun onError() {
        Log.e("SerieDetailsActivity", "Something went wrong")
    }

    override fun onCheckButtonClicked(serieId: String) {

        FirebaseInteraction.getNextToSee(serieId.toInt()) {nextToSeePair ->
            FirebaseInteraction.updateNextToSee(
                requireContext(),
                serieId.toInt(),
                nextToSeePair!!.first,
                nextToSeePair!!.second,
            ) {
                loadHomeSerie()
            }
        }
    }
}
