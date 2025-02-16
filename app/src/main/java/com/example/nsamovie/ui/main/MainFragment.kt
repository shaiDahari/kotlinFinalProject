package com.example.nsamovie.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nsamovie.R
import com.example.nsamovie.data.local_db.MovieDao
import com.example.nsamovie.data.local_db.MovieDatabase
import com.example.nsamovie.data.model.Movie
import com.example.nsamovie.data.repository.MovieRepository
import com.example.nsamovie.databinding.FragmentMainBinding
import com.example.nsamovie.ui.adapter.MovieAdapter
import com.example.nsamovie.ui.viewmodel.MoviesViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var movieDao: MovieDao
    private lateinit var repository: MovieRepository

    private val viewModel: MoviesViewModel by viewModels {
        MoviesViewModel.Factory(repository)
    }

    private lateinit var movieAdapter: MovieAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        movieDao = MovieDatabase.getDatabase(requireContext()).movieDao()
        repository = MovieRepository(movieDao)

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter(
            onMovieClick = { movie ->
                val action = MainFragmentDirections.actionMainFragmentToMovieDetailsFragment(movie.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { movie ->
                showDeleteConfirmationDialog(movie)
            }
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
        binding.fabAddMovie.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_addEditMovieFragment)
        }
    }

    private fun showDeleteConfirmationDialog(movie: Movie) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_confirmation_title))
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.action_delete)) { _, _ ->
                viewModel.delete(movie)
                Toast.makeText(requireContext(),R.string.msg_delete_success,Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
