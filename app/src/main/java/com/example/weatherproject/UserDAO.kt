package com.example.weatherproject


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface WeatherDao {
    @Query("SELECT * FROM WeatherRoomClass")
    fun getAll(): Flow<List<WeatherRoomClass>>

    @Query("SELECT fcstDate, fcstTime, `temp`, rainRatio FROM WeatherRoomClass")
    fun getAllDetails(): Flow<List<WeatherDetails>>

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
    fun getTempAvg(): Flow<Int>

    @Query("SELECT AVG(`temp`) FROM WeatherRoomClass WHERE fcstDate = `baseD+1`")
    fun getTempAvgD1(): Flow<Int>

    @Query("SELECT AVG(`temp`) FROM WeatherRoomClass WHERE fcstDate = `baseD+2`")
    fun getTempAvgD2(): Flow<Int>

    @Query("SELECT MAX(rainRatio) FROM WeatherRoomClass WHERE fcstDate = baseDate")
    fun getRainMax(): Flow<Int>

    @Query("SELECT MAX(rainRatio) FROM WeatherRoomClass WHERE fcstDate = `baseD+1`")
    fun getRainMaxD1(): Flow<Int>

    @Query("SELECT MAX(rainRatio) FROM WeatherRoomClass WHERE fcstDate = `baseD+2`")
    fun getRainMaxD2(): Flow<Int>

}

@Dao
interface RegDao {
    @Insert
    fun insertAll(vararg users: RegionRoomClass)

    @Query("DELETE FROM RegionRoomClass")
    fun clearAll()

    @Query(
        """
        SELECT regName1 || ' ' || regName2 || ' ' || regName3 AS combinedNames 
        FROM RegionRoomClass 
        ORDER BY Euclidian ASC 
        LIMIT 1
    """
    )
    fun getRegionWithMinEuclidian(): Flow<String>

    @Query(
        """
        SELECT regName2
        FROM RegionRoomClass 
        ORDER BY Euclidian ASC 
        LIMIT 1
    """
    )
    fun getRegion2(): Flow<String>
}

@Dao
interface DustDao {
    @Insert
    fun insertAll(vararg users: DustRoomClass)

    @Query("DELETE FROM DustRoomClass")
    fun clearAll()

    fun insertDustFactors(vararg dustFactors: DustFactor) {
        val dustRoomClass = dustFactors.map { dustFactor ->
            DustRoomClass(
                basedate = dustFactor.baseDate,
                baseD1 = dustFactor.baseD1,
                informCode = dustFactor.informCode,
                informRegion = dustFactor.informRegion,
                informGrade = dustFactor.informGrade,
                informData = dustFactor.informDate
            )
        }
        insertAll(*dustRoomClass.toTypedArray())
    }

    @Query("SELECT informGrade FROM DustRoomClass WHERE informData=:baseDate AND informRegion=:region AND informCode='PM10'")
    fun getDust10(baseDate: String, region: String): Flow<String>

    @Query("SELECT informGrade FROM DustRoomClass WHERE informData=:baseD1 AND informRegion=:region AND informCode='PM10'")
    fun getDust10D1(baseD1: String, region: String): Flow<String>

    @Query("SELECT informGrade FROM DustRoomClass WHERE informData=:baseDate AND informRegion=:region AND informCode='PM25'")
    fun getDust25(baseDate: String, region: String): Flow<String>

    @Query("SELECT informGrade FROM DustRoomClass WHERE informData=:baseD1 AND informRegion=:region AND informCode='PM25'")
    fun getDust25D1(baseD1: String, region: String): Flow<String>
}