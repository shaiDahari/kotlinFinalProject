package com.example.nsamovie.network.model

import com.google.gson.annotations.SerializedName
import com.example.nsamovie.data.model.Movie

data class TMDBMovieResponse(
    @SerializedName("results") val movies: List<Movie> // The list of movies is mapped here
)
