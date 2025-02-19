package com.example.nsamovie.data.repository


import com.example.nsamovie.data.local_db.MovieDao
import com.example.nsamovie.data.model.Movie
import com.example.nsamovie.network.TMDBApiService

class MovieRepository(private val movieDao: MovieDao, private val apiService: TMDBApiService) {

    suspend fun getRecommendedMovies(): List<Movie> {
        return apiService.getRecommendedMovies()
    }

    suspend fun getSearchResults(query: String): List<Movie> {
        return apiService.searchMovies(query)
    }

    suspend fun insertMovie(movie: Movie) {
        movieDao.insertMovie(movie)
    }

    suspend fun deleteMovie(movie: Movie) {
        movieDao.deleteMovie(movie)
    }
}
