package com.example.tmts.beans

import com.example.tmts.beans.Genre
import com.google.gson.annotations.SerializedName
import java.io.Serial

data class SerieDetails (
    @SerializedName("id") val id: Int,
    @SerializedName("original_name") val title: String,
    @SerializedName("original_language") val original_language: String,
    @SerializedName("origin_country") val origin_country: List<String>,
    @SerializedName("poster_path") val posterPath: String,
    @SerializedName("backdrop_path") val backdropPath: String,
    @SerializedName("genres") val genres: List<Genre>,
    @SerializedName("overview") val overview: String,
    @SerializedName("first_air_date") val first_air_date: String,
    @SerializedName("last_air_date") val last_air_date: String,
    @SerializedName("number_of_episodes") val total_number_of_episodes: Int,
    @SerializedName("number_of_seasons") val number_of_seasons: Int,
    @SerializedName("networks") val networks: List<Network>,
    )
