package com.example.nsamovie.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.nsamovie.data.model.Movie
import com.example.nsamovie.data.repository.MovieRepository
import kotlinx.coroutines.launch

class MoviesViewModel(private val repository: MovieRepository) : ViewModel() {

    val allMovies: LiveData<List<Movie>> = repository.getAllMovies()

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _selectedMovie = MutableLiveData<Movie?>()
    val selectedMovie: LiveData<Movie?> get() = _selectedMovie

    fun insert(movie: Movie) = viewModelScope.launch {
        _isLoading.value = true
        try {
            repository.insertMovie(movie)
        } catch (e: Exception) {
            _errorMessage.value = "Error adding movie: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun update(movie: Movie) = viewModelScope.launch {
        _isLoading.value = true
        try {
            repository.updateMovie(movie)
        } catch (e: Exception) {
            _errorMessage.value = "Error updating movie: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun delete(movie: Movie) = viewModelScope.launch {
        _isLoading.value = true
        try {
            repository.deleteMovie(movie)
        } catch (e: Exception) {
            _errorMessage.value = "Error deleting movie: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun getMovieById(movieId: Int) = viewModelScope.launch {
        _isLoading.value = true
        try {
            _selectedMovie.value = repository.getMovieById(movieId)
        } catch (e: Exception) {
            _errorMessage.value = "Error fetching movie: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun setSelectedMovie(movie: Movie?) {
        _selectedMovie.value = movie
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

    private val _recommendedMovies = MutableLiveData<List<Movie>>()
    val recommendedMovies: LiveData<List<Movie>> get() = _recommendedMovies

    fun fetchRecommendedMovies() {
        viewModelScope.launch {
            try {
                val movies = repository.getRecommendedMovies() // Fetch API through Repository
                _recommendedMovies.postValue(movies)
            } catch (e: Exception) {
                Log.e("ViewModel", "Error fetching recommended movies", e)
            }
        }
    }

    fun getMoviesByCategory(): LiveData<List<Category>> {
        return Transformations.map(allMovies) { moviesList ->
            listOf(
                Category("Highest Rated", moviesList.sortedByDescending { it.rating }.take(10)),
                Category("Latest Movies", moviesList.sortedByDescending { it.releaseDate.takeLast(4).toIntOrNull() ?: 0 }),
                Category("Action Movies", moviesList.filter { it.genre.contains("Action", ignoreCase = true) }),
                Category("Comedy", moviesList.filter { it.genre.contains("Comedy", ignoreCase = true) })
            )
        }
    }
}
