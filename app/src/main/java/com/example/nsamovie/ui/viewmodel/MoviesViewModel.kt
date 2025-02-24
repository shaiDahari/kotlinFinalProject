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
import java.util.Locale
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

    private val _genreMap = MutableLiveData<Map<Int, String>>()
    val genreMapLiveData: LiveData<Map<Int, String>> = _genreMap

    init {
        fetchGenres()
    }

    private fun fetchGenres() {
        viewModelScope.launch {
            try {
                val genres = repository.getGenres()
                val genreMapping = genres?.associateBy({ it.id }, { it.name }) ?: emptyMap()
                _genreMap.postValue(genreMapping)
                Log.d("MoviesViewModel", "Genres fetched successfully: ${genreMapping.size}")
            } catch (e: Exception) {
                Log.e("MoviesViewModel", "Error fetching genres: ${e.localizedMessage}")
            }
        }
    }

    fun getGenreNameById(id: Int): String {
        return _genreMap.value?.get(id) ?: "Unknown Genre"
    }

    private fun getCurrentLanguage(): String {
        val locale = Locale.getDefault()
        return if (locale.language == "iw") "he-IL" else locale.toLanguageTag() // TMDB uses "he-IL"
    }

    private fun getCurrentRegion(): String? {
        return Locale.getDefault().country // e.g., "IL", "US"
    }

    // Expose a public function to trigger movie loading from the UI.
    fun loadMovies() {
        fetchMovies()
    }

    fun fetchMovies(page: Int = 1) {
        viewModelScope.launch {
            try {
                val language = getCurrentLanguage()
                val region = getCurrentRegion()
                val movies = repository.getPopularMovies(page = page)
                _movieList.postValue(movies)
                _highRatedMovies.postValue(movies.sortedByDescending { it.rating })
                _newReleasesMovies.postValue(movies.sortedByDescending { it.releaseDate })
                repository.insertMovies(movies)
            } catch (e: Exception) {
                Log.e("MoviesViewModel", "Error fetching movies: ${e.localizedMessage}")
                _movieList.postValue(emptyList())
            }
        }
    }

    fun searchMovies(query: String) {
        viewModelScope.launch {
            try {
                val response = repository.searchMovies(query)
                val movies = response.movies.map { repository.convertTMDBMovieToMovie(it) }
                _movieList.postValue(movies)
                repository.insertMovies(movies)
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
                    val response = repository.searchMovies(year.toString())
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
            } catch (e: Exception) {
                Log.e("MoviesViewModel", "Error searching movies by year range: ${e.localizedMessage}")
                _movieList.postValue(emptyList())
            }
        }
    }

    fun searchMoviesByRegionAndLanguage(region: String, language: String) {
        Log.d("MoviesViewModel", "Fetching movies for region: $region, language: $language")
        viewModelScope.launch {
            try {
                val response = repository.getPopularMovies()
                if (response.isNotEmpty()) {
                    Log.d("MoviesViewModel", "Movies retrieved successfully: ${response.size}")
                    _movieList.postValue(response)
                } else {
                    Log.w("MoviesViewModel", "No movies found for region: $region, language: $language")
                    _movieList.postValue(emptyList())
                }
            } catch (e: Exception) {
                Log.e("MoviesViewModel", "Error fetching movies", e)
                _movieList.postValue(emptyList())
            }
        }
    }

    fun fetchLocalizedMovies() {
        viewModelScope.launch {
            try {
                val moviesList = repository.getMoviesBasedOnLocale() ?: emptyList()
                val convertedMovies = moviesList.map { repository.convertTMDBMovieToMovie(it) }
                _movieList.postValue(convertedMovies)
                _highRatedMovies.postValue(convertedMovies.sortedByDescending { it.rating })
                _newReleasesMovies.postValue(convertedMovies.sortedByDescending { it.releaseDate })
                repository.insertMovies(convertedMovies)
                Log.d("MoviesViewModel", "Movies retrieved successfully: ${convertedMovies.size}")
            } catch (e: Exception) {
                Log.e("MoviesViewModel", "Error fetching localized movies: ${e.message}")
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
}
