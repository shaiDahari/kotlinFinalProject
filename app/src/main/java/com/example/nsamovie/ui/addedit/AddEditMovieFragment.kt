package com.example.nsamovie.ui.addedit

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.nsamovie.R
import com.example.nsamovie.data.local_db.MovieDatabase
import com.example.nsamovie.data.model.Movie
import com.example.nsamovie.data.repository.MovieRepository
import com.example.nsamovie.databinding.FragmentAddEditMovieBinding
import com.example.nsamovie.ui.viewmodel.MoviesViewModel
import java.util.*

class AddEditMovieFragment : Fragment() {
    private var _binding: FragmentAddEditMovieBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MoviesViewModel by viewModels {
        val database = MovieDatabase.getDatabase(requireContext())
        val repository = MovieRepository(database.movieDao())
        MoviesViewModel.Factory(repository)
    }

    private val args: AddEditMovieFragmentArgs by navArgs()

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditMovieBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            binding.titleEditText.setText(savedInstanceState.getString("title"))
            binding.directorEditText.setText(savedInstanceState.getString("director"))
            binding.genreEditText.setText(savedInstanceState.getString("genre"))
            binding.descriptionEditText.setText(savedInstanceState.getString("description"))
            binding.buttonSelectReleaseDate.text = savedInstanceState.getString("releaseDate")
            binding.ratingBar.rating = savedInstanceState.getFloat("rating")

            val imageUriString = savedInstanceState.getString("imageUri")
            if (!imageUriString.isNullOrEmpty()) {
                imageUri = Uri.parse(imageUriString)
                binding.moviePosterImage.setImageURI(imageUri)
            }
        } else {
            setupInitialState()
        }

        setupListeners()

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                openGallery()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.gallery_access_required),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val selectedImageUri: Uri? = result.data?.data
                selectedImageUri?.let { uri ->
                    imageUri = uri
                    binding.moviePosterImage.setImageURI(imageUri)

                    requireContext().contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
            }
        }
    }




    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("title", binding.titleEditText.text.toString())
        outState.putString("director", binding.directorEditText.text.toString())
        outState.putString("genre", binding.genreEditText.text.toString())
        outState.putString("description", binding.descriptionEditText.text.toString())
        outState.putString("releaseDate", binding.buttonSelectReleaseDate.text.toString())
        outState.putFloat("rating", binding.ratingBar.rating)
        imageUri?.let { outState.putString("imageUri", it.toString()) }
    }



    private fun setupInitialState() {
        if (args.movieId != -1) {
            viewModel.getMovieById(args.movieId)
            viewModel.selectedMovie.observe(viewLifecycleOwner) { movie ->
                movie?.let { populateFields(it) }
            }
        }
    }

    private fun populateFields(movie: Movie) {
        binding.apply {
            titleEditText.setText(movie.title)
            directorEditText.setText(movie.director)
            descriptionEditText.setText(movie.description)
            buttonSelectReleaseDate.text = movie.releaseDate
            ratingBar.rating = movie.rating
            genreEditText.setText(movie.genre)

            if (!movie.posterPath.isNullOrEmpty()) {
                val uri = Uri.parse(movie.posterPath)
                if (isUriValid(uri)) {
                    // Load the image if URI is valid
                    binding.moviePosterImage.setImageURI(uri)
                    imageUri = uri
                } else {
                    binding.moviePosterImage.setImageResource(R.drawable.ic_movie_placeholder)
                    imageUri = null

                }
            } else {
                binding.moviePosterImage.setImageResource(R.drawable.ic_movie_placeholder)
                imageUri = null
            }
        }
    }

    private fun isUriValid(uri: Uri): Boolean {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            inputStream?.close()
            true
        } catch (e: Exception) {
            false
        }
    }


    private fun setupListeners() {
        binding.btnAddPhoto.setOnClickListener {
            checkPermissionAndOpenGallery()
        }

        binding.buttonSelectReleaseDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.saveButton.setOnClickListener {
            if (validateInputs()) {
                saveMovie()
            }
        }
    }

    private fun checkPermissionAndOpenGallery() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openGallery()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format(
                    Locale.getDefault(),
                    "%02d/%02d/%04d",
                    dayOfMonth,
                    month + 1,
                    year
                )
                binding.buttonSelectReleaseDate.text = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        binding.apply {
            if (titleEditText.text.isNullOrEmpty()) {
                titleEditText.error = getString(R.string.error_empty_title)
                isValid = false
            }

            if (directorEditText.text.isNullOrEmpty()) {
                directorEditText.error = getString(R.string.error_empty_director)
                isValid = false
            }

            if (buttonSelectReleaseDate.text.isNullOrEmpty() || buttonSelectReleaseDate.text == getString(
                    R.string.movie_release_date
                )
            ) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_invalid_date),
                    Toast.LENGTH_SHORT
                ).show()
                isValid = false
            }
        }
        return isValid
    }

    private fun saveMovie() {
        val movie = Movie(
            id = if (args.movieId != -1) args.movieId else 0,
            title = binding.titleEditText.text.toString(),
            director = binding.directorEditText.text.toString(),
            releaseDate = binding.buttonSelectReleaseDate.text.toString(),
            description = binding.descriptionEditText.text.toString(),
            posterPath = imageUri?.toString(),
            rating = binding.ratingBar.rating,
            genre = binding.genreEditText.text.toString()
        )

        if (args.movieId != -1) {
            viewModel.update(movie)
            showToast(R.string.msg_edit_success)
        } else {
            viewModel.insert(movie)
            showToast(R.string.msg_save_success)
        }

        findNavController().navigateUp()
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
