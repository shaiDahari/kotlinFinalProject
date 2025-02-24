package com.example.nsamovie.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nsamovie.data.model.Movie
import com.example.nsamovie.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    val favoriteMovies: LiveData<List<Movie>> = repository.getFavoriteMovies()

    private val _movieList = MutableLiveData<List<Movie>>()
    val movieList: LiveData<List<Movie>> = _movieList


    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error


    private val _highRatedMovies = MutableLiveData<List<Movie>>()
    val highRatedMovies: LiveData<List<Movie>> = _highRatedMovies

    private val _newReleasesMovies = MutableLiveData<List<Movie>>()
    val newReleasesMovies: LiveData<List<Movie>> = _newReleasesMovies

    private val _genreMap = MutableLiveData<Map<Int, String>>()

    init {
        fetchGenres()
    }

    private fun fetchGenres() {
        viewModelScope.launch {
            try {
                val genres = repository.getGenres(getCurrentLanguage())
                val genreMapping = genres.associateBy({ it.id }, { it.name })
                _genreMap.postValue(genreMapping)
                Log.d("MoviesViewModel", "Genres fetched successfully: ${genreMapping.size}")
            } catch (e: Exception) {
                Log.e("MoviesViewModel", "Error fetching genres: ${e.localizedMessage}")
            }
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

    fun fetchMovies(page: Int = 1) {
        viewModelScope.launch {
            try {
                val movies = repository.getPopularMovies(page)
                _movieList.postValue(movies)
                _highRatedMovies.postValue(movies.sortedByDescending { it.rating })
                _newReleasesMovies.postValue(movies.sortedByDescending { it.releaseDate })
                repository.insertMovies(movies)
                Log.d("MoviesViewModel", "Movies fetched successfully: ${movies.size}")
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
                val movies = response.movies.map { repository.convertTMDBMovieToMovie(it) }
                _movieList.postValue(movies)
                repository.insertMovies(movies)
                Log.d("MoviesViewModel", "Search completed: ${movies.size} results")
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
                        .map { repository.convertTMDBMovieToMovie(it) }
                    allMovies.addAll(moviesFromYear)
                }
                val uniqueMovies = allMovies.distinctBy { it.id }
                _movieList.postValue(uniqueMovies)
                repository.insertMovies(uniqueMovies)
                Log.d("MoviesViewModel", "Year range search completed: ${uniqueMovies.size} results")
            } catch (e: Exception) {
                Log.e("MoviesViewModel", "Error searching movies by year range: ${e.localizedMessage}")
                _movieList.postValue(emptyList())
            }
        }
    }

    fun fetchMoviesByCountryCode(countryCode: String) {
        viewModelScope.launch {
            try {
                val moviesFromApi = repository.getMoviesByOriginCountry(countryCode)
                _movieList.postValue(moviesFromApi)
            } catch (e: Exception) {
                _error.postValue("Failed to fetch movies: ${e.message}")
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
                    Log.d("MoviesViewModel", "Favorite status updated for movie: $movieId")
                }
            } catch (e: Exception) {
                Log.e("MoviesViewModel", "Error updating favorite status: ${e.localizedMessage}")
            }
        }
    }
}