package com.example.nsamovie.ui.favorite


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nsamovie.databinding.FragmentFavoritesBinding
import com.example.nsamovie.ui.adapter.MovieAdapter
import com.example.nsamovie.ui.viewmodel.MoviesViewModel

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MoviesViewModel by viewModels()
    private lateinit var movieAdapter: MovieAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter(
            onMovieClick = { /* לא מבצע ניווט, אלא רק הצגה */ },
            onDeleteClick = { movie -> viewModel.delete(movie) }
        )

        binding.recyclerViewFavorites.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = movieAdapter
        }
    }

    private fun setupObservers() {
        viewModel.favoriteMovies.observe(viewLifecycleOwner) { movies ->
            movieAdapter.submitList(movies)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
