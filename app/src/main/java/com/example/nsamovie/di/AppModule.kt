package com.example.nsamovie.di

import android.content.Context
import com.example.nsamovie.data.local_db.MovieDao
import com.example.nsamovie.data.local_db.MovieDatabase
import com.example.nsamovie.data.repository.MovieRepository
import com.example.nsamovie.network.RetrofitInstance
import com.example.nsamovie.network.TMDBApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMovieDatabase(@ApplicationContext context: Context): MovieDatabase {
        return MovieDatabase.getDatabase(context)
    }

    @Provides
    fun provideMovieDao(database: MovieDatabase): MovieDao {
        return database.movieDao()
    }

    @Provides
    @Singleton
    fun provideTMDBApiService(): TMDBApiService {
        return RetrofitInstance.api
    }

    @Provides
    @Singleton
    fun provideMovieRepository(
        movieDao: MovieDao,
        apiService: TMDBApiService,
        @ApplicationContext context: Context // Provide the context here
    ): MovieRepository {
        return MovieRepository(movieDao, apiService, context) // Pass context to repository
    }
}
