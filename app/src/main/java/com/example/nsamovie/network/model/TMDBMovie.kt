package com.example.nsamovie.network.model

import com.google.gson.annotations.SerializedName

data class TMDBMovie(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("release_date") val releaseDate: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("overview") val overview: String
)
