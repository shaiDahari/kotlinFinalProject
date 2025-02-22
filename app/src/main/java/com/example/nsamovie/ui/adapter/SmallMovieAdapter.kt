package com.example.nsamovie.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nsamovie.databinding.ItemSmallMovieBinding
import com.example.nsamovie.data.model.Movie
import com.example.nsamovie.R
import com.bumptech.glide.load.engine.DiskCacheStrategy
import android.util.Log


import android.graphics.drawable.Drawable

import com.bumptech.glide.load.DataSource

import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target


class SmallMovieAdapter(
    private var movies: MutableList<Movie>,
    private val onMovieClick: (movieId: Int) -> Unit
) : RecyclerView.Adapter<SmallMovieAdapter.SmallMovieViewHolder>() {

    private val TAG = "SmallMovieAdapter"

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
            Log.d(TAG, "Binding movie: ${movie.title}")
            Log.d(TAG, "Poster URL: ${movie.posterPath}")

            binding.movieTitle.text = movie.title
            binding.movieYear.text = movie.releaseDate.takeIf { it.isNotBlank() } ?: "N/A"
            binding.movieGenre.text = movie.genre.joinToString(", ")
            binding.movieRatingText.text = String.format("%.1f/10", movie.rating)

            Glide.with(binding.root.context)
                .load(movie.posterPath)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_movie_placeholder)
                .error(R.drawable.ic_movie_placeholder)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e(TAG, "Image load failed for ${movie.title}: ${e?.message}")
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d(TAG, "Image loaded successfully for ${movie.title}")
                        return false
                    }
                })
                .into(binding.moviePoster)

            itemView.setOnClickListener {
                onMovieClick(movie.id)
            }
        }
    }
}