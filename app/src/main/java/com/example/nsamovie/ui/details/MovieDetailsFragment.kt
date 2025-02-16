package com.example.nsamovie.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.nsamovie.R
import com.example.nsamovie.databinding.FragmentMovieDetailsBinding
import com.example.nsamovie.ui.viewmodel.MoviesViewModel
import com.example.nsamovie.data.repository.MovieRepository
import com.example.nsamovie.data.local_db.MovieDao
import com.example.nsamovie.data.local_db.MovieDatabase

class MovieDetailsFragment : Fragment() {

    private var _binding: FragmentMovieDetailsBinding? = null
    private val binding get() = _binding!!

    private val movieDao: MovieDao by lazy {
        MovieDatabase.getDatabase(requireContext()).movieDao()
    }

    private val viewModel: MoviesViewModel by viewModels {
        MoviesViewModel.Factory(MovieRepository(movieDao))
    }

    private val args: MovieDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadMovie()
        setupClickListeners()
    }

    private fun loadMovie() {
        viewModel.getMovieById(args.movieId)
        viewModel.selectedMovie.observe(viewLifecycleOwner) { movie ->
            movie?.let {
                binding.movieTitle.text = it.title
                binding.movieYear.text = it.releaseDate
                binding.movieDirector.text = it.director
                binding.ratingBar.rating = it.rating
                binding.movieGenre.text = it.genre
                binding.movieDescription.text = it.description
                Glide.with(requireContext())
                    .load(movie.posterPath)
                    .fitCenter()
                    .fallback(R.drawable.ic_movie_placeholder)
                    .into(binding.moviePoster)
            }
        }
    }

    private fun setupClickListeners() {
        binding.buttonEditMovie.setOnClickListener {
            findNavController().navigate(
                MovieDetailsFragmentDirections.actionMovieDetailsFragmentToAddEditMovieFragment(args.movieId)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
