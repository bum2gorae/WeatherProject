package com.example.weatherproject

import com.google.gson.annotations.SerializedName


data class WeatherClass(
    @SerializedName("response")
    val response: Response
)

data class Response(
    @SerializedName("header")
    val header: Header,
    @SerializedName("body")
    val body: Body
)

data class Header(
    @SerializedName("resultCode")
    val resultCode: Int,
    @SerializedName("resulMsg")
    val resultMsg: String
)

data class Body(
    @SerializedName("dataType")
    val dataType: String,
    @SerializedName("items")
    val items: Items,
    @SerializedName("pageNo")
    val pageNo: Int,
    @SerializedName("numOfRows")
    val numOfRows: Int,
    @SerializedName("totalCount")
    val totalCount: Int
)

data class Items(val item: List<Item>)

// category : 자료 구분 코드, fcstDate : 예측 날짜, fcstTime : 예측 시간, fcstValue : 예보 값
data class Item(
    val category: String,
    val fcstDate: String,
    val fcstTime: String,
    val fcstValue: String,
    val baseDate: String,
    val baseTime: String,
    val nx: Int,
    val ny: Int
)

data class ForecastFactor(
    val baseTime : String,
    val baseDate : String,
    val fcstTime : String,
    val fcstDate : String,
    val actNx : Int,
    val actNy : Int,
    var rainRatio: Int,
    var rainType : String,
    var humidity : Int,
    var sky: String,
    var temp : Int
)


