package com.example.weatherproject


data class WeatherClass(
//    @SerializedName("response")
    val response: Response)
data class Response(
//    @SerializedName("header")
    val header: Header,
//    @SerializedName("body")
    val body: Body)
data class Header(
//    @SerializedName("resultCode")
    val resultCode: Int,
//    @SerializedName("resulMsg")
    val resultMsg: String)
data class Body(
//    @SerializedName("dataType")
    val dataType: String,
//    @SerializedName("items")
    val items: Items)
data class Items(val item: List<Item>)

// category : 자료 구분 코드, fcstDate : 예측 날짜, fcstTime : 예측 시간, fcstValue : 예보 값
data class Item(
    val category: String,
    val fcstDate: String,
    val fcstTime: String,
    val fcstValue: String,
    val baseDate: Int,
    val baseTime: Int
)



