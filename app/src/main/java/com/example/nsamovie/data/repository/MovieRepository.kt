package com.example.nsamovie.data.repository


import com.example.nsamovie.data.local_db.MovieDao
import com.example.nsamovie.data.model.Movie
import com.example.nsamovie.data.remote.MovieApiService

class MovieRepository(private val movieDao: MovieDao, private val apiService: MovieApiService) {

    suspend fun getRecommendedMovies(): List<Movie> {
        return apiService.getRecommendedMovies()
    }

    suspend fun getSearchResults(query: String): List<Movie> {
        return apiService.searchMovies(query)
    }

    suspend fun insert(movie: Movie) {
        movieDao.insert(movie)
    }

    suspend fun delete(movie: Movie) {
        movieDao.delete(movie)
    }
}
