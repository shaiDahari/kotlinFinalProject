package com.example.nsamovie.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nsamovie.R
import com.example.nsamovie.data.model.Movie
import com.example.nsamovie.databinding.ItemMovieBinding

import com.bumptech.glide.Glide


class MovieAdapter(
    private val onMovieClick: (Movie) -> Unit,
) : ListAdapter<Movie, MovieAdapter.MovieViewHolder>(MovieDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = getItem(position)
        holder.bind(movie)
    }

    inner class MovieViewHolder(private val binding: ItemMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie) {
            binding.apply {
                movieTitle.text = movie.title
                movieGenre.text = movie.genre.joinToString(", ")
                movieRatingText.text = String.format("%.1f/10", movie.rating)

                // שימוש ב-Glide לטעינת התמונה
                Glide.with(binding.root.context)
                    .load(movie.posterPath)
                    .placeholder(R.drawable.ic_movie_placeholder)
                    .error(R.drawable.ic_movie_placeholder)
                    .into(moviePoster)

                root.setOnClickListener { onMovieClick(movie) }
            }
        }
    }
}

class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
    override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean =
        oldItem == newItem
}