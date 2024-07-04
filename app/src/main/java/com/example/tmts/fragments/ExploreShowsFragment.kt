package com.example.tmts.fragments

import android.content.Intent
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
import com.example.tmts.activities.MoreShowAccountsActivity
import com.example.tmts.adapters.ExploreShowsAdapter
import com.example.tmts.beans.MovieDetails
import com.example.tmts.beans.SerieDetails
import com.example.tmts.beans.User
import com.example.tmts.beans.results.ShowDetailsResult
import com.example.tmts.interfaces.OnMoreAccountClickListener
import kotlin.math.min

class ExploreShowsFragment : Fragment(), OnMoreAccountClickListener{

    private val MAX_USERS_PER_SHOW: Int = 4
    private lateinit var rvExplore: RecyclerView
    private lateinit var exploreMoviesAdapter: ExploreShowsAdapter
    private var loggedUser: User? = null
    private val retrievedShows = mutableListOf<Pair<String,String>>()
    private val showFollowersList = mutableListOf<Pair<ShowDetailsResult, ArrayList<User>>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore_shows, container, false)

        exploreMoviesAdapter = ExploreShowsAdapter(requireContext(), ArrayList(), this)
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
        val showDetailsResult = ShowDetailsResult("SER", null, serieDetails)
        showFollowersList.add(Pair(showDetailsResult, ArrayList()))
        FirebaseInteraction.getSerieFollowers(serieDetails.id) { follows ->
            onShowUsersFetched(showDetailsResult, follows)
        }
    }

    private fun onMovieDetailsFetched(movieDetails: MovieDetails) {
        val showDetailsResult = ShowDetailsResult("MOV", movieDetails, null)
        showFollowersList.add(Pair(showDetailsResult, ArrayList()))
        FirebaseInteraction.getMovieFollowers(movieDetails.id) { follows ->
            onShowUsersFetched(showDetailsResult, follows)
        }
    }

    private fun onShowUsersFetched(show: ShowDetailsResult, follows: List<String>){
        val followers = ArrayList(follows)
        followers.shuffle()
        if (follows.find { it == loggedUser?.id} != null) {
            followers.remove(loggedUser!!.id)
        }
        show.retrievedUsers.addAll(followers)
        val nUsersShowed = if (show.retrievedUsers.size <= 4) min(MAX_USERS_PER_SHOW - 1, show.retrievedUsers.size - 1) else MAX_USERS_PER_SHOW - 2
        for (i in 0..nUsersShowed) {
            val it = followers[i]
            FirebaseInteraction.getUserInfo(it,
                onSuccess = { user ->
                    show.loadedUsers.add(user)
                    exploreMoviesAdapter.updateShows(show)
                },
                onFailure = {
                    Log.e("Explore Movie Fragment", "Something went wrong")
                }
            )
        }
    }

    override fun onMoreAccountClickListener(moreAccountClickResult: ShowDetailsResult) {
        val intent = Intent(this.context, MoreShowAccountsActivity::class.java)
        intent.putExtra("showType", moreAccountClickResult.showTypeId)
        when(moreAccountClickResult.showTypeId) {
            "MOV" -> intent.putExtra("showId", moreAccountClickResult.movieDetails!!.id.toString())
            "SER" -> intent.putExtra("showId", moreAccountClickResult.serieDetails!!.id.toString())
        }
        intent.putExtra("retrievedFollowers", moreAccountClickResult.retrievedUsers)
        intent.putExtra("loadedUsers", ArrayList(moreAccountClickResult.loadedUsers.map { it.id }))
        context?.startActivity(intent)
    }


}