package com.example.weatherproject

import com.google.gson.annotations.SerializedName


data class ModelWeather (
    @SerializedName("rainType") var rainType: String = "",      // 강수 형태
    @SerializedName("humidity") var humidity: String = "",      // 습도
    @SerializedName("sky") var sky: String = "",           // 하능 상태
    @SerializedName("temp") var temp: String = "",          // 기온
    @SerializedName("fcstTime") var fcstTime: String = "",      // 예보시각
)

//data class WEATHER (val response : RESPONSE)
//data class RESPONSE(val header : HEADER, val body : BODY)
//data class HEADER(val resultCode : Int, val resultMsg : String)
//data class BODY(val dataType : String, val items : ITEMS, val totalCount : Int)
//data class ITEMS(val item : List<ITEM>)
//
//// category : 자료 구분 코드, fcstDate : 예측 날짜, fcstTime : 예측 시간, fcstValue : 예보 값
//data class ITEM(val category : String, val fcstDate : String, val fcstTime : String, val fcstValue : String)



