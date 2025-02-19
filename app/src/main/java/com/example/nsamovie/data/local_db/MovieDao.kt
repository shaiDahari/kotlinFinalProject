package com.example.nsamovie.data.local_db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.nsamovie.data.model.Movie

@Dao
interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: Movie)

    @Delete
    suspend fun deleteMovie(movie: Movie)

    @Query("SELECT * FROM movies")
    fun getAllMovies(): LiveData<List<Movie>>

    @Query("SELECT * FROM movies WHERE favorites = 1")
    fun getFavoriteMovies(): LiveData<List<Movie>> // פונקציה זו מחזירה את הסרטים המועדפים

    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieById(movieId: Int): Movie?

    @Update
    suspend fun updateMovie(movie: Movie)
}