package com.example.nsamovie.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.nsamovie.R
import com.example.nsamovie.data.local_db.MovieDao
import com.example.nsamovie.data.model.Movie
import com.example.nsamovie.network.TMDBApiService
import com.example.nsamovie.network.model.TMDBGenre
import com.example.nsamovie.network.model.TMDBGenreResponse
import com.example.nsamovie.network.model.TMDBMovie
import com.example.nsamovie.network.model.TMDBMovieResponse
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Locale
import android.util.Log

@Singleton
class MovieRepository @Inject constructor(
    private val movieDao: MovieDao,
    private val apiService: TMDBApiService,
    private val context: Context
) {
    private val apiKey: String = context.getString(R.string.api_key)
    private var genres: List<TMDBGenre> = emptyList()
    private val TAG = "MovieRepository"

    private fun getCurrentLanguage(): String {
        val locale = Locale.getDefault()
        return if (locale.language == "iw") "he-IL" else locale.toLanguageTag() // TMDB uses "he-IL"
    }

    private fun getCurrentRegion(): String? {
        return Locale.getDefault().country // e.g., "IL", "US"
    }

    suspend fun getGenres(): List<TMDBGenre>? {
        val language = getCurrentLanguage()
        val response: TMDBGenreResponse = apiService.getMovieGenres(apiKey, language)
        genres = response.genres
        return genres
    }

    fun getGenreNameById(id: Int): String {
        return genres.find { it.id == id }?.name ?: "Unknown"
    }

    fun getAllMovies(): LiveData<List<Movie>> = movieDao.getAllMovies()

    fun getFavoriteMovies(): LiveData<List<Movie>> = movieDao.getFavoriteMovies()

    suspend fun getPopularMovies(page: Int = 1): List<Movie> {
        val language = getCurrentLanguage()
        val region = getCurrentRegion()
        return try {
            val response = apiService.getPopularMovies(apiKey, language, page, region)
            Log.d(TAG, "API Response: ${response.movies.size} movies for region: $region, language: $language")
            val moviesFromApi = response.movies.map { tmdbMovie ->
                convertTMDBMovieToMovie(tmdbMovie)
            }
            movieDao.insertMovies(moviesFromApi)
            moviesFromApi
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching movies: ${e.message}")
            movieDao.getAllMoviesAsList()
        }
    }

    suspend fun getMoviesBasedOnLocale(): List<TMDBMovie>? {
        val language = getCurrentLanguage()
        val region = getCurrentRegion()

        Log.d(TAG, "Fetching localized movies for region: $region, language: $language")

        return try {
            val response = apiService.getMoviesBasedOnLocale(region, language, apiKey)
            if (response.isSuccessful) {
                movieDao.insertMovies(response.body()?.movies?.map { convertTMDBMovieToMovie(it) } ?: emptyList())
                response.body()?.movies
            } else {
                Log.e(TAG, "Failed to fetch localized movies: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching localized movies: ${e.message}")
            null
        }
    }

    suspend fun getRecommendedMovies(movieId: Int): TMDBMovieResponse =
        apiService.getRecommendedMovies(movieId, apiKey, language = getCurrentLanguage())

    suspend fun searchMovies(query: String): TMDBMovieResponse {
        val language = getCurrentLanguage()
        val response = apiService.searchMovies(apiKey, query, language)
        val movies = response.movies.map { tmdbMovie ->
            convertTMDBMovieToMovie(tmdbMovie)
        }
        movieDao.insertMovies(movies)
        return response
    }

    fun convertTMDBMovieToMovie(tmdbMovie: TMDBMovie, isFavorite: Boolean = false): Movie {
        val baseImageUrl = "https://image.tmdb.org/t/p/w500"
        val posterPath = tmdbMovie.posterPath?.let {
            if (it.startsWith("/")) baseImageUrl + it else "$baseImageUrl/$it"
        } ?: ""

        Log.d(TAG, "Converting movie: ${tmdbMovie.title}")
        return Movie(
            id = tmdbMovie.id,
            title = tmdbMovie.title,
            releaseDate = tmdbMovie.releaseDate ?: "N/A",
            posterPath = posterPath,
            rating = tmdbMovie.voteAverage ?: 0.0,
            genre = tmdbMovie.genreIds.map { getGenreNameById(it) },
            overview = tmdbMovie.overview ?: "",
            favorites = isFavorite
        )
    }

    suspend fun updateMovie(movie: Movie) = movieDao.updateMovie(movie)

    suspend fun getMovieById(movieId: Int): Movie? = movieDao.getMovieById(movieId)

    suspend fun insertMovies(movies: List<Movie>) {
        movieDao.insertMovies(movies)
    }
}
