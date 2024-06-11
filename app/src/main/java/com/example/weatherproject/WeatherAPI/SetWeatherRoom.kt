package com.example.weatherproject.WeatherAPI

import android.content.Context
import android.util.Log
import com.example.weatherproject.AppDatabase
import com.example.weatherproject.ForecastFactor
import com.example.weatherproject.WeatherClass
import com.example.weatherproject.WeatherIF
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tan

// retrofit builder & object for 기상청
private val retrofit = Retrofit.Builder()
    .baseUrl("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

object ApiObject {
    val retrofitService: WeatherIF by lazy {
        retrofit.create(WeatherIF::class.java)
    }
}


// 기상청 data room input
fun setWeather(
    lat: Double,
    lon: Double,
    cal: Calendar,
    baseDate: String,
    baseD1: String,
    baseD2: String,
    contextDB: Context
) {
    val dbWeather = AppDatabase.getDatabase(contextDB)
    val timeFormat = SimpleDateFormat("HH", Locale.KOREA)
    timeFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
    val timeNow = timeFormat.format(cal.time) // 현재 시간
    val baseTime = getTime(timeNow)

    Log.d("data", "$baseTime, $baseDate")

    val (nx, ny) = getXY(lat, lon)

    // 날씨 정보 가져오기
    // (응답 자료 형식-"JSON", 한 페이지 결과 수 = 10, 페이지 번호 = 1, 발표 날싸, 발표 시각, 예보지점 좌표)
    val call = ApiObject.retrofitService.getWeather(
        4,
        1,
        "JSON",
        baseDate,
        baseTime,
        nx,
        ny
    )

    call.enqueue(object : retrofit2.Callback<WeatherClass> {
        // 응답 성공 시
        override fun onResponse(call: Call<WeatherClass>, response: Response<WeatherClass>) {
            if (response.isSuccessful) {
                val scope = CoroutineScope(Dispatchers.IO)
                scope.launch {
                    val roomInput = mutableMapOf<String, ForecastFactor>()

                    response.body()!!.response.body.items.item.forEach {
                        roomInput[it.fcstDate + it.fcstTime] =
                            roomInput.getOrDefault(
                                key = it.fcstDate + it.fcstTime,
                                defaultValue = ForecastFactor(
                                    baseTime = baseTime,
                                    baseDate = baseDate,
                                    fcstTime = it.fcstTime,
                                    fcstDate = it.fcstDate,
                                    actNx = it.nx,
                                    actNy = it.ny,
                                    rainRatio = 0,
                                    rainType = "",
                                    humidity = 0,
                                    sky = "",
                                    temp = 0,
                                    baseD1 = baseD1,
                                    baseD2 = baseD2
                                )
                            ).apply {
                                when (it.category) {
                                    "POP" -> this.rainRatio = it.fcstValue.toInt()    // 강수 기온
                                    "PTY" -> this.rainType = it.fcstValue     // 강수 형태
                                    "REH" -> this.humidity = it.fcstValue.toInt()     // 습도
                                    "SKY" -> this.sky = it.fcstValue      // 하늘 상태
                                    "TMP" -> this.temp = it.fcstValue.toInt()  // 기온
                                }
                            }
                    }
                    dbWeather.userDao().clearAll()
                    dbWeather.userDao().insertForecastFactors(
                        *roomInput.map { it.value }.toTypedArray()
                    )
                    Log.d("onSuccess", "Success $baseTime")
                    withContext(Dispatchers.Main) {
                    }
                }
            }
            return
        }

        // 응답 실패 시
        override fun onFailure(call: Call<WeatherClass>, t: Throwable) {
            Log.d("api fail", t.message.toString())
        }
    })
}

// 시간 설정하기
// 동네 예보 API는 3시간마다 현재시각+4시간 뒤의 날씨 예보를 보여줌
// 따라서 현재 시간대의 날씨를 알기 위해서는 아래와 같은 과정이 필요함. 자세한 내용은 함께 제공된 파일 확인
fun getTime(time: String): String {
    val result: String
    when (time.toInt()) {
        in 0..2 -> result = "2000"    // 00~02
        in 3..5 -> result = "2300"    // 03~05
        in 6..8 -> result = "0200"    // 06~08
        in 9..11 -> result = "0500"    // 09~11
        in 12..14 -> result = "0800"    // 12~14
        in 15..17 -> result = "1100"    // 15~17
        in 18..20 -> result = "1400"    // 18~20
        else -> result = "1700"             // 21~23
    }
    return result
}

// 기상청 좌표 격자 변환 함수
fun getXY(xInput: Double, yInput: Double): Pair<String, String> {
    val Re = 6371.00877
    val grid = 5.0
    val slat1 = 30.0
    val slat2 = 60.0
    val olon = 126.0
    val olat = 38.0
    val xo = 43
    val yo = 136

    val Pi = Math.PI
    val Degrad = Pi / 180.0

    val re = Re / grid
    val slat1Rad = slat1 * Degrad
    val slat2Rad = slat2 * Degrad
    val olonRad = olon * Degrad
    val olatRad = olat * Degrad

    var sn = tan(Math.PI * 0.25 + slat2Rad * 0.5) / tan(Math.PI * 0.25 + slat1Rad * 0.5)
    sn = Math.log(cos(slat1Rad) / cos(slat2Rad)) / Math.log(sn)
    var sf = tan(Math.PI * 0.25 + slat1Rad * 0.5)
    sf = sf.pow(sn) * cos(slat1Rad) / sn
    var ro = tan(Math.PI * 0.25 + olatRad * 0.5)
    ro = re * sf / ro.pow(sn)
    var ra = tan(Math.PI * 0.25 + xInput * Degrad * 0.5)
    ra = re * sf / ra.pow(sn)
    var theta = yInput * Degrad - olonRad
    if (theta > Math.PI) theta -= 2.0 * Math.PI
    if (theta < -Math.PI) theta += 2.0 * Math.PI
    theta *= sn

    val xOut = (ra * sin(theta) + xo + 0.5).toInt()
    val yOut = (ro - ra * cos(theta) + yo + 0.5).toInt()

    Log.d("grid location", "$xOut, $yOut")

    return Pair(xOut.toString(), yOut.toString())
}