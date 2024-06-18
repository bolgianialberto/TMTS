package com.example.tmts

import android.util.Log
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener

object FirebaseInteraction {
    var mDbRef = FirebaseDatabase.getInstance().getReference()
    var mAuth = FirebaseAuth.getInstance()
    val user = mAuth.currentUser!!
    val followingSeriesRef = mDbRef.child("users").child(user.uid).child("following_series")
    val followingMoviesRef = mDbRef.child("users").child(user.uid).child("following_movies")
    val watchedMoviesRef = mDbRef.child("users").child(user.uid).child("watched_movies")

    fun getFollowingSeries(callback: (List<Triple<String, String, Long>>) -> Unit){
        followingSeriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val series = mutableListOf<Triple<String, String, Long>>()

                snapshot.children.forEach { child ->
                    val serieId = child.key
                    val nextToSee = child.child("nextToSee").getValue(String::class.java)
                    val timestamp = child.child("timestamp").getValue(Long::class.java)
                    if (serieId != null && nextToSee != null && timestamp != null) {
                        series.add(Triple(serieId, nextToSee, timestamp))
                    }
                }

                callback(series)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Errore nel recupero dei dati: ${error.message}")
                callback(emptyList())
            }
        })
    }

    fun getFollowingMovies(callback: (List<Pair<String, Long>>) -> Unit) {
        followingMoviesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val movies = mutableListOf<Pair<String, Long>>()

                snapshot.children.forEach { child ->
                    val movieId = child.key
                    val timestamp = child.child("timestamp").getValue(Long::class.java)
                    if (movieId != null && timestamp != null) {
                        movies.add(Pair(movieId, timestamp))
                    }
                }

                callback(movies)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Errore nel recupero dei dati: ${error.message}")
                // Chiamata della callback con lista vuota in caso di errore
                callback(emptyList())
            }
        })
    }

    fun checkSerieExistanceInFollowing(
        serieId: Int,
        callback: (Boolean) -> Unit
    ){
        val serieRef = followingSeriesRef
            .child(serieId.toString())

        serieRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val exists = dataSnapshot.exists()
                callback(exists)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FirebaseCheck", "checkSerieExists:onCancelled", databaseError.toException())
                callback(false)
            }
        })
    }

    fun checkMovieExistanceInFollowing(
        movieId: Int,
        callback: (Boolean) -> Unit
    ){
        val movieRef = followingMoviesRef
            .child(movieId.toString())

        movieRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val exists = dataSnapshot.exists()
                callback(exists)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FirebaseCheck", "checkSerieExists:onCancelled", databaseError.toException())
                callback(false)
            }
        })
    }

    fun checkEpisodeValue(
        serieId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
        callback: (Boolean?) -> Unit
    ){
        val episodeRef = mDbRef
            .child("users")
            .child(user.uid)
            .child("following_series")
            .child(serieId.toString())
            .child("seasons")
            .child(seasonNumber.toString())
            .child("episodes")
            .child(episodeNumber.toString())

        episodeRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val episodeValue = snapshot.getValue(Boolean::class.java)
                    Log.d("FirebaseCheck", "Episode value: $episodeValue")
                    callback(episodeValue)
                } else {
                    Log.d("FirebaseCheck", "Episode does not exist")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("FirebaseCheck", "checkEpisodeExists:onCancelled", error.toException())
            }

        })

    }

    fun removeSerieFromFollowing(serieId: Int, onSuccess: (() -> Unit)? = null) {
        val serieRef = followingSeriesRef
            .child(serieId.toString())

        serieRef.removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // La serie è stata rimossa con successo
                    onSuccess?.invoke() // Esegui la callback onSuccess se è stata fornita
                } else {
                    // Gestisci il caso in cui la rimozione non sia stata completata con successo
                    Log.e("Firebase", "Errore nella rimozione della serie: ${task.exception?.message}")
                }
            }
    }

    fun removeMovieFromFollowing(movieId: Int, onSuccess: (() -> Unit)? = null) {
        val movieRef = followingMoviesRef
            .child(movieId.toString())

        movieRef.removeValue()
            .addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    onSuccess?.invoke()
                    Log.d("Firebase", "Film rimosso con successo")
                } else {
                    Log.e("Firebase", "Errore nella rimozione del film", task.exception)
                }
            }
    }

    fun addSerieToFollowing(serieId: Int, onSuccess: (() -> Unit)? = null) {
        MediaRepository.getSerieDetails(
            serieId,
            onSuccess = { serie ->
                val serieRef = followingSeriesRef
                    .child(serieId.toString())

                serieRef.child("nextToSee").setValue("1_1")
                serieRef.child("timestamp").setValue(ServerValue.TIMESTAMP)

                var operationsToComplete = serie.number_of_seasons

                for (n_season in 1..serie.number_of_seasons) {
                    val seasonRef = serieRef.child("seasons").child(n_season.toString())
                    seasonRef.child("status").setValue(false)

                    MediaRepository.getSeasonDetails(
                        serieId,
                        n_season,
                        onSuccess = { season ->
                            season?.let {
                                val episodesRef = seasonRef.child("episodes")

                                for (n_episode in 1..it.number_of_episodes) {
                                    episodesRef.child(n_episode.toString()).setValue(false)
                                }
                            }
                            // Controlla se tutte le operazioni sono completate
                            operationsToComplete--
                            if (operationsToComplete == 0) {
                                onSuccess?.invoke() // Chiamare onSuccess solo quando tutte le operazioni sono completate
                            }
                        },
                        onError = ::onError
                    )
                }
            },
            onError = ::onError
        )
    }

    fun addMovieToFollowing(movieId: Int, onSuccess: (() -> Unit)? = null) {
        val movieRef = followingMoviesRef.child(movieId.toString())
        val timestampValue = ServerValue.TIMESTAMP

        movieRef.child("timestamp").setValue(timestampValue)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess?.invoke()
                    Log.d("Firebase", "Film aggiunto con successo a Firebase")
                } else {
                    Log.e("Firebase", "Errore durante l'aggiunta del film a Firebase", task.exception)
                }
            }
    }


    fun addMovieToWatched(movieId: Int) {
        watchedMoviesRef.child(movieId.toString()).setValue(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Firebase", "Film aggiunto con successo")
                } else {
                    Log.e("Firebase", "Errore nell'aggiunta del film", task.exception)
                }
            }
    }

    fun updateNextToSee (
        serieId: Int,
        watchedSeason: Int,
        watchedEpisode: Int,
        callback: (() -> Unit)? = null
    ){
        getNextToSee(serieId) {nextToSeePair ->
            if (watchedSeason < nextToSeePair!!.first || (watchedSeason == nextToSeePair.first && watchedEpisode < nextToSeePair.second)) {
                setEpisodeValue(serieId, watchedSeason, watchedEpisode, false) {
                    setNextToSee(serieId, watchedSeason, watchedEpisode) {
                        callback?.invoke()
                    }
                }
            } else {
                Log.d("FirebaseCheck", "current next to see: ${nextToSeePair}")
                setEpisodeValue(serieId, watchedSeason, watchedEpisode, true) {
                    Log.d("FirebaseCheck", "Episode watched: ${watchedSeason} - ${watchedEpisode}")
                    if (nextToSeePair!!.first == watchedSeason && nextToSeePair!!.second == watchedEpisode) {
                        val serieRef = followingSeriesRef
                            .child(serieId.toString())

                        fun findNextEpisode(seasonNumber: Int, episodeNumber: Int) {
                            val nextEpisodeRef = serieRef
                                .child("seasons")
                                .child(seasonNumber.toString())
                                .child("episodes")
                                .child(episodeNumber.toString())

                            nextEpisodeRef.addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists() && snapshot.getValue(Boolean::class.java) == true) {
                                        // Se l'episodio è già visto, passa al successivo
                                        findNextEpisode(seasonNumber, episodeNumber + 1)
                                    } else if (snapshot.exists()) {
                                        // Se l'episodio esiste ed è non visto, aggiornalo come prossimo da vedere
                                        val newNext = "${seasonNumber}_$episodeNumber"
                                        serieRef.child("nextToSee").setValue(newNext)
                                        callback?.invoke()
                                    } else {
                                        // Se l'episodio non esiste, controlla la prossima stagione
                                        serieRef.child("seasons").child(seasonNumber.toString())
                                            .child("status").setValue(true)

                                        val nextSeasonRef = serieRef
                                            .child("seasons")
                                            .child((seasonNumber + 1).toString())

                                        nextSeasonRef.addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.exists()) {
                                                    // Se la stagione successiva esiste, inizia dal primo episodio
                                                    findNextEpisode(seasonNumber + 1, 1)
                                                } else {
                                                    // Se non esistono più stagioni, imposta nextToSee a null e rimuovi la serie
                                                    serieRef.child("nextToSee").setValue(null)
                                                    serieRef.removeValue()
                                                        .addOnCompleteListener { task ->
                                                            if (task.isSuccessful) {
                                                                Log.d(
                                                                    "Firebase",
                                                                    "Serie rimossa con successo"
                                                                )
                                                            } else {
                                                                Log.e(
                                                                    "Firebase",
                                                                    "Errore nella rimozione della serie",
                                                                    task.exception
                                                                )
                                                            }
                                                        }
                                                    // Aggiungi la serie alle serie viste
                                                    val watchedSeriesRef =
                                                        mDbRef.child("users").child(user!!.uid)
                                                            .child("watched_series")

                                                    watchedSeriesRef.child(serieId.toString())
                                                        .setValue(true)
                                                    callback?.invoke()
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                Log.e(
                                                    "SerieHomeFragment",
                                                    "Errore durante il controllo della prossima stagione: ${error.message}"
                                                )
                                            }
                                        })
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e(
                                        "SerieHomeFragment",
                                        "Errore durante il controllo del prossimo episodio: ${error.message}"
                                    )
                                }
                            })
                        }

                        // Inizia la ricerca del prossimo episodio non visto
                        findNextEpisode(nextToSeePair.first, nextToSeePair.second + 1)
                    } else {
                        callback?.invoke()
                    }
                }
            }
        }
    }

    fun getNextToSee(serieId: Int, callback: (Pair<Int, Int>?) -> Unit){
        val serieRef = followingSeriesRef
            .child(serieId.toString())

        serieRef.child("nextToSee").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nextToSeeValue = snapshot.getValue(String::class.java)
                if (nextToSeeValue != null) {
                    val parts = nextToSeeValue.split("_")
                    if (parts.size == 2) {
                        val season = parts[0].toIntOrNull()
                        val episode = parts[1].toIntOrNull()
                        if (season != null && episode != null) {
                            callback(Pair(season, episode))
                            return
                        }
                    }
                }
                callback(null)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SerieHomeFragment", "Errore durante il recupero di nextToSee: ${error.message}")
            }
        })
    }

    fun setNextToSee(
        serieId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
        callback: (() -> Unit)?
    ) {
        val serieRef = followingSeriesRef
            .child(serieId.toString())

        val nextToSee = "${seasonNumber}_$episodeNumber"
        serieRef.child("nextToSee").setValue(nextToSee)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback?.invoke()
                } else {
                    Log.e("Firebase", "Errore durante l'aggiornamento di nextToSee", task.exception)
                }
            }
    }

    fun setEpisodeValue(
        serieId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
        value: Boolean,
        callback: (() -> Unit)?
    ){
        val serieRef = followingSeriesRef
            .child(serieId.toString())

        val episodeRef = serieRef.child("seasons")
            .child(seasonNumber.toString())
            .child("episodes")
            .child(episodeNumber.toString())

        episodeRef.setValue(value)
            .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Operazione completata con successo, invoca la callback
                callback?.invoke()
            } else {
                // Gestione degli errori, se necessario
                Log.e("Firebase", "Errore durante l'impostazione dell'episodio come visto", task.exception)
            }
        }
    }

    fun onError(){
        Log.e("Firebase", "Something went wrong")
    }
}