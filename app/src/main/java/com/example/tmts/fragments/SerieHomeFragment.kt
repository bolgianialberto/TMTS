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
import com.example.tmts.OnCheckButtonClickListener
import com.example.tmts.R
import com.example.tmts.TMDbApiClient
import com.example.tmts.adapters.HomeSerieAdapter
import com.example.tmts.beans.EpisodeDetails
import com.example.tmts.beans.SerieDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SerieHomeFragment : Fragment(), OnCheckButtonClickListener {
    private lateinit var tmdbApiClient: TMDbApiClient
    private lateinit var homeSerieAdapter: HomeSerieAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var followingSeriesRef: DatabaseReference
    private lateinit var seriesRef: DatabaseReference
    private var currentUser: FirebaseUser? = null
    val episodeDetailsList = mutableListOf<Pair<EpisodeDetails, Long>>()
    val followingSeries = mutableListOf<Triple<String, String, Long>>() // (serieId, nextToSee, timestamp)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_serie_home, container, false)

        tmdbApiClient = TMDbApiClient()
        mDbRef = FirebaseDatabase.getInstance().getReference()
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser

        followingSeriesRef = mDbRef.child("users").child(currentUser!!.uid).child("following_series")

        homeSerieAdapter = HomeSerieAdapter(requireContext(), emptyList(), this)

        val recyclerView: RecyclerView = view.findViewById(R.id.rv_home_serie)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = homeSerieAdapter

        loadHomeSerie()

        return view
    }

    private fun loadHomeSerie() {
        followingSeriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                followingSeries.clear() // Pulisci la lista prima di aggiungere nuovi elementi
                snapshot.children.forEach { child ->
                    val serieId = child.key
                    val nextToSee = child.child("nextToSee").getValue(String::class.java)
                    val timestamp = child.child("timestamp").getValue(Long::class.java)
                    if (serieId != null && nextToSee != null && timestamp != null) {
                        followingSeries.add(Triple(serieId, nextToSee, timestamp))
                    }
                }
                fetchNextEpisodes()
            }

            override fun onCancelled(error: DatabaseError) {
                println("Errore nel recupero dei dati: ${error.message}")
            }
        })
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
        seriesRef = followingSeriesRef.child(serieId)

        seriesRef.child("nextToSee").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nextToSeeValue = snapshot.getValue(String::class.java)
                updateNextToSee(nextToSeeValue)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SerieHomeFragment", "Errore durante il recupero di nextToSee: ${error.message}")
            }
        })
    }

    private fun updateNextToSee(nextToSee: String?) {
        val (currentSeasonNumberStr, currentEpisodeNumberStr) = nextToSee!!.split("_")
        var seasonNumber = currentSeasonNumberStr.toInt()
        var episodeNumber = currentEpisodeNumberStr.toInt()

        // settare a true l'episodio appena visto
        seriesRef.child("seasons")
            .child(seasonNumber.toString())
            .child("episodes")
            .child(currentEpisodeNumberStr)
            .setValue(true)

        // trovare il prossimo episodio
        val nextEpisodeRef = seriesRef
            .child("seasons")
            .child(currentSeasonNumberStr)
            .child("episodes")
            .child((episodeNumber + 1).toString())

        nextEpisodeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val episodeExists = snapshot.exists()
                if (episodeExists) {
                    episodeNumber++
                    val newNext = "${seasonNumber}_$episodeNumber"
                    seriesRef.child("nextToSee").setValue(newNext)
                    loadHomeSerie()
                } else {
                    // Se l'episodio non esiste, controlla la prossima stagione
                    val nextSeasonRef = seriesRef
                        .child("seasons")
                        .child((seasonNumber + 1).toString())

                    nextSeasonRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val seasonExists = snapshot.exists()
                            if (seasonExists) {
                                seasonNumber++
                                episodeNumber = 1
                                val newNext = "${seasonNumber}_$episodeNumber"
                                seriesRef.child("nextToSee").setValue(newNext)
                            } else {
                                // Se non esistono più episodi o stagioni, impostare nextToSee a null
                                seriesRef.child("nextToSee").setValue(null)
                            }
                            loadHomeSerie() // Spostato qui per evitare duplicati
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("SerieHomeFragment", "Errore durante il controllo della prossima stagione: ${error.message}")
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SerieHomeFragment", "Errore durante il controllo del prossimo episodio: ${error.message}")
            }
        })
    }
}
