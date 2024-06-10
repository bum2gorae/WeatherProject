package com.example.weatherproject

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DustRoomClass(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "baseDate") val basedate : String?,
    @ColumnInfo(name = "baseD+1") val baseD1 : String?,
    @ColumnInfo(name = "informCode") val informCode : String?,
    @ColumnInfo(name = "informRegion") val informRegion : String?,
    @ColumnInfo(name = "informGrade") val informGrade : String?,
    @ColumnInfo(name = "informData") val informData : String?
)