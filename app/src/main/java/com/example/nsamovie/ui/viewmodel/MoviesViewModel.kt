package com.example.nsamovie.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nsamovie.data.model.Movie
import com.example.nsamovie.data.repository.MovieRepository
import com.example.nsamovie.network.model.TMDBMovieResponse
import com.example.nsamovie.network.TMDBApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val repository: MovieRepository,
    private val apiService: TMDBApiService
) : ViewModel() {

    // LiveData for all movies and favorites
    val allMovies: LiveData<List<Movie>> = repository.getAllMovies()
    val favoriteMovies: LiveData<List<Movie>> = repository.getFavoriteMovies()

    // MutableLiveData for fetched movies
    private val _movieList = MutableLiveData<List<Movie>>()
    val movieList: LiveData<List<Movie>> get() = _movieList

    // Function to fetch movies from API
    fun fetchMovies(apiKey: String, language: String = "en-US", page: Int = 1) {
        viewModelScope.launch {
            try {
                val response = apiService.getPopularMovies(apiKey, language, page)
                if (response.movies.isNullOrEmpty()) {
                    Log.e("MoviesViewModel", "No movies fetched")
                } else {
                    _movieList.postValue(response.movies) // Update the live data with fetched movies
                }
            } catch (e: Exception) {
                Log.e("MoviesViewModel", "Error fetching movies: ${e.localizedMessage}")
            }
        }
    }

    suspend fun getMovieById(movieId: Int): Movie? {
        return repository.getMovieById(movieId)
    }

    fun deleteMovie(movie: Movie) {
        viewModelScope.launch {
            repository.deleteMovie(movie)
        }
    }

    fun updateFavoriteStatus(movieId: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            val movie = repository.getMovieById(movieId)
            movie?.let {
                val updatedMovie = it.copy(favorites = isFavorite)
                repository.updateMovie(updatedMovie)
            }
        }
    }
}
