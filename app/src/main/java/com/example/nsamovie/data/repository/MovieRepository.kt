package com.example.nsamovie.data.repository

import androidx.lifecycle.LiveData
import com.example.nsamovie.data.local_db.MovieDao
import com.example.nsamovie.data.model.Movie

class MovieRepository(private val movieDao: MovieDao) {
    val allMovies: LiveData<List<Movie>> = movieDao.getAllMovies()

    suspend fun insert(movie: Movie) {
        movieDao.insertMovie(movie)
    }

    suspend fun update(movie: Movie) {
        movieDao.updateMovie(movie)
    }

    suspend fun delete(movie: Movie) {
        movieDao.deleteMovie(movie)
    }

    suspend fun getMovieById(id: Int): Movie? {
        return movieDao.getMovieById(id)
    }
}
