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

    suspend fun getGenres(currentLanguage: String): List<TMDBGenre>? {
        val response: TMDBGenreResponse = apiService.getMovieGenres(apiKey, language = currentLanguage)
        genres = response.genres
        return genres
    }

    fun getGenreNameById(id: Int): String {
        return genres.find { it.id == id }?.name ?: "Unknown"
    }

    fun getFavoriteMovies(): LiveData<List<Movie>> = movieDao.getFavoriteMovies()

    suspend fun getPopularMovies(page: Int = 1): List<Movie> {
        return try {
            val languageCode = getCurrentLanguage()
            val region = getRegionCode() // Get region based on locale

            Log.d(TAG, "Fetching movies with language: $languageCode, region: $region")

            // Fetch movies with both language and region
            val response = apiService.getPopularMovies(
                apiKey = apiKey,
                language = languageCode,
                page = page,
                region = region
            )

            // Pre-fetch genres in the correct language
            getGenres(languageCode)

            val moviesFromApi = response.movies.map { tmdbMovie ->
                val existingMovie = movieDao.getMovieById(tmdbMovie.id)
                Log.d(TAG, "Processing movie: ${tmdbMovie.title} (ID: ${tmdbMovie.id} ")

                convertTMDBMovieToMovie(
                    tmdbMovie = tmdbMovie,
                    isFavorite = existingMovie?.favorites ?: false
                )
            }

            // Batch insert all movies
            movieDao.insertMovies(moviesFromApi)

            Log.d(TAG, "Successfully processed ${moviesFromApi.size} movies in $languageCode")
            moviesFromApi
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching popular movies", e)
            movieDao.getAllMoviesAsList()
        }
    }

    private fun getRegionCode(): String {
        val locale = Locale.getDefault()
        return locale.country.uppercase()
    }

    suspend fun updateMovie(movie: Movie) = movieDao.updateMovie(movie)

    suspend fun getMovieById(movieId: Int): Movie? = movieDao.getMovieById(movieId)

    suspend fun getRecommendedMovies(movieId: Int, currentLanguage: String): TMDBMovieResponse =
        apiService.getRecommendedMovies(movieId, apiKey, language = currentLanguage)

    suspend fun searchMovies(query: String, currentLanguage: String): TMDBMovieResponse {
        val response = apiService.searchMovies(apiKey, query, language = currentLanguage)
        val movies = response.movies.map { tmdbMovie ->
            convertTMDBMovieToMovie(tmdbMovie)
        }
        movies.forEach { movie ->
            movieDao.insertMovie(movie)
        }
        return response
    }

    fun convertTMDBMovieToMovie(tmdbMovie: TMDBMovie, isFavorite: Boolean = false): Movie {
        val baseImageUrl = "https://image.tmdb.org/t/p/w500"
        val posterPath = if (!tmdbMovie.posterPath.isNullOrEmpty()) {
            if (tmdbMovie.posterPath.startsWith("/")) {
                baseImageUrl + tmdbMovie.posterPath
            } else {
                "$baseImageUrl/${tmdbMovie.posterPath}"
            }
        } else {
            ""
        }

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

    suspend fun insertMovies(movies: List<Movie>) {
        movies.forEach {
            movieDao.insertMovie(it)
        }
    }

    private fun getCurrentLanguage(): String {
        val locale = Locale.getDefault()
        return when (locale.language) {
            "iw", "he" -> "he-IL"  // Hebrew
            "en" -> "${locale.language}-${locale.country}"  // e.g., en-US
            else -> locale.toLanguageTag()
        }
    }
}