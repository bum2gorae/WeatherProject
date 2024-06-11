package com.example.weatherproject

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tan

// 미세먼지 room input
fun setDust(
    baseDate: String,
    baseD1: String,
    db: AppDatabaseDust
) {
    val baseDateFactor =
        listOf(baseDate.substring(0, 4), baseDate.substring(4, 6), baseDate.substring(6, 8))
    val baseDateDust = baseDateFactor.joinToString("-")
    Log.d("baseDateD", baseDateDust)
    val callDust = ApiobjectD.retrofitServiceDust.getDust(
        baseDateDust,
        "JSON",
        "Z+VGgQxuJdpbHl7FH1zN/Aa3LNv6M4Vyh0VHh6+aY6YN1u7+NQRX/M4A1PuZlx8uVUP4FEd6dODHdZ8Ikg494w==",
        50,
        1
    )

    callDust.enqueue(object : Callback<DustParsingClass> {
        override fun onResponse(
            call: Call<DustParsingClass>,
            response: Response<DustParsingClass>
        ) {
            if (response.isSuccessful) {
                Log.d("dust api Success", baseDateDust)
                val scope = CoroutineScope(Dispatchers.IO)

                scope.launch {
                    val roomInputD = mutableMapOf<String, String>()
                    db.DustDao().clearAll()
                    response.body()!!.Dustresponse.Dustbody.DustItem.forEach { dustItem ->
                        if (dustItem.DustInformCode == "PM25" || dustItem.DustInformCode == "PM10") {
                            val rawInformGrade =
                                dustItem.DustInformGrade.split(",").map { rawInform ->
                                    val parts = rawInform.split(":")
                                    parts[0].trim() to parts[1].trim()
                                }.toMap()
                            roomInputD.putAll(rawInformGrade)
                            roomInputD.forEach { roomInputFactor ->
                                val inputData = DustFactor(
                                    baseDate,
                                    baseD1,
                                    dustItem.DustInformCode,
                                    roomInputFactor.key,
                                    roomInputFactor.value,
                                    dustItem.DustInformData
                                )
                                db.DustDao().insertDustFactors(inputData)
                            }
                        }
                    }
                }
            }
        }

        override fun onFailure(call: Call<DustParsingClass>, t: Throwable) {
            Log.e("dust api fail", t.message.toString())
        }
    })
}

