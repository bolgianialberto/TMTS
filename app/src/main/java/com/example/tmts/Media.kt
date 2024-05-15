package com.example.tmts

import com.google.gson.annotations.SerializedName

data class Media(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("poster_path") val posterPath: String?,
)