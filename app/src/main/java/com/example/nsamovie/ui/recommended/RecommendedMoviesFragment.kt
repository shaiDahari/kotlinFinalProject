package com.example.nsamovie.ui.recommended

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
    private lateinit var highRatedAdapter: SmallMovieAdapter
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

        val apiKey = getString(R.string.api_key)
        viewModel.fetchMovies(apiKey)
    }

    private fun setupRecyclerViews() {
        highRatedAdapter = SmallMovieAdapter(
            movies = mutableListOf(),
            onMovieClick = { movieId ->
                findNavController().navigate(
                    R.id.action_recommendedMoviesFragment_to_ratingsFragment,
                    Bundle().apply {
                        putInt("movieId", movieId)
                    }
                )
            }
        )

        newReleasesAdapter = SmallMovieAdapter(
            movies = mutableListOf(),
            onMovieClick = { movieId ->
                findNavController().navigate(
                    R.id.action_recommendedMoviesFragment_to_ratingsFragment,
                    Bundle().apply {
                        putInt("movieId", movieId)
                    }
                )
            }
        )

        binding.recyclerViewRecommendedMovies.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = highRatedAdapter
        }

        binding.recyclerViewNewReleases.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = newReleasesAdapter
        }
    }

    private fun setupObservers() {
        viewModel.highRatedMovies.observe(viewLifecycleOwner) { movies ->
            highRatedAdapter.setMovies(movies)
        }

        viewModel.newReleasesMovies.observe(viewLifecycleOwner) { movies ->
            newReleasesAdapter.setMovies(movies)
        }
    }

    private fun setupListeners() {
        binding.btnSearchMovies.setOnClickListener {
            try {
                findNavController().navigate(
                    R.id.action_recommendedMoviesFragment_to_searchMoviesFragment
                )
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