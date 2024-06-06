package com.example.weatherproject


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WeatherRoomColumns(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "baseTime") val basetime: String?,
    @ColumnInfo(name = "baseDate") val basedate: String?,
    @ColumnInfo(name = "humidity") val humidity: String?,
    @ColumnInfo(name = "temp") val temp: Int?,
    @ColumnInfo(name = "rainRatio") val rainratio: String?,
    @ColumnInfo(name = "rainType") val raintype: String?,
    @ColumnInfo(name = "Nx") val nx: Int,
    @ColumnInfo(name = "Ny") val ny: Int?,
    @ColumnInfo(name = "fcstDate") val fcstdate: String?,
    @ColumnInfo(name = "fcstTime") val fcsttime: String?
)