package com.example.nsamovie.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.nsamovie.R
import com.example.nsamovie.data.local_db.MovieDao
import com.example.nsamovie.data.model.Movie
import com.example.nsamovie.network.TMDBApiService
import com.example.nsamovie.network.model.TMDBGenre
import com.example.nsamovie.network.model.TMDBGenreResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepository @Inject constructor(
    private val movieDao: MovieDao,
    private val apiService: TMDBApiService,
    private val context: Context
) {
    private val apiKey: String = context.getString(R.string.api_key)
    private var genres: List<TMDBGenre> = emptyList()

    suspend fun getGenres() {
        val response: TMDBGenreResponse = apiService.getMovieGenres(apiKey)
        genres = response.genres
    }

    fun getGenreNameById(id: Int): String {
        return genres.find { it.id == id }?.name ?: "Unknown"
    }

    fun getAllMovies(): LiveData<List<Movie>> = movieDao.getAllMovies()

    fun getFavoriteMovies(): LiveData<List<Movie>> = movieDao.getFavoriteMovies()

    suspend fun insertMovie(movie: Movie) = movieDao.insertMovie(movie)

    suspend fun updateMovie(movie: Movie) = movieDao.updateMovie(movie)

    suspend fun getMovieById(movieId: Int): Movie? = movieDao.getMovieById(movieId)

    suspend fun getRecommendedMovies(movieId: Int) =
        apiService.getRecommendedMovies(movieId, apiKey)

    suspend fun searchMovies(query: String) =
        apiService.searchMovies(apiKey, query)

    suspend fun getPopularMovies(language: String = "en-US", page: Int = 1) =
        apiService.getPopularMovies(apiKey, language, page)
}
