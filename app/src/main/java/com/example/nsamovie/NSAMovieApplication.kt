package com.example.nsamovie


import android.app.Application
import com.example.nsamovie.data.local_db.MovieDatabase
import com.example.nsamovie.data.repository.MovieRepository
import com.example.nsamovie.network.RetrofitInstance

import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NSAMovieApplication : Application()