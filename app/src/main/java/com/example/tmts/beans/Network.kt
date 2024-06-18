package com.example.tmts.beans

import com.google.gson.annotations.SerializedName

data class Network(
    @SerializedName("logo_path") val logo_path: String,
    @SerializedName("name") val name: String,
)
