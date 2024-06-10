package com.example.weatherproject

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat
import com.google.gson.annotations.SerializedName

// JSON Parsing
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

data class DustParsingClass(
    @SerializedName("response")
    val Dustresponse: DustResponse
)

data class DustResponse(
    @SerializedName("body")
    val Dustbody: Dustbody
)

data class Dustbody(
    @SerializedName("items")
    val DustItem: List<DustItem>
)

data class DustItem(
    @SerializedName("informCode")
    val DustInformCode: String,
    @SerializedName("informData")
    val DustInformData: String,
    @SerializedName("informGrade")
    val DustInformGrade: String
)


// RoomInput용 class
data class ForecastFactor(
    val baseTime: String,
    val baseDate: String,
    val fcstTime: String,
    val fcstDate: String,
    val actNx: Int,
    val actNy: Int,
    var rainRatio: Int,
    var rainType: String,
    var humidity: Int,
    var sky: String,
    var temp: Int,
    val baseD1: String,
    val baseD2: String
)

// MainScreen용 class
class MainScreenData() {
    var viewRain: String = ""
    var viewTemp: String = ""
    var imageRain: Int = R.drawable.transparent
    var textRain: String = ""
    var imageTemp: Int = R.drawable.transparent
    var textTemp: String = ""
    var dayCheck: String = ""
    var imageDetail : Int = R.drawable.transparent
    var viewDust: String = ""
    var imageDust: Int = R.drawable.transparent
    var textDust: String = ""
}

// 지역 Data용 class
class RegionData(
    var nx: Double,
    var ny: Double,
    var regionNameNow: String,
    var baseDate: String
)



data class DustFactor(
    val baseDate: String,
    val baseD1 : String,
    val informCode: String?,
    val informRegion: String?,
    val informGrade: String?,
    val informDate: String?
)

//data class DustDataParsing