// 기상청 data room input
fun setWeather(
    nx: String,
    ny: String,
    db: AppDatabase,
    cal: Calendar,
    baseDate: String,
    baseD1: String,
    baseD2: String
) {
    val timeFormat = SimpleDateFormat("HH", Locale.KOREA)
    timeFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
    val timeNow = timeFormat.format(cal.time) // 현재 시간
    val baseTime = getTime(timeNow)

    Log.d("data", "$baseTime, $baseDate")

    // 날씨 정보 가져오기
    // (응답 자료 형식-"JSON", 한 페이지 결과 수 = 10, 페이지 번호 = 1, 발표 날싸, 발표 시각, 예보지점 좌표)
    val call = ApiObject.retrofitService.getWeather(
        400,
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
                    db.userDao().clearAll()
                    db.userDao().insertForecastFactors(
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

// GPS data를 기반으로 현재 주소를 구하기 위해 유클리드 거리 계산
fun getEuclidianDistance(
    x1: Double,
    y1: Double,
    x2: Double,
    y2: Double
): Double {
    val result = Math.sqrt(((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2)))
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

    Log.d("fin loc", "$xOut, $yOut")

    return Pair(xOut.toString(), yOut.toString())
}

// 기상청 API와 미세먼지 API의 행정구역 구분이 달라서 변환하기 위함
fun getRegion(region: String): String {
    val regionMap = mapOf(
        "서울" to listOf(
            "종로구",
            "중구",
            "용산구",
            "성동구",
            "광진구",
            "동대문구",
            "중랑구",
            "성북구",
            "강북구",
            "도봉구",
            "노원구",
            "은평구",
            "서대문구",
            "마포구",
            "양천구",
            "강서구",
            "구로구",
            "금천구",
            "영등포구",
            "동작구",
            "관악구",
            "서초구",
            "강남구",
            "송파구",
            "강동구"
        ),
        "제주" to listOf(
            "제주시",
            "서귀포시"
        ),
        "전남" to listOf(
            "목포시",
            "여수시",
            "순천시",
            "나주시",
            "광양시",
            "담양군",
            "곡성군",
            "구례군",
            "고흥군",
            "보성군",
            "화순군",
            "장흥군",
            "강진군",
            "해남군",
            "영암군",
            "무안군",
            "함평군",
            "영광군",
            "장성군",
            "완도군",
            "진도군",
            "신안군"
        ),
        "전북" to listOf(
            "전주시완산구",
            "전주시덕진구",
            "군산시",
            "익산시",
            "정읍시",
            "남원시",
            "김제시",
            "완주군",
            "진안군",
            "무주군",
            "장수군",
            "임실군",
            "순창군",
            "고창군",
            "부안군"
        ),
        "광주" to listOf(
            "동구",
            "서구",
            "남구",
            "북구",
            "광산구"
        ),
        "경남" to listOf(
            "창원시 의창구",
            "창원시 성산구",
            "창원시 마산합포구",
            "창원시 마산회원구",
            "창원시 진해구",
            "진주시",
            "통영시",
            "사천시",
            "김해시",
            "밀양시",
            "거제시",
            "양산시",
            "의령군",
            "함안군",
            "창녕군",
            "남해군",
            "하동군",
            "산청군",
            "함양군",
            "거창군",
            "합천군"
        ),
        "경북" to listOf(
            "포항시남구",
            "포항시북구",
            "경주시",
            "김천시",
            "안동시",
            "구미시",
            "영주시",
            "영천시",
            "상주시",
            "문경시",
            "경산시",
            "군위군",
            "의성군",
            "청송군",
            "영양군",
            "영덕군",
            "청도군",
            "고령군",
            "성주군",
            "칠곡군",
            "예천군",
            "봉화군",
            "울진군",
            "울릉군"
        ),
        "울산" to listOf(
            "중구",
            "남구",
            "동구",
            "북구",
            "울주군"
        ),
        "대구" to listOf(
            "중구",
            "동구",
            "서구",
            "남구",
            "북구",
            "수성구",
            "달서구",
            "달성군"
        ),
        "부산" to listOf(
            "서구",
            "동구",
            "영도구",
            "부산진구",
            "동래구",
            "남구",
            "북구",
            "해운대구",
            "사하구",
            "금정구",
            "강서구",
            "연제구",
            "수영구",
            "사상구",
            "기장군"
        ),
        "충남" to listOf(
            "천안시동남구",
            "천안시서북구",
            "공주시",
            "보령시",
            "아산시",
            "서산시",
            "논산시",
            "계룡시",
            "당진시",
            "금산군",
            "부여군",
            "서천군",
            "청양군",
            "홍성군",
            "예산군",
            "태안군"
        ),
        "충북" to listOf(
            "청주시상당구",
            "청주시서원구",
            "청주시흥덕구",
            "청주시청원구",
            "충주시",
            "제천시",
            "보은군",
            "옥천군",
            "영동군",
            "증평군",
            "진천군",
            "괴산군",
            "음성군",
            "단양군"
        ),
        "세종" to listOf(
            "세종특별자치시"
        ),
        "대전" to listOf(
            "동구",
            "중구",
            "서구",
            "유성구",
            "대덕구"
        ),
        "영동" to listOf(
            "강릉시",
            "동해시",
            "태백시",
            "속초시",
            "삼척시"
        ),
        "영서" to listOf(
            "춘천시",
            "원주시",
            "홍천군",
            "횡성군",
            "영월군",
            "평창군",
            "정선군",
            "철원군",
            "화천군",
            "양구군",
            "인제군",
            "고성군",
            "양양군"
        ),
        "경기남부" to listOf(
            "수원시장안구",
            "수원시권선구",
            "수원시팔달구",
            "수원시영통구",
            "성남시수정구",
            "성남시중원구",
            "성남시분당구",
            "안양시만안구",
            "안양시동안구",
            "부천시",
            "광명시",
            "평택시",
            "안산시상록구",
            "안산시단원구",
            "과천시",
            "시흥시",
            "군포시",
            "의왕시",
            "하남시",
            "용인시처인구",
            "용인시기흥구",
            "용인시수지구",
            "오산시",
            "화성시",
            "안성시"
        ),
        "경기북부" to listOf(
            "의정부시",
            "동두천시",
            "고양시덕양구",
            "고양시일산동구",
            "고양시일산서구",
            "구리시",
            "남양주시",
            "파주시",
            "이천시",
            "김포시",
            "광주시",
            "양주시",
            "포천시",
            "여주시",
            "연천군",
            "가평군",
            "양평군"
        ),
        "인천" to listOf(
            "미추홀구",
            "연수구",
            "남동구",
            "부평구",
            "계양구",
            "서구",
            "강화군",
            "옹진군"
        )
    )
    val targetKey = regionMap.filter { it.value.contains(region) }.keys
    return targetKey.toString()
}

// DustGrade 변환용
fun getDustGrade(input:String?): Int {
    val result =
        when (input) {
        "좋음" -> 1
        "보통" -> 2
        "나쁨" -> 3
        "매우나쁨" -> 4
        else -> 0
    }
    return result
}