package com.example.nsamovie.network.model

import com.google.gson.annotations.SerializedName

data class TMDBMovieResponse(
    @SerializedName("results") val movies: List<TMDBMovie> // ✅ Changed Movie → TMDBMovie
)
