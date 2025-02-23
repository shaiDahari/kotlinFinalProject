package com.example.nsamovie.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nsamovie.data.model.Movie
import com.example.nsamovie.network.model.TMDBMovie
import com.example.nsamovie.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    val favoriteMovies: LiveData<List<Movie>> = repository.getFavoriteMovies()

    private val _movieList = MutableLiveData<List<Movie>>()
    val movieList: LiveData<List<Movie>> = _movieList

    private val _highRatedMovies = MutableLiveData<List<Movie>>()
    val highRatedMovies: LiveData<List<Movie>> = _highRatedMovies

    private val _newReleasesMovies = MutableLiveData<List<Movie>>()
    val newReleasesMovies: LiveData<List<Movie>> = _newReleasesMovies

    private val genreMap = mutableMapOf<Int, String>()

    init {
        fetchGenres()
    }

    private fun fetchGenres() {
        viewModelScope.launch {
            try {
                val genres = repository.getGenres(getCurrentLanguage())
                genres?.forEach { genre ->
                    genreMap[genre.id] = genre.name
                }
            } catch (e: Exception) {
                Log.e("MoviesViewModel", "Error fetching genres: ${e.localizedMessage}")
            }
        }
    }

    private fun getCurrentLanguage(): String {
        Log.e("MoviesViewModel", "language is : {${Locale.getDefault().language}}")
        return Locale.getDefault().language

    }

    fun getGenreNameById(id: Int): String {
        return genreMap[id] ?: "Unknown Genre"
    }

    fun fetchMovies(language: String = getCurrentLanguage(), page: Int = 1) {
        viewModelScope.launch {
            try {
                val movies = repository.getPopularMovies(language, page)
                _movieList.postValue(movies)
                _highRatedMovies.postValue(movies.sortedByDescending { it.rating })
                _newReleasesMovies.postValue(movies.sortedByDescending { it.releaseDate })
                repository.insertMovies(movies) // Insert into Room
            } catch (e: Exception) {
                Log.e("MoviesViewModel", "Error fetching movies: ${e.localizedMessage}")
                _movieList.postValue(emptyList())
            }
        }
    }

    fun searchMovies(query: String) {
        viewModelScope.launch {
            try {
                val response = repository.searchMovies(query, getCurrentLanguage())
                val movies = response.movies.map { convertTMDBMovieToMovie(it) }
                _movieList.postValue(movies)
                repository.insertMovies(movies) // Insert into Room
            } catch (e: Exception) {
                Log.e("MoviesViewModel", "Error searching movies: ${e.localizedMessage}")
                _movieList.postValue(emptyList())
            }
        }
    }

    fun searchMoviesByYearRange(startYear: Int, endYear: Int) {
        viewModelScope.launch {
            try {
                val allMovies = mutableListOf<Movie>()
                for (year in startYear..endYear) {
                    val response = repository.searchMovies(year.toString(), getCurrentLanguage())
                    val moviesFromYear = response.movies
                        .filter { movie ->
                            val movieYear = movie.releaseDate?.split("-")?.firstOrNull()?.toIntOrNull()
                            movieYear != null && movieYear in startYear..endYear
                        }
                        .map { convertTMDBMovieToMovie(it) }
                    allMovies.addAll(moviesFromYear)
                }
                val uniqueMovies = allMovies.distinctBy { it.id }
                _movieList.postValue(uniqueMovies)
                repository.insertMovies(uniqueMovies) // Insert into Room
            } catch (e: Exception) {
                Log.e("MoviesViewModel", "Error searching movies by year range: ${e.localizedMessage}")
                _movieList.postValue(emptyList())
            }
        }
    }

    fun searchMoviesByRegionAndLanguage(region: String, language: String) {
        viewModelScope.launch {
            try {
                val movies = repository.getPopularMovies(
                    language = language,
                    page = 1,
                    region = region
                )
                _movieList.postValue(movies)
                repository.insertMovies(movies) // Insert into Room
            } catch (e: Exception) {
                Log.e("MoviesViewModel", "Error searching movies by region and language: ${e.localizedMessage}")
                _movieList.postValue(emptyList())
            }
        }
    }



    suspend fun getMovieById(movieId: Int): Movie? {
        return try {
            repository.getMovieById(movieId)
        } catch (e: Exception) {
            Log.e("MoviesViewModel", "Error fetching movie by ID: ${e.localizedMessage}")
            null
        }
    }

    fun updateFavoriteStatus(movieId: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                val movie = repository.getMovieById(movieId)
                if (movie != null) {
                    val updatedMovie = movie.copy(favorites = isFavorite)
                    repository.updateMovie(updatedMovie)
                }
            } catch (e: Exception) {
                Log.e("MoviesViewModel", "Error updating favorite status: ${e.localizedMessage}")
            }
        }
    }

    private fun convertTMDBMovieToMovie(tmdbMovie: TMDBMovie): Movie {
        return Movie(
            id = tmdbMovie.id,
            title = tmdbMovie.title,
            releaseDate = tmdbMovie.releaseDate ?: "N/A",
            posterPath = "https://image.tmdb.org/t/p/w500" + (tmdbMovie.posterPath ?: ""),
            rating = tmdbMovie.voteAverage ?: 0.0,
            genre = tmdbMovie.genreIds.map { getGenreNameById(it) },
            overview = tmdbMovie.overview ?: "",
            favorites = false
        )
    }
}
