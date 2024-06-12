package com.example.tmts.beans

import com.google.gson.annotations.SerializedName

data class Media(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("original_name") val original_name: String,
    @SerializedName("poster_path") val posterPath: String?,
)