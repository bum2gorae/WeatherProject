package com.example.weatherproject

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WeatherRoomClass::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): WeatherDao

    companion object {
        @Volatile
        private var instance : AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return instance ?:
            Room.databaseBuilder(
                context,
                AppDatabase::class.java, "contact_weather.db"
            ).build()
                .also { instance = it }
        }
    }
}

@Database(entities = [RegionRoomClass::class], version = 1)
abstract class AppDatabaseReg : RoomDatabase() {
    abstract fun RegDao(): RegDao

    companion object {
        @Volatile
        private var instance : AppDatabaseReg? = null

        fun getDatabase(context: Context): AppDatabaseReg {
            return instance ?:
            Room.databaseBuilder(
                context,
                AppDatabaseReg::class.java, "contact_region.db"
            ).build()
                .also { instance = it }
        }
    }
}

@Database(entities = [DustRoomClass::class], version = 1)
abstract class AppDatabaseDust : RoomDatabase() {
    abstract fun DustDao(): DustDao

    companion object {
        @Volatile
        private var instance : AppDatabaseDust? = null

        fun getDatabase(context: Context): AppDatabaseDust {
            return instance ?:
            Room.databaseBuilder(
                context,
                AppDatabaseDust::class.java, "contact_dust.db"
            ).build()
                .also { instance = it }
        }
    }
}