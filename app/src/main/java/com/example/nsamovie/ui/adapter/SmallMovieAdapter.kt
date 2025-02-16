package com.example.nsamovie.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nsamovie.databinding.ItemSmallMovieBinding
import com.example.nsamovie.data.model.Movie

class SmallMovieAdapter(
    private val movies: List<Movie>,
    private val onMovieClick: (movieId: Int) -> Unit
) : RecyclerView.Adapter<SmallMovieAdapter.SmallMovieViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmallMovieViewHolder {
        val binding = ItemSmallMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SmallMovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SmallMovieViewHolder, position: Int) {
        val movie = movies[position]
        holder.bind(movie)
    }

    override fun getItemCount(): Int = movies.size

    inner class SmallMovieViewHolder(private val binding: ItemSmallMovieBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie) {

            Glide.with(binding.root.context)
                .load(movie.posterPath)
                .into(binding.moviePoster)

            binding.movieTitle.text = movie.title
            binding.movieYear.text = movie.releaseDate.toString()
            binding.movieGenre.text = movie.genre.joinToString(", ")  // Assuming genre is a list of strings
            binding.movieRatingText.text = "${movie.rating}/10"

            itemView.setOnClickListener {
                onMovieClick(movie.id)
            }
        }
    }
}
