package com.example.nsamovie.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nsamovie.databinding.ItemSmallMovieBinding
import com.example.nsamovie.data.model.Movie

class SmallMovieAdapter(
    private var movies: MutableList<Movie>,
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

    fun setMovies(newMovies: List<Movie>) {
        this.movies.clear()
        this.movies.addAll(newMovies)
        notifyDataSetChanged()
    }

    inner class SmallMovieViewHolder(private val binding: ItemSmallMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie) {
            Glide.with(binding.root.context)
                .load(movie.posterPath)
                .into(binding.moviePoster)

            binding.movieTitle.text = movie.title
            binding.movieYear.text = movie.releaseDate.takeIf { it.isNotBlank() } ?: "N/A"
            binding.movieGenre.text = movie.genre.joinToString(", ")
            binding.movieRatingText.text = String.format("%.1f/10", movie.rating)

            itemView.setOnClickListener {
                onMovieClick(movie.id)
            }
        }
    }
}
