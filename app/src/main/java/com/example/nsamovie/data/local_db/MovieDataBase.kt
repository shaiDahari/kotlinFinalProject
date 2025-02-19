package com.example.nsamovie.data.local_db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.nsamovie.data.model.Movie

@Database(entities = [Movie::class], version = 1, exportSchema = false)
abstract class MovieDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao

    companion object {
        @Volatile
        private var INSTANCE: MovieDatabase? = null

        fun getDatabase(context: Context): MovieDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MovieDatabase::class.java,
                    "movie_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
    @Database(entities = [Movie::class], version = 1, exportSchema = false)
    @TypeConverters(Converters::class) // הוספת ה-Converter לשימוש
    abstract class MovieDatabase : RoomDatabase() {
        abstract fun movieDao(): MovieDao
    }

}
