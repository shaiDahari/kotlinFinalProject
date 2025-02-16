package com.example.nsamovie.ui.viewmodel

import androidx.lifecycle.*
import com.example.nsamovie.data.model.Movie
import com.example.nsamovie.data.repository.MovieRepository
import kotlinx.coroutines.launch

/**
 * ה-ViewModel אחראי על ניהול הנתונים ושמירתם במצב המסך
 * עובד בתיווך בין ה-Repository לבין ה-UI
 */
class MoviesViewModel(private val repository: MovieRepository) : ViewModel() {

    // LiveData לשמירה ועדכון רשימת הסרטים בזמן אמת
    val allMovies: LiveData<List<Movie>> = repository.getAllMovies()

    // LiveData לניהול הודעות שגיאה
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // LiveData לניהול מצב טעינה
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // משתנה לסרט שנבחר
    private val _selectedMovie = MutableLiveData<Movie?>()
    val selectedMovie: LiveData<Movie?> get() = _selectedMovie

    /**
     * מוסיף סרט חדש למסד הנתונים
     */
    fun insert(movie: Movie) = viewModelScope.launch {
        _isLoading.value = true
        try {
            repository.insertMovie(movie)
        } catch (e: Exception) {
            _errorMessage.value = "שגיאה בהוספת הסרט: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * מעדכן סרט קיים במסד הנתונים
     */
    fun update(movie: Movie) = viewModelScope.launch {
        _isLoading.value = true
        try {
            repository.updateMovie(movie)
        } catch (e: Exception) {
            _errorMessage.value = "שגיאה בעדכון הסרט: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * מוחק סרט ממסד הנתונים
     */
    fun delete(movie: Movie) = viewModelScope.launch {
        _isLoading.value = true
        try {
            repository.deleteMovie(movie)
        } catch (e: Exception) {
            _errorMessage.value = "שגיאה במחיקת הסרט: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * מבצע שליפה של סרט לפי ה-ID שלו
     */
    fun getMovieById(movieId: Int) = viewModelScope.launch {
        _isLoading.value = true
        try {
            _selectedMovie.value = repository.getMovieById(movieId)
        } catch (e: Exception) {
            _errorMessage.value = "שגיאה בשליפת הסרט: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * מאפס את הודעות השגיאה
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * מגדיר סרט שנבחר
     */
    fun setSelectedMovie(movie: Movie?) {
        _selectedMovie.value = movie
    }

    /**
     * Factory עבור יצירת ה-ViewModel עם ה-Repository הנכון
     */
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
                val movies = repository.getRecommendedMovies() // קריאה ל-API דרך ה-Repository
                _recommendedMovies.postValue(movies)
            } catch (e: Exception) {
                Log.e("ViewModel", "Error fetching recommended movies", e)
            }
        }
    }

    companion object {
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
