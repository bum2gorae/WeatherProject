package com.example.weatherproject


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


//    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    fun findByName(first: String, last: String): User


@Dao
interface UserDao {
    @Query("SELECT * FROM WeatherRoomColumns")
    fun getAll(): Flow<List<WeatherRoomColumns>>

    @Query("SELECT * FROM WeatherRoomColumns WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): Flow<List<WeatherRoomColumns>>

    @Insert
    fun insertAll(vararg users: WeatherRoomColumns)

    @Delete
    fun delete(user: WeatherRoomColumns)

    @Query("SELECT `temp` FROM WeatherRoomColumns")
    fun getTemp(): Int
}