package com.example.nsamovie.ui.ratings
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.core.net.toUri
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.lifecycleScope
//import androidx.navigation.fragment.navArgs
//import com.example.nsamovie.R
//import com.example.nsamovie.databinding.FragmentRatingsBinding
//import com.example.nsamovie.ui.viewmodel.MoviesViewModel
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.launch
//import java.util.Locale
//
//import androidx.navigation.fragment.findNavController
//
//import android.util.Log
//
//
//import com.bumptech.glide.Glide
//import com.bumptech.glide.load.engine.DiskCacheStrategy
//
//
//@AndroidEntryPoint
//class RatingsFragment : Fragment() {
//
//    private var _binding: FragmentRatingsBinding? = null
//    private val binding get() = _binding!!
//
//    private val viewModel: MoviesViewModel by viewModels()
//    private val args: RatingsFragmentArgs by navArgs()
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentRatingsBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        setupObservers()
//
//        binding.toggleFavorite.setOnCheckedChangeListener { _, isChecked ->
//            viewModel.updateFavoriteStatus(args.movieId, isChecked)
//        }
//    }
//
//    // בתוך setupObservers() בקובץ RatingsFragment.kt:
//
//    private fun setupObservers() {
//        lifecycleScope.launch {
//            val movie = viewModel.getMovieById(args.movieId)
//            if (movie != null) {
//                binding.apply {
//                    textViewMovieTitle.text = movie.title
//                    textViewMovieReleaseDate.text = getString(R.string.movie_release_date, movie.releaseDate)
//                    textViewMovieGenre.text = getString(R.string.genre, movie.genre.joinToString(", "))
//                    textViewMovieDescription.text = movie.overview
//                    textViewRating.text = String.format(Locale.getDefault(), "%.1f", movie.rating)
//                    toggleFavorite.isChecked = movie.favorites
//
//                    // שימוש משופר ב-Glide
//                    Glide.with(requireContext())
//                        .load(movie.posterPath)
//                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                        .placeholder(R.drawable.ic_movie_placeholder)
//                        .error(R.drawable.ic_movie_placeholder)
//                        .into(imageViewPoster)
//                }
//            }
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRatingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()

        binding.toggleFavorite.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateFavoriteStatus(args.movieId, isChecked)
        }

        // Navigation to Favorites Fragment
        binding.buttonGoToFavorites.setOnClickListener {
            findNavController().navigate(R.id.action_ratingsFragment_to_favoritesFragment)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            val movie = viewModel.getMovieById(args.movieId)
            if (movie != null) {
                binding.apply {
                    textViewMovieTitle.text = movie.title
                    textViewMovieReleaseDate.text = getString(R.string.movie_release_date, movie.releaseDate)
                    textViewMovieGenre.text = getString(R.string.genre, movie.genre.joinToString(", "))
                    textViewMovieDescription.text = movie.overview
                    textViewRating.text = String.format(Locale.getDefault(), "%.1f", movie.rating)
                    toggleFavorite.isChecked = movie.favorites

                    // שימוש ב-Glide לטעינת התמונה
                    Glide.with(requireContext())
                        .load(movie.posterPath)
                        .placeholder(R.drawable.ic_movie_placeholder)
                        .error(R.drawable.ic_movie_placeholder)
                        .into(imageViewPoster)
                }
            } else {
                binding.textViewMovieTitle.text = getString(R.string.movie_not_found)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}