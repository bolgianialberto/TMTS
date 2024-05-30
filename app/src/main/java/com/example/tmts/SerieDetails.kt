package com.example.tmts

import com.google.gson.annotations.SerializedName

data class SerieDetails (
    @SerializedName("id") val id: Int,
    @SerializedName("original_name") val title: String,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("genres") val genres: List<Genre>,
    @SerializedName("overview") val overview: String,
    @SerializedName("first_air_date") val first_air_date: String,
    @SerializedName("last_air_date") val last_air_date: String,
    @SerializedName("number_of_episodes") val number_of_episodes: Int,
    @SerializedName("number_of_seasons") val number_of_seasons: Int,
    )
