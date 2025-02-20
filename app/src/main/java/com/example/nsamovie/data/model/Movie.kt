package com.example.nsamovie.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.nsamovie.data.local_db.Converters

@Entity(tableName = "movies")
@TypeConverters(Converters::class)  // Apply TypeConverters
data class Movie(
    @PrimaryKey val id: Int,
    val title: String,
    val releaseDate: String,
    val posterPath: String,
    val rating: Double,
    val genre: List<String>,  // List of genre names
    val overview: String,
    val favorites: Boolean = false
)
