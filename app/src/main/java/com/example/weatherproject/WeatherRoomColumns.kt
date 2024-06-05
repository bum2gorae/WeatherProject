package com.example.weatherproject


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WeatherRoomColumns(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "basetime") val basetime: String?,
    @ColumnInfo(name = "basedate") val basedate: String?,
    @ColumnInfo(name = "humidity") val humidity: String?,
    @ColumnInfo(name = "temp") val temp: Int?,
    @ColumnInfo(name = "rainratio") val rainratio: String?,
    @ColumnInfo(name = "raintype") val raintype: String?
)