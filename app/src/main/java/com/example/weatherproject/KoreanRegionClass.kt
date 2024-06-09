package com.example.weatherproject

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class KoreanRegionClass(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    @ColumnInfo(name = "regName1") val regName1: String,
    @ColumnInfo(name = "regName2") val regName2: String,
    @ColumnInfo(name = "regName3") val regName3: String,
    @ColumnInfo(name = "regX") val regX: Int,
    @ColumnInfo(name = "regY") val regY: Int,
    @ColumnInfo(name = "rawX") val rawX: Double?,
    @ColumnInfo(name = "rawY") val rawY: Double?
)