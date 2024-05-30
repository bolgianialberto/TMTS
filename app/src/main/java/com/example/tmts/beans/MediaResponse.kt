package com.example.tmts.beans

import com.example.tmts.beans.Media
import com.google.gson.annotations.SerializedName

data class MediaResponse(
    @SerializedName("results") val results: List<Media>
)