package com.example.nsamovie.ui.viewmodel
//
//
//import android.util.Log
//import androidx.lifecycle.*
//import com.example.nsamovie.data.model.Movie
//import com.example.nsamovie.data.repository.MovieRepository
//import kotlinx.coroutines.launch
//
//import dagger.hilt.android.lifecycle.HiltViewModel
//import javax.inject.Inject


import androidx.lifecycle.*
import com.example.nsamovie.data.model.Movie
import com.example.nsamovie.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(private val repository: MovieRepository) : ViewModel() {
    val allMovies: LiveData<List<Movie>> = repository.getAllMovies()
    val favoriteMovies: LiveData<List<Movie>> = repository.getFavoriteMovies()

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _selectedMovie = MutableLiveData<Movie?>()
    val selectedMovie: LiveData<Movie?> get() = _selectedMovie

    fun updateFavoriteStatus(movieId: Int, isFavorite: Boolean) = viewModelScope.launch {
        val movie = repository.getMovieById(movieId)
        movie?.let {
            val updatedMovie = it.copy(favorites = isFavorite)
            repository.updateMovie(updatedMovie)
        }
    }

    fun insert(movie: Movie) = viewModelScope.launch {
        try {
            repository.insertMovie(movie)
        } catch (e: Exception) {
            _errorMessage.value = "Error adding movie: ${e.message}"
        }
    }

    fun update(movie: Movie) = viewModelScope.launch {
        try {
            repository.updateMovie(movie)
        } catch (e: Exception) {
            _errorMessage.value = "Error updating movie: ${e.message}"
        }
    }

    fun delete(movie: Movie) = viewModelScope.launch {
        try {
            repository.deleteMovie(movie)
        } catch (e: Exception) {
            _errorMessage.value = "Error deleting movie: ${e.message}"
        }
    }

    fun getMovieById(movieId: Int): LiveData<Movie?> {
        val result = MutableLiveData<Movie?>()
        viewModelScope.launch {
            try {
                result.value = repository.getMovieById(movieId)
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching movie: ${e.message}"
            }
        }
        return result
    }
}