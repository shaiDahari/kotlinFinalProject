package com.example.nsamovie.network

import com.example.nsamovie.network.model.TMDBMovieResponse

import retrofit2.http.GET
import retrofit2.http.Query

interface TMDBApiService {
    @GET("movie/popular")
    suspend fun

            getPopularMovies(
        @Query("api_key")apiKey:String,
        @Query("language")language:String="en-US",
        @Query("page")page:Int=1
    ):TMDBMovieResponse
}
