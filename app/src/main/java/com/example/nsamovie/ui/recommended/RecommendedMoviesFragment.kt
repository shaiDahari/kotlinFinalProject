package com.example.nsamovie.ui.recommended

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nsamovie.R
import com.example.nsamovie.databinding.FragmentRecommendedMoviesBinding
import com.example.nsamovie.ui.adapter.MovieAdapter
import com.example.nsamovie.ui.viewmodel.MoviesViewModel

class RecommendedMoviesFragment : Fragment() {

    private var _binding: FragmentRecommendedMoviesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MoviesViewModel by viewModels()
    private lateinit var movieAdapter: MovieAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecommendedMoviesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter(
            onMovieClick = { movie ->
                val action = RecommendedMoviesFragmentDirections
                    .actionRecommendedMoviesFragmentToRatingsFragment(movie.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { movie -> viewModel.delete(movie) }
        )

        binding.recyclerViewMovies.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = movieAdapter
        }
    }

    private fun setupObservers() {
        viewModel.allMovies.observe(viewLifecycleOwner) { movies ->
            movieAdapter.submitList(movies)
        }
    }

    private fun setupListeners() {
        binding.btnSearchMovies.setOnClickListener {
            findNavController().navigate(R.id.action_recommendedMoviesFragment_to_searchMoviesFragment)
        }

        binding.btnFavorites.setOnClickListener {
            findNavController().navigate(R.id.action_recommendedMoviesFragment_to_favoritesFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
