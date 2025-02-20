package com.example.nsamovie.ui.ratings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.nsamovie.R
import com.example.nsamovie.databinding.FragmentRatingsBinding
import com.example.nsamovie.ui.viewmodel.MoviesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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

        binding.toggleFavorite.setOnCheckedChangeListener { _, isChecked ->
            updateFavoriteStatus(isChecked)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            val movie = viewModel.getMovieById(args.movieId)
            if (movie != null) {
                updateUI(movie)
            } else {
                binding.textViewMovieTitle.text = getString(R.string.movie_not_found)
            }
        }
    }

    private fun updateUI(movie: com.example.nsamovie.data.model.Movie) {
        binding.apply {
            textViewMovieTitle.text = movie.title
            textViewMovieReleaseDate.text = getString(R.string.movie_release_date, movie.releaseDate)
            textViewMovieGenre.text = getString(R.string.genre, movie.genre.joinToString(", "))
            textViewMovieDescription.text = movie.overview
            textViewRating.text = String.format(Locale.getDefault(), "%.1f", movie.rating)

            movie.posterPath.let { poster ->
                imageViewPoster.setImageURI(poster.toUri())
            }

            toggleFavorite.isChecked = movie.favorites
        }
    }

    private fun updateFavoriteStatus(isFavorite: Boolean) {
        lifecycleScope.launch {
            viewModel.updateFavoriteStatus(args.movieId, isFavorite)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
