package com.example.nsamovie.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.nsamovie.data.model.Movie
import com.example.nsamovie.data.repository.MovieRepository
import kotlinx.coroutines.launch


class MoviesViewModel(private val repository: MovieRepository) : ViewModel() {
    val allMovies: LiveData<List<Movie>> = repository.getAllMovies()
    val favoriteMovies: LiveData<List<Movie>> = repository.getFavoriteMovies()

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _selectedMovie = MutableLiveData<Movie?>()
    val selectedMovie: LiveData<Movie?> get() = _selectedMovie

    // פונקציה לעדכון סטטוס המועדף של הסרט
    fun updateFavoriteStatus(movieId: Int, isFavorite: Boolean) = viewModelScope.launch {
        val movie = repository.getMovieById(movieId)
        movie?.let {
            // יצירת אובייקט חדש עם הערכים המעודכנים
            val updatedMovie = it.copy(favorites = isFavorite)
            repository.updateMovie(updatedMovie) // עדכון במסד הנתונים
        }
    }

    // הוספת סרט למסד הנתונים
    fun insert(movie: Movie) = viewModelScope.launch {
        try {
            repository.insertMovie(movie)
        } catch (e: Exception) {
            _errorMessage.value = "Error adding movie: ${e.message}"
        }
    }

    // עדכון סרט
    fun update(movie: Movie) = viewModelScope.launch {
        try {
            repository.updateMovie(movie)
        } catch (e: Exception) {
            _errorMessage.value = "Error updating movie: ${e.message}"
        }
    }

    // מחיקת סרט
    fun delete(movie: Movie) = viewModelScope.launch {
        try {
            repository.deleteMovie(movie)
        } catch (e: Exception) {
            _errorMessage.value = "Error deleting movie: ${e.message}"
        }
    }

    // שליפת סרט לפי ID
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

    class Factory(private val repository: MovieRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MoviesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MoviesViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}