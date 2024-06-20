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
import com.example.tmts.adapters.ExploreShowsAdapter
import com.example.tmts.beans.MovieDetails
import com.example.tmts.beans.SerieDetails
import com.example.tmts.beans.ShowDetails
import com.example.tmts.beans.User
import kotlin.math.min

class ExploreShowsFragment : Fragment() {

    private val MAX_USERS_PER_SHOW: Int = 4
    private lateinit var rvExplore: RecyclerView
    private lateinit var exploreMoviesAdapter: ExploreShowsAdapter
    private var loggedUser: User? = null
    private val retrievedShows = mutableListOf<Pair<String,String>>()
    private val showFollowersList = mutableListOf<Pair<ShowDetails, ArrayList<User>>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore_shows, container, false)
        exploreMoviesAdapter = ExploreShowsAdapter(requireContext())
        rvExplore = view.findViewById(R.id.rv_explore_movie)
        rvExplore.layoutManager = LinearLayoutManager(requireContext())
        rvExplore.adapter = exploreMoviesAdapter
        loadData()
        return view
    }

    private fun loadData() {
        FirebaseInteraction.getLoggedUser(onSuccess = {
            loggedUser = it
            loadShows()
        }, onFailure = {
            Log.e("USER ERROR", "No user logged in")
            loadShows()
        })
    }

    private fun loadShows() {
        retrievedShows.clear()
        showFollowersList.clear()
        FirebaseInteraction.getExploreShows { shows ->
            retrievedShows.addAll(shows)
            fetchDetails()
        }
    }

    private fun fetchDetails() {
        for (show in retrievedShows) {
            val showId = show.second
            when (show.first) {
                "MOV" -> {
                    MediaRepository.getMovieDetails(
                        showId.toInt(),
                        onSuccess = ::onMovieDetailsFetched,
                        onError = ::onError
                    )
                }
                "SER" -> {
                    MediaRepository.getSerieDetails(
                        showId.toInt(),
                        onSuccess = ::onSerieDetailsFetched,
                        onError = :: onError
                    )
                }
            }
        }
        if (retrievedShows.isEmpty()) {
            exploreMoviesAdapter.clearShows()
            Log.d("FetchMovieDetails", "Nessun film da elaborare.")
        }
    }

    private fun onSerieDetailsFetched(serieDetails: SerieDetails) {
        val showDetails = ShowDetails("SER", null, serieDetails)
        showFollowersList.add(Pair(showDetails, ArrayList()))
        FirebaseInteraction.getSerieFollowers(serieDetails.id) { follows ->
            onShowUsersFetched(showDetails, follows)
        }
    }

    private fun onMovieDetailsFetched(movieDetails: MovieDetails) {
        val showDetails = ShowDetails("MOV", movieDetails, null)
        showFollowersList.add(Pair(showDetails, ArrayList()))
        FirebaseInteraction.getMovieFollowers(movieDetails.id) { follows ->
            onShowUsersFetched(showDetails, follows)
        }
    }

    private fun onShowUsersFetched(show: ShowDetails, follows: List<String>){
        val followers = ArrayList(follows)
        if (follows.find { it == loggedUser?.id} != null) {
            followers.remove(loggedUser!!.id)
        }
        for (i in 0..min(MAX_USERS_PER_SHOW - 1, followers.size - 1)) {
            val it = followers[i]
            FirebaseInteraction.getUserInfo(it,
                onSuccess = { user ->
                    exploreMoviesAdapter.updateShows(show, user)
                },
                onFailure = {
                    Log.e("Explore Movie Fragment", "Something went wrong")
                }
            )
        }
    }

}