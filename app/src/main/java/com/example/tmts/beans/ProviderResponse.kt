package com.example.tmts.beans

import com.google.gson.annotations.SerializedName

data class ProviderResponse (
    @SerializedName("results") val results: Map<String, CountryData>
)