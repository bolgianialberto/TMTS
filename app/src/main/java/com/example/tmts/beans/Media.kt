package com.example.tmts.beans

import com.google.gson.annotations.SerializedName

data class Media(
    @SerializedName("id") override val id: Int,
    @SerializedName("title") override val title: String,
    @SerializedName("original_name")  val original_name: String,
    @SerializedName("poster_path") override val posterPath: String?,
) : MediaDetails