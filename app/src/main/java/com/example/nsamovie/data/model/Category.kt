package com.example.nsamovie.data.model


data class Category(
    val categoryName: String,
    val movies: List<Movie> // רשימת הסרטים ששייכים לקטגוריה הזו
)
