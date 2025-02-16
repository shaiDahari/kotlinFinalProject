package com.example.nsamovie.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Categories")
data class Category(
    @PrimaryKey
    val categoryName: String,
    val movies: List<Movie> // רשימת הסרטים ששייכים לקטגוריה הזו
)
