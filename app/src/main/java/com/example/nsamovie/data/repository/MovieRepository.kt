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

    fun getAllMovies(): LiveData<List<Movie>> = movieDao.getAllMovies()

    fun getFavoriteMovies(): LiveData<List<Movie>> = movieDao.getFavoriteMovies()

    suspend fun getPopularMovies(
        language: String = "en-US",
        page: Int = 1,
        region: String? = null
    ): List<Movie> {
        return try {
            val response = apiService.getPopularMovies(apiKey, language, page, region)
            Log.d(TAG, "API Response: ${response.movies.size} movies for region: $region, language: $language")
            Log.d(TAG, "Raw API Response: ${response.movies}")
            val moviesFromApi = response.movies.map { tmdbMovie ->
                Log.d(TAG, "Movie Title: ${tmdbMovie.title}, Region: $region")
                val existingMovie = movieDao.getMovieById(tmdbMovie.id)
                convertTMDBMovieToMovie(tmdbMovie, existingMovie?.favorites ?: false)
            }
            moviesFromApi.forEach { movie ->
                movieDao.insertMovie(movie)
            }
            moviesFromApi
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching movies: ${e.message}")
            movieDao.getAllMoviesAsList()
        }
    }

    suspend fun getMoviesBasedOnLocale(): List<TMDBMovie>? {
        val currentLocale = Locale.getDefault()
        val languageCode = currentLocale.language // e.g., "he", "en"
        val regionCode = currentLocale.country     // e.g., "IL", "US"

        Log.d(TAG, "Fetching movies for region: $regionCode, language: $languageCode")

        return try {
            // Use getPopularMovies with locale parameters
            val response = apiService.getPopularMovies(apiKey, languageCode, page = 1, region = regionCode)
            response.movies
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching movies by locale: ${e.message}")
            null
        }
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

    // Make this conversion function public so it can be used by the ViewModel.
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
        Log.d(TAG, "Converting movie: ${tmdbMovie.title}")
        Log.d(TAG, "Original poster path: ${tmdbMovie.posterPath}")
        Log.d(TAG, "Final poster path: $posterPath")
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
}
