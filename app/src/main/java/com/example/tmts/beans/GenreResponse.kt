package com.example.tmts.beans

import com.google.gson.annotations.SerializedName

data class GenreResponse (
    @SerializedName("genres") val results: List<Genre>
)