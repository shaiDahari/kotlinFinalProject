package com.example.nsamovie.ui.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nsamovie.databinding.ItemMovieBinding
import com.example.nsamovie.data.model.Movie

import com.bumptech.glide.Glide


class MoviesHorizontalAdapter(
    private val movies: List<Movie>,
    private val onMovieClick: (Movie) -> Unit
) : RecyclerView.Adapter<MoviesHorizontalAdapter.MovieViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        holder.bind(movie)
    }

    override fun getItemCount(): Int = movies.size

    inner class MovieViewHolder(private val binding: ItemMovieBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            binding.movieTitle.text = movie.title

            // השתמש ב-Glide לטעינת התמונה
            Glide.with(binding.moviePoster.context)
                .load(movie.posterPath) // Assumes posterPath is a valid URL
                .into(binding.moviePoster)

            binding.root.setOnClickListener { onMovieClick(movie) }
        }
    }
}