package com.example.tmts

import android.icu.text.SimpleDateFormat
import android.util.Log
import com.example.tmts.beans.Review
import com.example.tmts.beans.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.Date
import java.util.Locale

object FirebaseInteraction {
    var mDbRef = FirebaseDatabase.getInstance().getReference()
    val mStRef = FirebaseStorage.getInstance().getReference()
    var mAuth = FirebaseAuth.getInstance()
    val user = mAuth.currentUser!!
    val userRef = mDbRef.child("users").child(user.uid)
    val followingSeriesRef = userRef.child("following_series")
    val followingMoviesRef = userRef.child("following_movies")
    val watchedMoviesRef = userRef.child("watched_movies")
    val moviesRef = mDbRef.child("shows").child("movies")
    val seriesRef = mDbRef.child("shows").child("series")
    val reviewsRef = mDbRef.child("reviews")


    fun getLoggedUser(onSuccess: ((User) -> Unit), onFailure: (String) -> Unit){
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userId = snapshot.key
                val username = snapshot.child("name").getValue(String::class.java)
                val email = snapshot.child("email").getValue(String::class.java)
                if (userId != null && username != null && email != null) {
                    val result = User(userId, username, email)
                    onSuccess(result)
                } else {
                    onFailure("Username not found for user ID: ${user.uid}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure(error.message)
            }
        })
    }

    fun getUsername(userId: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        val usernameRef = mDbRef.child("users").child(userId).child("name")

        usernameRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.getValue(String::class.java)
                if (username != null) {
                    onSuccess(username)
                } else {
                    onFailure("Username not found for user ID: ${user.uid}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure(error.message)
            }
        })
    }

    fun getUserInfo(userId: String, onSuccess: (User) -> Unit, onFailure: (String) -> Unit) {
        val userRef = mDbRef.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("GET USER INFO", "Info for user $userId")
                val username = snapshot.child("name").getValue(String::class.java)
                val email = snapshot.child("email").getValue(String::class.java)
                if (username != null && email != null) {
                    val result = User(userId, username, email)
                    onSuccess(result)
                } else {
                    onFailure("Username not found for user ID: ${user.uid}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure(error.message)
            }
        })
    }

    fun getReviewsForMovie(
        movieId: String,
        onSuccess: (List<Review>) -> Unit,
        onError: (String) -> Unit
    ) {
        val MoviesReviewsRef = moviesRef.child(movieId).child("reviews")

        MoviesReviewsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reviews = mutableListOf<Review>()
                val reviewIds = mutableListOf<String>()

                // Step 1: Collect all review IDs for the movie
                for (reviewSnapshot in snapshot.children) {
                    val reviewId = reviewSnapshot.key
                    Log.d("reviewId", "${reviewId}")
                    reviewId?.let {
                        reviewIds.add(it)
                    }
                }

                // Step 2: Fetch details for each review using its ID
                reviewIds.forEach { id ->
                    val reviewDetailsRef = reviewsRef.child(id)

                    reviewDetailsRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(reviewSnapshot: DataSnapshot) {

                            val usId = reviewSnapshot.child("idUser").getValue(String::class.java)
                            val movId = reviewSnapshot.child("idShow").getValue(String::class.java)
                            val comment = reviewSnapshot.child("comment").getValue(String::class.java)
                            val date = reviewSnapshot.child("date").getValue(String::class.java)

                            Log.d("Boh", "${usId} ${movId} ${comment} ${date}")

                            val r = Review(usId, movId, comment, date)
                            Log.d("r", "${r}")
                            reviews.add(r)

                            // Check if this is the last review being fetched
                            if (reviews.size == reviewIds.size) {

                                onSuccess(reviews)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            onError(error.message)
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        })
    }

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

    fun getUnorderedFollowingMovies(callback: (List<String>) -> Unit) {
        followingMoviesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val movies = mutableListOf<String>()

                snapshot.children.forEach { child ->
                    val movieId = child.key
                    if (movieId != null) {
                        movies.add(movieId)
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

    fun removeFollowerFromMovie(movieId: Int, onSuccess: (() -> Unit)? = null) {
        val movieRef = moviesRef.child(movieId.toString())
        val followersRef = movieRef.child("followers")

        followersRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val followersList = task.result?.children?.map { it.value.toString() }?.toMutableList() ?: mutableListOf()

                if (followersList.contains(user.uid)) {
                    followersList.remove(user.uid)
                    followersRef.setValue(followersList).addOnCompleteListener { setValueTask ->
                        if (setValueTask.isSuccessful) {
                            onSuccess?.invoke()
                            Log.d("Firebase", "Utente rimosso con successo dai follower del film")
                        } else {
                            Log.e("Firebase", "Errore durante la rimozione dell'utente dai follower del film", setValueTask.exception)
                        }
                    }
                } else {
                    onSuccess?.invoke()
                    Log.d("Firebase", "Utente non presente nella lista dei follower")
                }
            } else {
                Log.e("Firebase", "Errore durante il recupero della lista dei follower", task.exception)
            }
        }
    }

    fun removeFollowerFromSeries(seriesId: Int, onSuccess: (() -> Unit)? = null) {
        val seriesRef = seriesRef.child(seriesId.toString())
        val followersRef = seriesRef.child("followers")

        followersRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val followersList = task.result?.children?.map { it.value.toString() }?.toMutableList() ?: mutableListOf()

                if (followersList.contains(user.uid)) {
                    followersList.remove(user.uid)
                    followersRef.setValue(followersList).addOnCompleteListener { setValueTask ->
                        if (setValueTask.isSuccessful) {
                            onSuccess?.invoke()
                            Log.d("Firebase", "Utente rimosso con successo dai follower della serie")
                        } else {
                            Log.e("Firebase", "Errore durante la rimozione dell'utente dai follower della serie", setValueTask.exception)
                        }
                    }
                } else {
                    onSuccess?.invoke()
                    Log.d("Firebase", "Utente non presente nella lista dei follower")
                }
            } else {
                Log.e("Firebase", "Errore durante il recupero della lista dei follower", task.exception)
            }
        }
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    fun addReviewToMovie(
        movieId: String,
        comment: String,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null
    ) {
        val movieReviewsRef = moviesRef.child(movieId).child("reviews")

        // Generate a unique ID for the new review
        val newReviewRef = reviewsRef.push()
        val reviewId = newReviewRef.key

        val currentDate = getCurrentDateTime()

        if (reviewId != null) {
            val review = Review(
                idUser = user.uid,
                idShow = movieId,
                comment = comment,
                date = currentDate
            )

            // Save the review to the "reviews" node
            newReviewRef.setValue(review).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Add the review ID to the movie's reviews list
                    movieReviewsRef.child(reviewId).setValue(true).addOnCompleteListener { task2 ->
                        if (task2.isSuccessful) {
                            onSuccess?.invoke()
                        } else {
                            onFailure?.invoke(task2.exception ?: Exception("Failed to add review ID to movie's reviews list"))
                        }
                    }
                } else {
                    onFailure?.invoke(task.exception ?: Exception("Failed to save review"))
                }
            }
        } else {
            onFailure?.invoke(Exception("Failed to generate review ID"))
        }
    }

    fun addFollowerToSeries(seriesId: Int, onSuccess: (() -> Unit)? = null) {
        val seriesRef = seriesRef.child(seriesId.toString())
        val followersRef = seriesRef.child("followers")

        followersRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val followersList = task.result?.children?.map { it.value.toString() }?.toMutableList() ?: mutableListOf()

                if (!followersList.contains(user.uid)) {
                    followersList.add(user.uid)
                    followersRef.setValue(followersList).addOnCompleteListener { setValueTask ->
                        if (setValueTask.isSuccessful) {
                            onSuccess?.invoke()
                            Log.d("Firebase", "Utente aggiunto con successo ai follower della serie")
                        } else {
                            Log.e("Firebase", "Errore durante l'aggiunta dell'utente ai follower della serie", setValueTask.exception)
                        }
                    }
                } else {
                    onSuccess?.invoke()
                    Log.d("Firebase", "Utente già presente nella lista dei follower")
                }
            } else {
                Log.e("Firebase", "Errore durante il recupero della lista dei follower", task.exception)
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

    fun addFollowerToMovie(movieId: Int, onSuccess: (() -> Unit)? = null) {
        val movieRef = moviesRef.child(movieId.toString())
        val followersRef = movieRef.child("followers")

        followersRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val followersList = task.result?.children?.map { it.value.toString() }?.toMutableList() ?: mutableListOf()

                if (!followersList.contains(user.uid)) {
                    followersList.add(user.uid)
                    followersRef.setValue(followersList).addOnCompleteListener { setValueTask ->
                        if (setValueTask.isSuccessful) {
                            onSuccess?.invoke()
                            Log.d("Firebase", "Utente aggiunto con successo ai follower del film")
                        } else {
                            Log.e("Firebase", "Errore durante l'aggiunta dell'utente ai follower del film", setValueTask.exception)
                        }
                    }
                } else {
                    onSuccess?.invoke()
                    Log.d("Firebase", "Utente già presente nella lista dei follower")
                }
            } else {
                Log.e("Firebase", "Errore durante il recupero della lista dei follower", task.exception)
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

    fun getMovieFollowers(movieId: Int, callback: ((List<String>) -> Unit)) {

        val movieFollowersRef = mDbRef.child("shows").child("movies").child(movieId.toString()).child("followers")
        movieFollowersRef.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val users = mutableListOf<String>()
                snapshot.children.forEach { child ->
                    val userId = child.value.toString()
                    users.add(userId)
                }
                callback(users)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Errore nel recupero dei dati: ${error.message}")
                // Chiamata della callback con lista vuota in caso di errore
                callback(emptyList())
            }
        })
    }

    fun getSerieFollowers(serieId: Int, callback: ((List<String>) -> Unit)) {

        val serieFollowersRef = mDbRef.child("shows").child("series").child(serieId.toString()).child("followers")
        serieFollowersRef.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val users = mutableListOf<String>()
                snapshot.children.forEach { child ->
                    val userId = child.value.toString()
                    users.add(userId)
                }
                callback(users)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Errore nel recupero dei dati: ${error.message}")
                // Chiamata della callback con lista vuota in caso di errore
                callback(emptyList())
            }
        })
    }

    fun getExploreShows(callback: (List<Pair<String, String>>) -> Unit) {
        followingMoviesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val movies = mutableListOf<Pair<String, String>>()

                snapshot.children.forEach { child ->
                    val movieId = child.key
                    if (movieId != null) {
                        movies.add(Pair("MOV", movieId))
                    }
                }

                followingSeriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val series = mutableListOf<Pair<String, String>>()

                        snapshot.children.forEach { child ->
                            val seriesId = child.key
                            if (seriesId != null) {
                                series.add(Pair("SER", seriesId))
                            }
                        }
                        callback((series + movies).shuffled())
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println("Errore nel recupero dei dati: ${error.message}")
                        callback(emptyList())
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Errore nel recupero dei dati: ${error.message}")
                // Chiamata della callback con lista vuota in caso di errore
                callback(emptyList())
            }
        })
    }

    fun getUserProfileImageRef(
        userId: String,
        onSuccess: (StorageReference) -> Unit,
        onFailure: (String) -> Unit
    ){
        val imageRef = mStRef.child("users/$userId/ProfileImage.png")
        if (imageRef != null) {
            onSuccess(imageRef)
        } else {
            onFailure("Image not found")
        }
    }

    fun onError(){
        Log.e("Firebase", "Something went wrong")
    }
}