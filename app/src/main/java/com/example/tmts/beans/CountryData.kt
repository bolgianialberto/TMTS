package com.example.tmts.beans

import com.google.gson.annotations.SerializedName

data class CountryData(
    @SerializedName("link") val link: String,
    @SerializedName("flatrate") val flatrate: List<Provider>
)
