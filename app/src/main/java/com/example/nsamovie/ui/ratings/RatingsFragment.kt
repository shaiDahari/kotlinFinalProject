package com.example.nsamovie.ui.ratings

//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.core.net.toUri
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.navigation.fragment.navArgs
//import com.example.nsamovie.R
//import com.example.nsamovie.databinding.FragmentRatingsBinding
//import com.example.nsamovie.mainActivity.MainActivity
//import com.example.nsamovie.ui.viewmodel.MoviesViewModel
//import com.example.nsamovie.ui.viewmodel.ViewModelFactory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.nsamovie.R
import com.example.nsamovie.databinding.FragmentRatingsBinding
import com.example.nsamovie.ui.viewmodel.MoviesViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
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
                    textViewMovieDirector.text = getString(R.string.movie_director, movie.director)
                    textViewMovieReleaseDate.text = getString(R.string.movie_release_date, movie.releaseDate)
                    textViewMovieGenre.text = getString(R.string.genre, movie.genre.joinToString(", "))
                    textViewMovieDescription.text = movie.description ?: getString(R.string.no_description)
                    textViewRating.text = String.format(Locale.getDefault(), "%.1f", movie.rating)

                    movie.posterPath?.let { poster ->
                        imageViewPoster.setImageURI(poster.toUri())
                    } ?: imageViewPoster.setImageResource(R.drawable.ic_movie_placeholder)

                    toggleFavorite.isChecked = movie.favorites
                }
            }
        }
    }

    private fun setupListeners() {
        binding.toggleFavorite.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateFavoriteStatus(args.movieId, isChecked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}