package com.example.nsamovie.network

import com.example.nsamovie.network.model.TMDBGenreResponse
import com.example.nsamovie.network.model.TMDBMovieResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.Response

interface TMDBApiService {

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("page") page: Int = 1,
        @Query("region") region: String? = null
    ): TMDBMovieResponse

    @GET("movie/{movie_id}/recommendations")
    suspend fun getRecommendedMovies(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("page") page: Int = 1
    ): TMDBMovieResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String,
        @Query("page") page: Int = 1
    ): TMDBMovieResponse

    @GET("genre/movie/list")
    suspend fun getMovieGenres(
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ): TMDBGenreResponse

    @GET("discover/movie")
    suspend fun getMoviesBasedOnLocale(
        @Query("region") region: String?,
        @Query("language") language: String,
        @Query("api_key") apiKey: String
    ): Response<TMDBMovieResponse>
}
