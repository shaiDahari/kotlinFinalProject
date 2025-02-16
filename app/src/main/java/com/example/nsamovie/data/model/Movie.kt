package com.example.nsamovie.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val genre: String,
    val director: String,
    val releaseDate: String,
    val posterPath: String? = null,
    val rating: Float = 0f,
    val description: String? = null
)