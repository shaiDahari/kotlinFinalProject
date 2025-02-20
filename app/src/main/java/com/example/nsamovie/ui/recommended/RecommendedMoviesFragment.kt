package com.example.nsamovie.ui.recommended

//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.nsamovie.R
//import com.example.nsamovie.databinding.FragmentRecommendedMoviesBinding
//import com.example.nsamovie.ui.adapter.MovieAdapter
//import com.example.nsamovie.ui.viewmodel.MoviesViewModel
//import dagger.hilt.android.AndroidEntryPoint
//


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nsamovie.R
import com.example.nsamovie.databinding.FragmentRecommendedMoviesBinding
import com.example.nsamovie.ui.adapter.SmallMovieAdapter
import com.example.nsamovie.ui.viewmodel.MoviesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecommendedMoviesFragment : Fragment() {

    private var _binding: FragmentRecommendedMoviesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MoviesViewModel by viewModels()
    private lateinit var recommendedMovieAdapter: SmallMovieAdapter
    private lateinit var newReleasesAdapter: SmallMovieAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecommendedMoviesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupObservers()
        setupListeners()

        // Fetch movies
        val apiKey = getString(R.string.api_key) // Get the API key from resources
        viewModel.fetchMovies(apiKey)
    }

    private fun setupRecyclerViews() {
        recommendedMovieAdapter = SmallMovieAdapter(
            movies = mutableListOf(),
            onMovieClick = { movieId ->
                val action = RecommendedMoviesFragmentDirections
                    .actionRecommendedMoviesFragmentToRatingsFragment(movieId)
                findNavController().navigate(action)
            }
        )

        newReleasesAdapter = SmallMovieAdapter(
            movies = mutableListOf(),
            onMovieClick = { movieId ->
                val action = RecommendedMoviesFragmentDirections
                    .actionRecommendedMoviesFragmentToRatingsFragment(movieId)
                findNavController().navigate(action)
            }
        )

        binding.recyclerViewRecommendedMovies.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recommendedMovieAdapter
        }

        binding.recyclerViewNewReleases.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = newReleasesAdapter
        }
    }

    private fun setupObservers() {
        viewModel.movieList.observe(viewLifecycleOwner) { movies ->
            if (movies.isNullOrEmpty()) {
                Log.e("RecommendedMovies", "No movies fetched")
            } else {
                Log.d("RecommendedMovies", "Movies received: ${movies.size}")
                recommendedMovieAdapter.setMovies(movies) // Update the recommended movie list
                newReleasesAdapter.setMovies(movies) // Update the new releases list (or handle differently if they differ)
            }
        }
    }

    private fun setupListeners() {
        binding.btnSearchMovies.setOnClickListener {
            try {
                Log.d("RecommendedMovies", "Attempting to navigate to search")
                val action = RecommendedMoviesFragmentDirections
                    .actionRecommendedMoviesFragmentToSearchMoviesFragment(-1)
                findNavController().navigate(action)
            } catch (e: Exception) {
                Log.e("RecommendedMovies", "Navigation failed", e)
            }
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