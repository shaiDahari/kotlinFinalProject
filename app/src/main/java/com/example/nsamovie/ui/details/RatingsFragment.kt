package com.example.nsamovie.ui.details


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.nsamovie.R
import com.example.nsamovie.databinding.FragmentRatingsBinding
import com.example.nsamovie.ui.viewmodel.MoviesViewModel

class RatingsFragment : Fragment() {

    private var _binding: FragmentRatingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MoviesViewModel by viewModels()
    private val args: RatingsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRatingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.getMovieById(args.movieId).observe(viewLifecycleOwner) { movie ->
            movie?.let {
                binding.apply {
                    textViewMovieTitle.text = movie.title
                    textViewMovieDirector.text = movie.director
                    ratingBar.rating = movie.rating
                    textViewMovieDescription.text = movie.description
                    textViewMovieGenre.text = movie.genre
                    movie.posterPath?.let { poster ->
                        imageViewPoster.setImageURI(poster.toUri())
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.buttonEditMovie.setOnClickListener {
            val action = RatingsFragmentDirections.actionRatingsFragmentToSearchMoviesFragment(args.movieId)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
