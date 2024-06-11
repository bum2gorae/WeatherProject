package com.example.weatherproject

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.weatherproject.ui.theme.WeatherProjectTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream
import java.lang.Math.log
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tan


class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContent {
            WeatherProjectTheme {
                Main(fusedLocationClient)
            }
        }
    }
}


private fun checkLocationPermission(context: Context): Boolean {
    return !(ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED)
}

// retrofit을 사용하기 위한 빌더 생성
private val retrofit = Retrofit.Builder()
    .baseUrl("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

// retrofit을 사용하기 위한 빌더 생성
private val retrofitD = Retrofit.Builder()
    .baseUrl("http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/")
    .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
    .client(
        OkHttpClient().newBuilder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    )
    .build()

object ApiObject {
    val retrofitService: WeatherIF by lazy {
        retrofit.create(WeatherIF::class.java)
    }
}

object ApiobjectD {
    val retrofitServiceDust: DustIF by lazy {
        retrofitD.create(DustIF::class.java)
    }
}

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
    cal.add(Calendar.DATE, 1)

    Log.d("data", "$baseTime, $baseDate")

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
                    db.userDao().clearAll()
                    db.userDao().insertForecastFactors(
                        *roomInput.map { it.value }.toTypedArray()
                    )
//                    roomInput.forEach {
//                        db.userDao().insertForecastFactors(
//                            it.value
//                        )
//                    }
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

fun getEuclidianDistance(
    x1: Double,
    y1: Double,
    x2: Double,
    y2: Double
): Double {
    val result = Math.sqrt(((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2)))
    return result
}

//좌표변환 함수
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
    sn = log(cos(slat1Rad) / cos(slat2Rad)) / log(sn)
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

interface LottieAnimationState : State<Float>

@Composable
fun Loader(onSwitch: Boolean) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lotte_json1))
    val progress by animateLottieCompositionAsState(
        composition,
        clipSpec = LottieClipSpec.Progress(0f, 1f)
    )
    val lottieAnimatable = rememberLottieAnimatable()
    LaunchedEffect(key1 = composition, key2 = onSwitch) {
        lottieAnimatable.animate(
            composition = composition,
            clipSpec = LottieClipSpec.Progress(0f, 1f),
            initialProgress = 0f
        )
    }
    LottieAnimation(composition = composition,
        progress = { lottieAnimatable.progress })
}

