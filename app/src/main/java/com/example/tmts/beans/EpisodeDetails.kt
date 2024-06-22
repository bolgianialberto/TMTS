package com.example.tmts.beans

import com.google.gson.annotations.SerializedName

data class EpisodeDetails(
    @SerializedName("id") val id: Int,
    @SerializedName("air_date") val air_date: String,
    @SerializedName("name") val title: String,
    @SerializedName("overview") val overview: String,
    @SerializedName("runtime") val runtime: Int,
    @SerializedName("season_number") val season_number: Int,
    @SerializedName("episode_number") val episode_number: Int,
    @SerializedName("still_path") val posterPath: String,
    @SerializedName("guest_stars") val stars: List<CastMember>,
    var seriePosterPath: String? = "",
    var serieId: Int = 0,
    var serieName: String? = null
)
