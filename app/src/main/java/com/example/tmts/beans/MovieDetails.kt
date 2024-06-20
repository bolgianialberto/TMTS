package com.example.tmts.beans

import com.google.gson.annotations.SerializedName

data class MovieDetails (
    @SerializedName("id") val id: Int,
    @SerializedName("original_title") val title: String,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("genres") val genres: List<Genre>,
    @SerializedName("overview") val overview: String,
    @SerializedName("release_date") val releaseDate: String,
    @SerializedName("runtime") val runtime: Int,
    @SerializedName("original_language") val original_language: String,
    @SerializedName("origin_country") val origin_country: List<String>
)