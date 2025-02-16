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

/**
 * Adapter לניהול רשימת הסרטים ב-RecyclerView
 */
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
        val movie = getItem(position)
        holder.bind(movie)
    }

    /**
     * ViewHolder לניהול הנתונים של כל פריט ברשימה
     */
    inner class MovieViewHolder(private val binding: ItemMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie) {
            binding.apply {
                // עדכון ה-UI עם נתוני הסרט
                textViewMovieTitle.text = movie.title
                textViewMovieDirector.text = movie.director
                textViewMovieGenre.text = movie.genre
                ratingBar.rating = movie.rating

                // הצגת תמונת הסרט
                if (!movie.posterPath.isNullOrEmpty()) {
                    imageViewPoster.setImageURI(Uri.parse(movie.posterPath))
                } else {
                    imageViewPoster.setImageResource(R.drawable.ic_movie_placeholder)
                }

                // האזנה ללחיצה על הפריט להצגת פרטים
                root.setOnClickListener { onMovieClick(movie) }

                // האזנה ללחיצה על כפתור המחיקה
                buttonDeleteMovie.setOnClickListener { onDeleteClick(movie) }
            }
        }
    }
}

/**
 * DiffUtil לשיפור ביצועים ברשימה (בודק הבדלים בין רשימות ומעדכן רק את מה שהשתנה)
 */
class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
    override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean =
        oldItem == newItem
}
