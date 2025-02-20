package com.example.nsamovie.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.nsamovie.R
import com.example.nsamovie.data.local_db.MovieDao
import com.example.nsamovie.data.model.Movie
import com.example.nsamovie.network.TMDBApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepository @Inject constructor(
    private val movieDao: MovieDao,
    private val apiService: TMDBApiService,
    private val context: Context // Injecting context to access resources
) {
    private val apiKey: String = context.getString(R.string.api_key) // Access API key from resources

    // Fetch all movies from local database
    fun getAllMovies(): LiveData<List<Movie>> = movieDao.getAllMovies()

    // Fetch favorite movies from local database
    fun getFavoriteMovies(): LiveData<List<Movie>> = movieDao.getFavoriteMovies()

    // Insert a movie into the local database
    suspend fun insertMovie(movie: Movie) = movieDao.insertMovie(movie)

    // Delete a movie from the local database
    suspend fun deleteMovie(movie: Movie) = movieDao.deleteMovie(movie)

    // Update a movie in the local database
    suspend fun updateMovie(movie: Movie) = movieDao.updateMovie(movie)

    // Fetch a movie by its ID from the local database
    suspend fun getMovieById(movieId: Int): Movie? = movieDao.getMovieById(movieId)

    // Fetch recommended movies from TMDB API
    suspend fun getRecommendedMovies(movieId: Int) =
        apiService.getRecommendedMovies(movieId, apiKey)

    // Search movies by a query string from TMDB API
    suspend fun searchMovies(query: String) =
        apiService.searchMovies(apiKey, query)

    // Fetch popular movies from TMDB API
    suspend fun getPopularMovies(page: Int = 1) =
        apiService.getPopularMovies(apiKey, "en-US", page)
}
