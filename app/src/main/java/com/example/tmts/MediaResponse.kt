package com.example.tmts

import com.google.gson.annotations.SerializedName

data class MediaResponse(
    @SerializedName("results") val results: List<Media>
)