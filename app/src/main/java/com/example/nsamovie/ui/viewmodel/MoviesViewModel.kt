package com.example.nsamovie.ui.viewmodel
//
//import android.app.DatePickerDialog
//import android.util.Log
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.nsamovie.data.model.Movie
//import com.example.nsamovie.network.model.TMDBMovie
//import com.example.nsamovie.data.repository.MovieRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//import androidx.lifecycle.*
//import java.util.Calendar
//
//
//
//
//@HiltViewModel
//class MoviesViewModel @Inject constructor(
//    private val repository: MovieRepository
//) : ViewModel() {
//
//    // LiveData for different movie lists
//    val allMovies: LiveData<List<Movie>> = repository.getAllMovies()
//    val favoriteMovies: LiveData<List<Movie>> = repository.getFavoriteMovies()
//
//    private val _movieList = MutableLiveData<List<Movie>>()
//    val movieList: LiveData<List<Movie>> = _movieList
//
//    private val _highRatedMovies = MutableLiveData<List<Movie>>()
//    val highRatedMovies: LiveData<List<Movie>> = _highRatedMovies
//
//    private val _newReleasesMovies = MutableLiveData<List<Movie>>()
//    val newReleasesMovies: LiveData<List<Movie>> = _newReleasesMovies
//
//    private val genreMap = mutableMapOf<Int, String>()
//
//    init {
//        fetchGenres()
//    }
//
//    private fun fetchGenres() {
//        viewModelScope.launch {
//            try {
//                val genres = repository.getGenres()
//                genres?.forEach { genre ->
//                    genreMap[genre.id] = genre.name
//                }
//            } catch (e: Exception) {
//                Log.e("MoviesViewModel", "Error fetching genres: ${e.localizedMessage}")
//            }
//        }
//    }
//
//    fun getGenreNameById(id: Int): String {
//        return genreMap[id] ?: "Unknown Genre"
//    }
//
//    fun fetchMovies(language: String = "en-US", page: Int = 1) {
//        viewModelScope.launch {
//            try {
//                val movies = repository.getPopularMovies(language, page)
//                _movieList.postValue(movies)
//                _highRatedMovies.postValue(movies.sortedByDescending { it.rating })
//                _newReleasesMovies.postValue(movies.sortedByDescending { it.releaseDate })
//            } catch (e: Exception) {
//                Log.e("MoviesViewModel", "Error fetching movies: ${e.localizedMessage}")
//            }
//        }
//    }
//
//    fun searchMovies(query: String) {
//        viewModelScope.launch {
//            try {
//                val response = repository.searchMovies(query)
//                val movies = response.movies.map { convertTMDBMovieToMovie(it) }
//                _movieList.postValue(movies)
//            } catch (e: Exception) {
//                Log.e("MoviesViewModel", "Error searching movies: ${e.localizedMessage}")
//                _movieList.postValue(emptyList())
//            }
//        }
//    }
//
//    fun searchMoviesByYear(year: String) {
//        viewModelScope.launch {
//            try {
//                val response = repository.searchMovies(year)
//                val movies = response.movies
//                    .filter { it.releaseDate?.startsWith(year) == true }
//                    .map { convertTMDBMovieToMovie(it) }
//                _movieList.postValue(movies)
//            } catch (e: Exception) {
//                Log.e("MoviesViewModel", "Error searching movies by year: ${e.localizedMessage}")
//                _movieList.postValue(emptyList())
//            }
//        }
//    }
//
//    fun searchMoviesByRegion(region: String) {
//        viewModelScope.launch {
//            try {
//                val movies = repository.getPopularMovies(language = "en-US", page = 1, region = region)
//                _movieList.postValue(movies)
//            } catch (e: Exception) {
//                Log.e("MoviesViewModel", "Error searching movies by region: ${e.localizedMessage}")
//                _movieList.postValue(emptyList())
//            }
//        }
//    }
//
//    fun getRecommendedMovies(movieId: Int) {
//        viewModelScope.launch {
//            try {
//                val response = repository.getRecommendedMovies(movieId)
//                val movies = response.movies.map { convertTMDBMovieToMovie(it) }
//                _movieList.postValue(movies)
//            } catch (e: Exception) {
//                Log.e("MoviesViewModel", "Error fetching recommended movies: ${e.localizedMessage}")
//            }
//        }
//    }
//
//    fun insertMovie(movie: Movie) {
//        viewModelScope.launch {
//            try {
//                repository.insertMovie(movie)
//            } catch (e: Exception) {
//                Log.e("MoviesViewModel", "Error inserting movie: ${e.localizedMessage}")
//            }
//        }
//    }
//
//    fun updateMovie(movie: Movie) {
//        viewModelScope.launch {
//            try {
//                repository.updateMovie(movie)
//            } catch (e: Exception) {
//                Log.e("MoviesViewModel", "Error updating movie: ${e.localizedMessage}")
//            }
//        }
//    }
//
//    suspend fun getMovieById(movieId: Int): Movie? {
//        return try {
//            repository.getMovieById(movieId)
//        } catch (e: Exception) {
//            Log.e("MoviesViewModel", "Error fetching movie by ID: ${e.localizedMessage}")
//            null
//        }
//    }
//
//    fun updateFavoriteStatus(movieId: Int, isFavorite: Boolean) {
//        viewModelScope.launch {
//            try {
//                val movie = repository.getMovieById(movieId)
//                if (movie != null) {
//                    val updatedMovie = movie.copy(favorites = isFavorite)
//                    repository.updateMovie(updatedMovie)
//                }
//            } catch (e: Exception) {
//                Log.e("MoviesViewModel", "Error updating favorite status: ${e.localizedMessage}")
//            }
//        }
//    }
//
//    private fun convertTMDBMovieToMovie(tmdbMovie: TMDBMovie): Movie {
//        return Movie(
//            id = tmdbMovie.id,
//            title = tmdbMovie.title,
//            releaseDate = tmdbMovie.releaseDate ?: "N/A",
//            posterPath = "https://image.tmdb.org/t/p/w500" + (tmdbMovie.posterPath ?: ""),
//            rating = tmdbMovie.voteAverage ?: 0.0,
//            genre = tmdbMovie.genreIds.map { getGenreNameById(it) },
//            overview = tmdbMovie.overview ?: "",
//            favorites = false
//        )
//    }
//}


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
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    // LiveData for different movie lists
    val allMovies: LiveData<List<Movie>> = repository.getAllMovies()
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
                val genres = repository.getGenres()
                genres?.forEach { genre ->
                    genreMap[genre.id] = genre.name
                }
            } catch (e: Exception) {
                Log.e("MoviesViewModel", "Error fetching genres: ${e.localizedMessage}")
            }
        }
    }

    fun getGenreNameById(id: Int): String {
        return genreMap[id] ?: "Unknown Genre"
    }

    fun fetchMovies(language: String = "en-US", page: Int = 1) {
        viewModelScope.launch {
            try {
                val movies = repository.getPopularMovies(language, page)
                _movieList.postValue(movies)
                _highRatedMovies.postValue(movies.sortedByDescending { it.rating })
                _newReleasesMovies.postValue(movies.sortedByDescending { it.releaseDate })
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
                val movies = response.movies.map { convertTMDBMovieToMovie(it) }
                _movieList.postValue(movies)
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
                // Fetch movies for each year in the range
                for (year in startYear..endYear) {
                    val response = repository.searchMovies(year.toString())
                    val moviesFromYear = response.movies
                        .filter { movie ->
                            val movieYear = movie.releaseDate?.split("-")?.firstOrNull()?.toIntOrNull()
                            movieYear != null && movieYear in startYear..endYear
                        }
                        .map { convertTMDBMovieToMovie(it) }
                    allMovies.addAll(moviesFromYear)
                }
                _movieList.postValue(allMovies.distinctBy { it.id })
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
            } catch (e: Exception) {
                Log.e("MoviesViewModel", "Error searching movies by region and language: ${e.localizedMessage}")
                _movieList.postValue(emptyList())
            }
        }
    }

    fun getRecommendedMovies(movieId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getRecommendedMovies(movieId)
                val movies = response.movies.map { convertTMDBMovieToMovie(it) }
                _movieList.postValue(movies)
            } catch (e: Exception) {
                Log.e("MoviesViewModel", "Error fetching recommended movies: ${e.localizedMessage}")
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