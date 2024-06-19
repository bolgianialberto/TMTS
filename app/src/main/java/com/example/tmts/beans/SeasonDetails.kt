package com.example.tmts.beans

import com.google.gson.annotations.SerializedName

data class SeasonDetails (
    @SerializedName("id") val id: String,
    @SerializedName("name") val title: String,
    @SerializedName("air_date") val air_date: String,
    @SerializedName("episodes") val episodes: List<EpisodeDetails>,
    @SerializedName("overview") val overview: String,
    @SerializedName("season_number") val season_number: Int,
    var serieId: Int = 0,
    var serieName: String? = null
){
    val number_of_episodes: Int
        get() = episodes.size
}