package com.example.nsamovie.ui.search

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.nsamovie.R
import com.example.nsamovie.databinding.FragmentSearchMoviesBinding
import com.example.nsamovie.data.model.Movie
import com.example.nsamovie.ui.viewmodel.MoviesViewModel
import java.util.*

class SearchMoviesFragment : Fragment() {

    private var _binding: FragmentSearchMoviesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MoviesViewModel by viewModels()
    private val args: SearchMoviesFragmentArgs by navArgs()

    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchMoviesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        if (args.movieId != -1) {
            populateMovieDetails()
        }
    }

    private fun setupListeners() {
        binding.buttonSelectPoster.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 100)
        }

        binding.buttonSelectReleaseDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.buttonSaveMovie.setOnClickListener {
            if (validateInputs()) {
                saveMovie()
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                binding.buttonSelectReleaseDate.text = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun validateInputs(): Boolean {
        if (binding.editTitle.text.isNullOrEmpty()) {
            Toast.makeText(requireContext(), R.string.error_empty_title, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveMovie() {
        val movie = Movie(
            id = if (args.movieId != -1) args.movieId else 0,
            title = binding.editTitle.text.toString(),
            director = binding.editDirector.text.toString(),
            releaseDate = binding.buttonSelectReleaseDate.text.toString(),
            description = binding.editDescription.text.toString(),
            posterUri = imageUri?.toString(),
            rating = binding.ratingBar.rating
        )

        if (args.movieId != -1) {
            viewModel.update(movie)
            Toast.makeText(requireContext(), R.string.msg_edit_success, Toast.LENGTH_SHORT).show()
        } else {
            viewModel.insert(movie)
            Toast.makeText(requireContext(), R.string.msg_save_success, Toast.LENGTH_SHORT).show()
        }

        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

