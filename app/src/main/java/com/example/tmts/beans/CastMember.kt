package com.example.tmts.beans

import com.google.gson.annotations.SerializedName

data class CastMember(
    @SerializedName("character") val character: String,
    @SerializedName("name") val actor_name: String,
    @SerializedName("profile_path") val profile_path: String,
)
