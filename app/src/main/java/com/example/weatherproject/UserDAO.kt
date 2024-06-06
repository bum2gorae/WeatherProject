package com.example.weatherproject


import androidx.room.ColumnInfo
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

    @Query("DELETE FROM WeatherRoomColumns")
    fun clearAll()

    @Query("SELECT `temp` FROM WeatherRoomColumns")
    fun getTemp(): Int

    fun insertForecastFactors(vararg forecastFactors: ForecastFactor) {
        val weatherRoomColumns = forecastFactors.map { forecastFactor ->
            WeatherRoomColumns(
                basetime = forecastFactor.baseTime,
                basedate = forecastFactor.baseDate,
                humidity = forecastFactor.humidity,
                temp = forecastFactor.temp,
                rainratio = forecastFactor.rainRatio,
                raintype = forecastFactor.rainType,
                nx = forecastFactor.actNx,
                ny = forecastFactor.actNy,
                fcstdate = forecastFactor.fcstDate,
                fcsttime = forecastFactor.fcstTime
            )
        }
        insertAll(*weatherRoomColumns.toTypedArray())
    }

    @Query("SELECT AVG(`temp`) FROM WeatherRoomColumns WHERE fcstDate = baseDate")
    fun getTempAvg() : Int
}