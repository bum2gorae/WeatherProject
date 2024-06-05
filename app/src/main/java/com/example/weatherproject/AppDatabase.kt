package com.example.weatherproject

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WeatherRoomColumns::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var instance : AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return instance ?:
            Room.databaseBuilder(
                context,
                AppDatabase::class.java, "contact.db"
            ).build()
                .also { instance = it }
        }
    }
}