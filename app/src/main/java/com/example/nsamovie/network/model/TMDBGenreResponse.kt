package com.example.nsamovie.network.model

import com.google.gson.annotations.SerializedName

data class TMDBGenreResponse(
    @SerializedName("genres") val genres: List<TMDBGenre>
)
