package com.example.weatherproject


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WeatherRoomClass(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "baseTime") val basetime: String?,
    @ColumnInfo(name = "baseDate") val basedate: String?,
    @ColumnInfo(name = "humidity") val humidity: Int?,
    @ColumnInfo(name = "temp") val temp: Int?,
    @ColumnInfo(name = "rainRatio") val rainratio: Int?,
    @ColumnInfo(name = "rainType") val raintype: String?,
    @ColumnInfo(name = "Nx") val nx: Int?,
    @ColumnInfo(name = "Ny") val ny: Int?,
    @ColumnInfo(name = "fcstDate") val fcstdate: String?,
    @ColumnInfo(name = "fcstTime") val fcsttime: String?,
    @ColumnInfo(name = "baseD+1") val baseD1: String?,
    @ColumnInfo(name = "baseD+2") val baseD2: String?,
)