@Composable
fun Main(fusedLocationClient: FusedLocationProviderClient) {
    val context = LocalContext.current as Activity?
    val contextDB = LocalContext.current
    val dbWeather = remember {
        AppDatabase.getDatabase(contextDB)
    }
    val dbRegion = remember {
        AppDatabaseReg.getDatabase(contextDB)
    }
    val dbDust = remember {
        AppDatabaseDust.getDatabase(contextDB)
    }
    // 준비 단계 : base_date(발표 일자), base_time(발표 시각)
// 현재 날짜, 시간 정보 가져오기
    val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")).apply {
        if (time.hours <= 2) {
//          동네예보  API는 3시간마다 현재시간+4시간 뒤의 날씨 예보를 알려주기 때문에
//          현재 시각이 00시가 넘었다면 어제 예보한 데이터를 가져와야함
            add(Calendar.DATE, -1)
        }
    }
    val baseDateFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREA) // 현재 날짜
    val baseDate = baseDateFormat.format(cal.time)
    cal.add(Calendar.DATE, 1)
    val baseD1 = baseDateFormat.format(cal.time)
    cal.add(Calendar.DATE, 1)
    val baseD2 = baseDateFormat.format(cal.time)

    val viewDateFormat = DateTimeFormatterBuilder()
        .appendPattern("yyyy년 ")
        .appendValue(ChronoField.MONTH_OF_YEAR)
        .appendLiteral("월 ")
        .appendValue(ChronoField.DAY_OF_MONTH)
        .appendLiteral("일")
        .toFormatter(Locale.KOREA)
    val date = LocalDate.now()
    val nextDay = date.plusDays(1)
    val viewDate = date.format(viewDateFormat)
    baseDateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")

    val viewDateD1 = nextDay.format(viewDateFormat)
    cal.add(Calendar.DATE, 1)

    var hasPermission by remember { mutableStateOf(checkLocationPermission(contextDB)) }
    val locationPermissionRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(
                Manifest.permission.ACCESS_FINE_LOCATION,
                false
            ) -> hasPermission = true

            permissions.getOrDefault(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                false
            ) -> hasPermission = true

        }
    }

    val assetManager: AssetManager = contextDB.assets
    val inputStream: InputStream = assetManager.open("KoreaRegion.txt")

    val nowRegionData by remember {
        mutableStateOf(RegionData(0.0, 0.0, "", ""))
    }
    LaunchedEffect(hasPermission) {
        when {
            hasPermission -> fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    // 위치가 null이 아닌 경우
                    location?.let {
                        nowRegionData.nx = it.latitude
                        nowRegionData.ny = it.longitude
                        // 위치를 사용하여 필요한 작업 수행
                        Log.d("loc", "${nowRegionData.nx}, ${nowRegionData.ny}")
//                        val (nx, ny) = getXY(it.latitude, it.longitude)
//                        Log.d("nx,ny", "$nx, $ny")
                        val nx = "55"
                        val ny = "127"

                        setWeather(nx, ny, dbWeather, cal, baseDate, baseD1, baseD2)
                        setDust(
                            baseDate,
                            baseD1,
                            dbDust
                        )

                        CoroutineScope(Dispatchers.IO).launch {
                            dbRegion.RegDao().clearAll()
                            inputStream.bufferedReader().readLines().forEach {
                                val token = it.split("\t")
                                val input = RegionRoomClass(
                                    token[0].toLong(),
                                    token[1],
                                    token[2],
                                    token[3],
                                    token[4].toInt(),
                                    token[5].toInt(),
                                    if (token[12].isEmpty()) null else token[12].toDouble(),
                                    if (token[13].isEmpty()) null else token[13].toDouble(),
                                    if (token[12].isEmpty()) null else {
                                        getEuclidianDistance(
                                            nowRegionData.nx,
                                            nowRegionData.ny,
                                            token[13].toDouble(),
                                            token[12].toDouble()
                                        )
                                    }
                                )
                                dbRegion.RegDao().insertAll(input)
                            }
                        }
                    }
                }.addOnFailureListener {
                    Log.d("loc fail", it.toString())
                }

            else -> locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    val reg2 = dbRegion.RegDao().getRegion2().collectAsState(initial = "서울").value ?: "서울"
    val dustReg = getRegion(reg2)

    val mainScreenData by remember {
        mutableStateOf(
            MainScreenData()
        )
    }
    var todaySwitchCheck by remember {
        mutableStateOf(false)
    }


    if (todaySwitchCheck) {
        mainScreenData.viewRain =
            dbWeather.userDao().getRainMaxD1().collectAsState(initial = -999).value.toString()
        mainScreenData.viewTemp =
            dbWeather.userDao().getTempAvgD1().collectAsState(initial = -999).value.toString()
        val dust1 = getDustGrade(
            dbDust.DustDao().getDust10D1(baseD1, dustReg).collectAsState(initial = "").value
        )
        val dust2 = getDustGrade(
            dbDust.DustDao().getDust25D1(baseD1, dustReg).collectAsState(initial = "").value
        )
        mainScreenData.viewDust = when {
            dust1 <= 2 && dust2 <= 2 -> "좋음"
            dust1 >= 3 && dust2 >= 3 -> "나쁨"
            else -> mainScreenData.viewDust
        }
        nowRegionData.baseDate = viewDateD1
        mainScreenData.dayCheck = "내일"
        mainScreenData.imageDetail = R.drawable.tomorrow_detail_icon
    } else {
        mainScreenData.viewRain =
            dbWeather.userDao().getRainMax().collectAsState(initial = -999).value.toString()
        mainScreenData.viewTemp =
            dbWeather.userDao().getTempAvg().collectAsState(initial = -999).value.toString()
        val dust1 = getDustGrade(
            dbDust.DustDao().getDust10(baseD1, dustReg).collectAsState(initial = "서울").value
        ) ?: 0
        val dust2 = getDustGrade(
            dbDust.DustDao().getDust25(baseD1, dustReg).collectAsState(initial = "서울").value
        ) ?: 0
        mainScreenData.viewDust = when {
            dust1 == 1 && dust2 == 1 -> "좋음"
            dust1 <= 2 && dust2 <= 2 -> "보통"
            dust1 >= 3 && dust2 >= 3 -> "나쁨"
            else -> mainScreenData.viewDust
        }
        nowRegionData.baseDate = viewDate
        mainScreenData.dayCheck = "오늘"
        mainScreenData.imageDetail = R.drawable.today_details_icon
    }

    var isFlipped by remember { mutableStateOf(false) }
    val scaleX by animateFloatAsState(
        targetValue = if (isFlipped) -1f else 1f,
        animationSpec = tween(durationMillis = 1000), label = "flip"
    )

    when {
        mainScreenData.viewTemp.toInt() == -999 -> {
            mainScreenData.imageTemp = mainScreenData.imageTemp
            mainScreenData.textTemp = mainScreenData.textTemp
        }

        mainScreenData.viewTemp.toInt() >= 25 -> {
            mainScreenData.imageTemp = R.drawable.hot_penguin
            mainScreenData.textTemp = "얇은 옷을 입어요!"
        }

        mainScreenData.viewTemp.toInt() in 15 until 25 -> {
            mainScreenData.imageTemp = R.drawable.normal_penguin
            mainScreenData.textTemp = "거의 봄날씨에요!"
        }

        mainScreenData.viewTemp.toInt() < 15 -> {
            mainScreenData.imageTemp = R.drawable.cold_penguin
            mainScreenData.textTemp = "옷을 따뜻하게 입어요!"
        }
    }

    when {
        mainScreenData.viewRain.toInt() == -999 -> {
            mainScreenData.imageRain = mainScreenData.imageRain
            mainScreenData.textRain = mainScreenData.textRain
        }

        mainScreenData.viewRain.toInt() >= 70 -> {
            mainScreenData.imageRain = R.drawable.rainy_cat
            mainScreenData.textRain = "우산이 필요할 수 있어요!"
        }

        mainScreenData.viewRain.toInt() < 70 -> {
            mainScreenData.imageRain = R.drawable.sunny_cat
            mainScreenData.textRain = "우산이 필요 없겠어요!"
        }
    }

    when {
        mainScreenData.viewDust == "" -> {
            mainScreenData.imageRain = mainScreenData.imageRain
            mainScreenData.textRain = mainScreenData.textRain
        }

        mainScreenData.viewDust == "좋음" -> {
            mainScreenData.imageDust = R.drawable.gooddust_bear
            mainScreenData.textDust = "피크닉해도 되겠어요!"
        }

        mainScreenData.viewDust == "보통" -> {
            mainScreenData.imageDust = R.drawable.normaldust_bear
            mainScreenData.textDust = "나쁘지 않지만 조심해요!"
        }

        mainScreenData.viewDust == "나쁨" -> {
            mainScreenData.imageDust = R.drawable.baddust_bear
            mainScreenData.textDust = "마스크가 필요해요!"
        }
    }

    nowRegionData.regionNameNow = dbRegion.RegDao().getRegionWithMinEuclidian()
        .collectAsState(
            initial = "위치를 불러올 수 없습니다."
        ).value ?: "Loading.."

    MainScreen(
        nowRegionData.baseDate,
        nowRegionData.regionNameNow,
        mainScreenData.dayCheck,
        mainScreenData.imageRain,
        mainScreenData.textRain,
        mainScreenData.imageTemp,
        mainScreenData.textTemp,
        todaySwitchCheck,
        onCheckedChange = {
            todaySwitchCheck = it
            isFlipped = it
        },
        onReloadClickSuccess = {
            isFlipped = !isFlipped
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    // 위치가 null이 아닌 경우
                    location?.let {
                        val latitude = it.latitude
                        val longitude = it.longitude
                        // 위치를 사용하여 필요한 작업 수행
                        Log.d("init loc", "$latitude, $longitude")
                        val (nx, ny) = getXY(latitude, longitude)
//                        nx = "55"
//                        ny = "127"
                        Log.d("fin loc", "$nx, $ny")
                        setWeather(nx, ny, dbWeather, cal, baseDate, baseD1, baseD2)
                    }
                }.addOnFailureListener {
                    Log.d("loc", it.toString())
                }
        },
        mainScreenData.imageDust,
        mainScreenData.textDust,
        context,
        onDetailClickSuccess = {
            val intent = Intent(it, DetailActivity::class.java)
            intent.putExtra("region", nowRegionData.regionNameNow)
            it?.startActivity(intent)
        },
        scaleX,
        lottieInsert = { Loader(isFlipped) }
    )
}

