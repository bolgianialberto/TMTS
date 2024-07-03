package com.example.tmts.beans

import com.google.gson.annotations.SerializedName

data class CastResponse(
    @SerializedName("cast") val results: List<CastMember>
)
