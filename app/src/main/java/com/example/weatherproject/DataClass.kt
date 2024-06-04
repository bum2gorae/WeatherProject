package com.example.weatherproject

import com.google.gson.annotations.SerializedName


data class ModelWeather (
    @SerializedName("rainType") var rainType: String = "",      // 강수 형태
    @SerializedName("humidity") var humidity: String = "",      // 습도
    @SerializedName("sky") var sky: String = "",           // 하능 상태
    @SerializedName("temp") var temp: String = "",          // 기온
    @SerializedName("fcstTime") var fcstTime: String = "",      // 예보시각
)

data class WEATHERCLASS(
    @SerializedName("response") val response: RESPONSE)
data class RESPONSE(
    @SerializedName("header") val header: HEADER,
    @SerializedName("body") val body: BODY)
data class HEADER(
    @SerializedName("resultCode") val resultCode: Int,
    @SerializedName("resulMsg") val resultMsg: String)
data class BODY(
    @SerializedName("dataType") val dataType: String,
    @SerializedName("items")val items: ITEMS)
data class ITEMS(val item: List<ITEM>)



