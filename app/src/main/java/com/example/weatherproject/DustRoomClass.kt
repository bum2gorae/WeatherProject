package com.example.weatherproject

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DustRoomClass(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "baseDate") val basedate : String?,
    @ColumnInfo(name = "baseD+1") val baseD1 : String?,
    @ColumnInfo(name = "imformRegion") val informRegion : String?,
    @ColumnInfo(name = "informGrade") val informGrade : String?,
    @ColumnInfo(name = "informDate") val informDate : String?,
    @ColumnInfo(name = "informD+1") val informD1 : String?
)