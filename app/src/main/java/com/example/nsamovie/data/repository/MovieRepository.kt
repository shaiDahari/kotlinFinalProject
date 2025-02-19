package com.example.nsamovie.data.repository


import com.example.nsamovie.data.local_db.MovieDao
import com.example.nsamovie.data.model.Movie
import com.example.nsamovie.network.TMDBApiService
import com.example.nsamovie.network.model.TMDBMovieResponse

class MovieRepository(private val movieDao: MovieDao, private val apiService: TMDBApiService) {

    // שליפת סרטים מומלצים (כעת עם movieId)
    suspend fun getRecommendedMovies(movieId: Int, apiKey: String): TMDBMovieResponse {
        return apiService.getRecommendedMovies(movieId, apiKey)
    }

    // חיפוש סרטים לפי שם
    suspend fun searchMovies(query: String, apiKey: String): TMDBMovieResponse {
        return apiService.searchMovies(apiKey, query)
    }

    // הוספת סרט למסד הנתונים המקומי (Room)
    suspend fun insertMovie(movie: Movie) {
        movieDao.insertMovie(movie)
    }

    // מחיקת סרט ממסד הנתונים המקומי
    suspend fun deleteMovie(movie: Movie) {
        movieDao.deleteMovie(movie)
    }
}
