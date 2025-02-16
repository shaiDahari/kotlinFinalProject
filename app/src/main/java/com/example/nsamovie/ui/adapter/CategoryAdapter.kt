package com.example.nsamovie.ui.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nsamovie.databinding.ItemCategoryBinding
import com.example.nsamovie.model.Category

class CategoryAdapter(
    private val categories: List<Category>,
    private val onMovieClick: (movieId: Int) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category) {
            binding.textViewCategoryTitle.text = category.categoryName

            // יצירת מתאם חדש לכל רשימת סרטים אופקית
            val movieAdapter = MoviesHorizontalAdapter(category.movies) { movie ->
                onMovieClick(movie.id)
            }

            binding.recyclerViewMovies.adapter = movieAdapter
        }
    }
}
