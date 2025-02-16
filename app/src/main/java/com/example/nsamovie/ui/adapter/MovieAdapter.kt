package com.example.nsamovie.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nsamovie.data.model.Movie
import com.example.nsamovie.databinding.ItemMovieBinding
import com.example.nsamovie.R

class MovieAdapter(
    private val onMovieClick: (Movie) -> Unit,
    private val onDeleteClick: (Movie) -> Unit
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
        holder.bind(getItem(position))
    }

    inner class MovieViewHolder(
        private val binding: ItemMovieBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onMovieClick(getItem(position))
                }
            }

            binding.btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(position))
                }
            }
        }

        fun bind(movie: Movie) {
            binding.apply {
                movieTitle.text = movie.title
                moviePoster.contentDescription = root.context.getString(R.string.movie_poster)
                movieYear.text = movie.releaseDate
                movieDirector.text = movie.director
                movieRating.rating = movie.rating
                movieGenre.text = movie.genre


                Glide.with(itemView.context)
                    .load(movie.posterPath)
                    .fitCenter()
                    .fallback(R.drawable.ic_movie_placeholder)
                    .into(binding.moviePoster)
            }
        }
    }
}

private class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
    override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem == newItem
    }
}
