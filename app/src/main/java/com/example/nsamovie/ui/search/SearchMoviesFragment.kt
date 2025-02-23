package com.example.nsamovie.ui.search

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nsamovie.R
import com.example.nsamovie.databinding.FragmentSearchMoviesBinding
import com.example.nsamovie.ui.adapter.SmallMovieAdapter
import com.example.nsamovie.ui.viewmodel.MoviesViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

@AndroidEntryPoint
class SearchMoviesFragment : Fragment() {

    private var _binding: FragmentSearchMoviesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MoviesViewModel by viewModels()
    private lateinit var movieAdapter: SmallMovieAdapter

    // Initialize location provider
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val LOCATION_REQUEST_CODE = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchMoviesBinding.inflate(inflater, container, false)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchFunctionality()
        setupYearRangePicker()
        setupLocationSearch()

        // Observe movie list LiveData and update UI
        viewModel.movieList.observe(viewLifecycleOwner) { movies ->
            if (movies.isNotEmpty()) {
                movieAdapter.setMovies(movies.toList())
                binding.emptyStateText.visibility = View.GONE
                binding.recyclerViewMovies.visibility = View.VISIBLE
            } else {
                Log.d("SearchMoviesFragment", "No movies found.")
                // Optionally show a message prompting the user to search
                binding.emptyStateText.visibility = View.VISIBLE
                binding.recyclerViewMovies.visibility = View.GONE
            }
        }

        // Remove the automatic loadMovies() call if you don't want an initial list:
        // viewModel.loadMovies() // <-- Remove or comment out this line
    }


    private fun setupRecyclerView() {
        movieAdapter = SmallMovieAdapter(mutableListOf()) { movieId ->
            findNavController().navigate(
                R.id.action_searchMoviesFragment_to_ratingsFragment,
                Bundle().apply { putInt("movieId", movieId) }
            )
        }
        binding.recyclerViewMovies.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = movieAdapter
        }
    }

    private fun setupSearchFunctionality() {
        binding.searchEditText.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = textView.text.toString()
                if (query.isNotEmpty()) {
                    viewModel.searchMovies(query)
                    showLoadingState()
                }
                true
            } else {
                false
            }
        }
    }

    private fun setupYearRangePicker() {
        binding.dateButton.setOnClickListener { showYearRangeDialog() }
    }

    private fun showYearRangeDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_year_range_picker)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val startYearPicker = dialog.findViewById<NumberPicker>(R.id.startYearPicker)
        val endYearPicker = dialog.findViewById<NumberPicker>(R.id.endYearPicker)

        startYearPicker.minValue = 1900
        startYearPicker.maxValue = currentYear
        endYearPicker.minValue = 1900
        endYearPicker.maxValue = currentYear

        startYearPicker.value = currentYear - 10
        endYearPicker.value = currentYear

        dialog.findViewById<View>(R.id.btnApply).setOnClickListener {
            val startYear = startYearPicker.value
            val endYear = endYearPicker.value
            if (startYear <= endYear) {
                viewModel.searchMoviesByYearRange(startYear, endYear)
                showLoadingState()
                dialog.dismiss()
            }
        }

        dialog.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun setupLocationSearch() {
        binding.locationButton.setOnClickListener {
            Log.d("SearchMoviesFragment", "Location button clicked")
            checkLocationPermissionAndSearch()
        }
    }

    private fun checkLocationPermissionAndSearch() {
        Log.d("SearchMoviesFragment", "Checking location permissions")
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("SearchMoviesFragment", "Permission already granted, getting location")
                getLocationAndSearchMovies()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Location Permission Needed")
                    .setMessage("This app requires location permission to provide location-based movie searches.")
                    .setPositiveButton("OK") { _, _ ->
                        requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            LOCATION_REQUEST_CODE
                        )
                    }
                    .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
            else -> {
                Log.d("SearchMoviesFragment", "Requesting location permission")
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_REQUEST_CODE
                )
            }
        }
    }

    private fun getLocationAndSearchMovies() {
        Log.d("SearchMoviesFragment", "Fetching location...")
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("SearchMoviesFragment", "Location permission not granted! Requesting again.")
            Toast.makeText(requireContext(), "Location permission is required", Toast.LENGTH_SHORT).show()
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            return
        }
        if (!::fusedLocationProviderClient.isInitialized) {
            Log.e("SearchMoviesFragment", "fusedLocationProviderClient is not initialized!")
            Toast.makeText(requireContext(), "Location service is not initialized", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    Log.d("SearchMoviesFragment", "Location retrieved: Lat=${location.latitude}, Lon=${location.longitude}")
                    lifecycleScope.launch {
                        try {
                            if (!Geocoder.isPresent()) {
                                Log.e("SearchMoviesFragment", "Geocoder not available on this device!")
                                Toast.makeText(requireContext(), "Geocoder not available", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            val geocoder = Geocoder(requireContext(), Locale.getDefault())
                            val addresses = withContext(Dispatchers.IO) {
                                try {
                                    geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                } catch (e: IOException) {
                                    Log.e("SearchMoviesFragment", "Geocoder failed: ${e.message}")
                                    null
                                }
                            }
                            val countryCode = addresses?.firstOrNull()?.countryCode
                            if (countryCode != null) {
                                Log.d("SearchMoviesFragment", "Country code detected: $countryCode")
                                val languageCode = Locale.getDefault().toLanguageTag()
                                Log.d("SearchMoviesFragment", "Language code detected: $languageCode")
                                viewModel.searchMoviesByRegionAndLanguage(region = countryCode, language = languageCode)
                                showLoadingState()
                            } else {
                                Log.e("SearchMoviesFragment", "Unable to determine country code")
                                Toast.makeText(requireContext(), "Unable to determine country code", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: IOException) {
                            Log.e("SearchMoviesFragment", "Failed to retrieve location: ${e.message}")
                            Toast.makeText(requireContext(), "Failed to retrieve location", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("SearchMoviesFragment", "Location not available, requesting new location...")
                    requestNewLocation()
                }
            }.addOnFailureListener { exception ->
                Log.e("SearchMoviesFragment", "Error retrieving location: ${exception.message}")
                Toast.makeText(requireContext(), "Error retrieving location: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Log.e("SearchMoviesFragment", "Security Exception: ${e.message}")
            Toast.makeText(requireContext(), "Security Exception: Location access denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestNewLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("SearchMoviesFragment", "Cannot request new location, permission not granted!")
            return
        }
        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 5000
        ).apply {
            setWaitForAccurateLocation(true)
            setMinUpdateIntervalMillis(2000)
        }.build()
        val locationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    Log.d("SearchMoviesFragment", "Fresh location retrieved: Lat=${location.latitude}, Lon=${location.longitude}")
                    fusedLocationProviderClient.removeLocationUpdates(this)
                    getLocationAndSearchMovies()
                }
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun showLoadingState() {
        binding.apply {
            recyclerViewMovies.visibility = View.GONE
            emptyStateText.visibility = View.VISIBLE
            emptyStateText.text = getString(R.string.loading)
        }
    }

    private fun showResults() {
        binding.apply {
            recyclerViewMovies.visibility = View.VISIBLE
            emptyStateText.visibility = View.GONE
        }
    }

    private fun showEmptyState() {
        binding.apply {
            recyclerViewMovies.visibility = View.GONE
            emptyStateText.visibility = View.VISIBLE
            emptyStateText.text = getString(R.string.no_results_found)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("SearchMoviesFragment", "Location permission granted")
                getLocationAndSearchMovies()
            } else {
                Log.e("SearchMoviesFragment", "Location permission denied")
                Toast.makeText(requireContext(), "Location permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