@Composable
fun MainScreen(
    baseDate: String,
    regionNameNow: String,
    dayCheck: String,
    imageRain: Int,
    textRain: String,
    imageTemp: Int,
    textTemp: String,
    todaySwitchCheck: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onReloadClickSuccess: () -> Unit,
    imageDust: Int,
    textDust: String,
    context: Activity?,
    onDetailClickSuccess: (Activity?) -> Unit,
    scaleX: Float,
    lottieInsert: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.size(30.dp))
        Box(
            modifier = Modifier
                .size(width = 200.dp, height = 50.dp)
                .background(
                    color = Color(0xff19abe0),
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = baseDate,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.size(15.dp))
        Box(
            modifier = Modifier
                .size(width = 300.dp, height = 50.dp)
                .background(
                    color = Color(0xff61ccf2),
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = regionNameNow,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.size(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Row(
                modifier = Modifier
                    .background(
                        color = if (dayCheck == "오늘") {
                            Color(0xffcaf5fc)
                        } else {
                            Color(0xffa7badb)
                        },
                        shape = RoundedCornerShape(20.dp)
                    )
                    .size(width = 120.dp, height = 45.dp)
                    .border(
                        BorderStroke(
                            2.dp, color = if (dayCheck == "오늘") {
                                Color(0xff99d8e0)
                            } else {
                                Color(0xff5f6e87)
                            }
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(start = 8.dp, end = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    text = dayCheck,
                    fontSize = 20.sp
                )
                Switch(
                    checked = todaySwitchCheck,
                    onCheckedChange = { onCheckedChange(it) },
                    enabled = true
//                colors = SwitchDefaults.colors()
                )
            }
        }
        //Grid
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconBox(
                    imageID = imageRain,
                    describeText = textRain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(10.dp),
                    scaleX
                )
                IconBox(
                    imageID = imageDust,
                    describeText = textDust,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(10.dp),
                    scaleX
                )
            }
            Spacer(modifier = Modifier.size(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconBox(
                    imageID = imageTemp,
                    describeText = textTemp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(10.dp),
                    scaleX
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(10.dp)
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        val boxSize = maxWidth
                        Box(modifier = Modifier
                            .size(boxSize)
                            .clickable {
                                onDetailClickSuccess(context)
                            },
                            contentAlignment = Alignment.Center) {
                            lottieInsert()
                        }
                    }
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = "상세보기",
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(modifier = Modifier.size(20.dp))
            Button(onClick = {
                onReloadClickSuccess()
            }) {
                Text(text = "새로고침")
            }

        }
    }

}

@Composable
fun IconBox(
    imageID: Int,
    describeText: String,
    modifier: Modifier = Modifier,
    scaleX: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = imageID),
            contentDescription = "test",
            modifier = Modifier.scale(scaleX = scaleX, scaleY = 1F)
//            contentScale =  ContentScale.FillWidth
        )
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = describeText,
            fontSize = 13.sp
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun MainPreview() {
//    WeatherProjectTheme {
//        MainScreen(
//            "2024년 6월 9일",
//            "서울특별시 도봉구 도봉제1동",
//            "오늘",
//            R.drawable.sunny_cat,
//            "",
//            R.drawable.hot_penguin,
//            "",
//            true,
//            {},
//            {},
//            R.drawable.today_details_icon,
//            R.drawable.gooddust_bear,
//            "",
//            Activity(),
//            {},
//            0F,
//            {}
//        )
//    }
//}