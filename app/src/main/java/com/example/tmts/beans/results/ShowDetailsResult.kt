package com.example.tmts.beans.results

import com.example.tmts.beans.MovieDetails
import com.example.tmts.beans.SerieDetails
import com.example.tmts.beans.User

data class ShowDetailsResult(
    val showTypeId: String,
    val movieDetails: MovieDetails?,
    val serieDetails: SerieDetails?,
    val retrievedUsers: ArrayList<String> = ArrayList(),
    val loadedUsers: ArrayList<User> = ArrayList()
)