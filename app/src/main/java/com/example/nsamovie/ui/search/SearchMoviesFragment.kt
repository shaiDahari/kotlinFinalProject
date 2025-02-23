package com.example.nsamovie.ui.search

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nsamovie.R
import com.example.nsamovie.databinding.FragmentSearchMoviesBinding
import com.example.nsamovie.ui.adapter.SmallMovieAdapter
import com.example.nsamovie.ui.viewmodel.MoviesViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar

@AndroidEntryPoint
class SearchMoviesFragment : Fragment() {

    private var _binding: FragmentSearchMoviesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MoviesViewModel by viewModels()
    private lateinit var movieAdapter: SmallMovieAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchMoviesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchFunctionality()
        setupYearRangePicker()
        setupLocationSearch()
        observeMovies()
    }

    private fun setupRecyclerView() {
        movieAdapter = SmallMovieAdapter(
            movies = mutableListOf(),
            onMovieClick = { movieId ->
                findNavController().navigate(
                    R.id.action_searchMoviesFragment_to_ratingsFragment,
                    Bundle().apply {
                        putInt("movieId", movieId)
                    }
                )
            }
        )

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
        binding.dateButton.setOnClickListener {
            showYearRangeDialog()
        }
    }

    private fun showYearRangeDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_year_range_picker)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val startYearPicker = dialog.findViewById<NumberPicker>(R.id.startYearPicker)
        val endYearPicker = dialog.findViewById<NumberPicker>(R.id.endYearPicker)

        // הגדרת טווח שנים אפשרי (למשל מ-1900 עד השנה הנוכחית)
        startYearPicker.minValue = 1900
        startYearPicker.maxValue = currentYear
        endYearPicker.minValue = 1900
        endYearPicker.maxValue = currentYear

        // ערכי ברירת מחדל
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

        dialog.findViewById<View>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupLocationSearch() {
        binding.locationButton.setOnClickListener {
            showCountryLanguageDialog()
        }
    }

    private fun showCountryLanguageDialog() {
        val countries = resources.getStringArray(R.array.countries)
        val countryCodes = resources.getStringArray(R.array.country_codes)
        val languageCodes = resources.getStringArray(R.array.language_codes)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.select_country))
            .setItems(countries) { dialog, which ->
                val selectedCountryCode = countryCodes[which]
                val selectedLanguageCode = languageCodes[which]
                viewModel.searchMoviesByRegionAndLanguage(
                    region = selectedCountryCode,
                    language = selectedLanguageCode
                )
                showLoadingState()
                binding.searchEditText.setText("")
                dialog.dismiss()
            }
            .setNegativeButton(getString(android.R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun observeMovies() {
        viewModel.movieList.observe(viewLifecycleOwner) { movies ->
            if (movies.isNotEmpty()) {
                movieAdapter.setMovies(movies)
                showResults()
            } else {
                showEmptyState()
            }
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}