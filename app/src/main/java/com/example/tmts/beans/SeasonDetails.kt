package com.example.tmts.beans

import com.google.gson.annotations.SerializedName

data class SeasonDetails (
    @SerializedName("id") val id: String,
    @SerializedName("name") val title: String,
    @SerializedName("air_date") val air_date: String,
    @SerializedName("episodes") val episodes: List<EpisodeDetails>,
    @SerializedName("overview") val overview: String,
){
    val number_of_episodes: Int
        get() = episodes.size
}