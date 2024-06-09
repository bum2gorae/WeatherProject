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
    @Query("SELECT * FROM WeatherRoomClass")
    fun getAll(): Flow<List<WeatherRoomClass>>

    @Query("SELECT * FROM WeatherRoomClass WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): Flow<List<WeatherRoomClass>>

    @Insert
    fun insertAll(vararg users: WeatherRoomClass)

    @Delete
    fun delete(user: WeatherRoomClass)

    @Query("DELETE FROM WeatherRoomClass")
    fun clearAll()

    @Query("SELECT `temp` FROM WeatherRoomClass")
    fun getTemp(): Flow<Int>

    fun insertForecastFactors(vararg forecastFactors: ForecastFactor) {
        val weatherRoomColumns = forecastFactors.map { forecastFactor ->
            WeatherRoomClass(
                basetime = forecastFactor.baseTime,
                basedate = forecastFactor.baseDate,
                humidity = forecastFactor.humidity,
                temp = forecastFactor.temp,
                rainratio = forecastFactor.rainRatio,
                raintype = forecastFactor.rainType,
                nx = forecastFactor.actNx,
                ny = forecastFactor.actNy,
                fcstdate = forecastFactor.fcstDate,
                fcsttime = forecastFactor.fcstTime,
                baseD1 = forecastFactor.baseD1,
                baseD2 = forecastFactor.baseD2,
            )
        }
        insertAll(*weatherRoomColumns.toTypedArray())
    }

    @Query("SELECT AVG(`temp`) FROM WeatherRoomClass WHERE fcstDate = baseDate")
    fun getTempAvg() : Flow<Int>

    @Query("SELECT AVG(`temp`) FROM WeatherRoomClass WHERE fcstDate = `baseD+1`")
    fun getTempAvgD1() : Flow<Int>

    @Query("SELECT AVG(`temp`) FROM WeatherRoomClass WHERE fcstDate = `baseD+2`")
    fun getTempAvgD2() : Flow<Int>

    @Query("SELECT MAX(rainRatio) FROM WeatherRoomClass WHERE fcstDate = baseDate")
    fun getRainMax() : Flow<Int>

    @Query("SELECT MAX(rainRatio) FROM WeatherRoomClass WHERE fcstDate = `baseD+1`")
    fun getRainMaxD1() : Flow<Int>

    @Query("SELECT MAX(rainRatio) FROM WeatherRoomClass WHERE fcstDate = `baseD+2`")
    fun getRainMaxD2() : Flow<Int>

}

@Dao
interface RegDao {
    @Insert
    fun insertAll(vararg users: KoreanRegionClass)

    @Query("DELETE FROM KoreanRegionClass")
    fun clearAll()

    @Query("""
        SELECT (regName1 || ' ' || regName2 || ' ' || regName3) as combinedNames
        FROM KoreanRegionClass
        ORDER BY ((CAST(rawX AS DOUBLE) - :nx) * (CAST(rawX AS DOUBLE) - :nx) + (CAST(rawY AS DOUBLE) - :ny) * (CAST(rawY AS DOUBLE) - :ny)) ASC
        LIMIT 1
    """)
    fun getNearestRegion(nx: Double, ny: Double): Flow<String>
}

@Dao
interface DustDao {
    @Insert
    fun insertAll(vararg users: DustRoomClass)

    @Query("DELETE FROM DustRoomClass")
    fun clearAll()

    fun insertDustFactors(vararg dustFactor: DustFactor) {
        val dustRoomClass = dustFactor.map { dustFactor ->
            DustRoomClass(
                basedate = dustFactor.baseDate,
                baseD1 = dustFactor.baseD1,
                informRegion = dustFactor.informRegion,
                informGrade = dustFactor.informGrade,
                informDate = dustFactor.informDate,
                informD1 = dustFactor.informD1
            )
        }
        insertAll(*dustRoomClass.toTypedArray())
    }
}