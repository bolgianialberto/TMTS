package com.example.tmts.beans

import com.google.gson.annotations.SerializedName

data class Provider(
    @SerializedName("logo_path") val logo_path: String,
    @SerializedName("provider_name") val name: String,
)
