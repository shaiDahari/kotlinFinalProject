package com.example.nsamovie.network.model

import com.google.gson.annotations.SerializedName

data class TMDBGenre(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)
