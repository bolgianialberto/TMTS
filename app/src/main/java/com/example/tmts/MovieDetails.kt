package com.example.tmts

import com.google.gson.annotations.SerializedName

data class MovieDetails(
    @SerializedName("id") val id: Int,
    @SerializedName("original_title") val title: String,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("genres") val genres: List<Genre>,
    @SerializedName("overview") val overview: String,
    @SerializedName("release_date") val releaseDate: String,
    @SerializedName("runtime") val runtime: Int
)