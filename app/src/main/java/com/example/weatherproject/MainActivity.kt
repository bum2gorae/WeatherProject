package com.example.weatherproject

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.weatherproject.ui.theme.WeatherProjectTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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


class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContent {
            WeatherProjectTheme {

                val context = LocalContext.current
                var hasPermission by remember { mutableStateOf(checkLocationPermission(context)) }
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
                LaunchedEffect(hasPermission) {
                    when {
                        hasPermission -> fusedLocationClient.lastLocation
                            .addOnSuccessListener { location ->
                                // 위치가 null이 아닌 경우
                                location?.let {
                                    val latitude = it.latitude
                                    val longitude = it.longitude
                                    // 위치를 사용하여 필요한 작업 수행
                                    Log.d("loc", "$latitude, $longitude")
                                }
                            }.addOnFailureListener {
                                Log.d("loc", "${it.toString()}")
                            }
                        else -> locationPermissionRequest.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }
                Main()
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
}

// retrofit을 사용하기 위한 빌더 생성
private val retrofit = Retrofit.Builder()
    .baseUrl("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

object ApiObject {
    val retrofitService: WeatherIF by lazy {
        retrofit.create(WeatherIF::class.java)
    }
}

fun setWeather(
    nx: String,
    ny: String,
    db: AppDatabase
) {

// 준비 단계 : base_date(발표 일자), base_time(발표 시각)
// 현재 날짜, 시간 정보 가져오기
    val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")).apply {
        if (time.hours >= 20) {
//          동네예보  API는 3시간마다 현재시간+4시간 뒤의 날씨 예보를 알려주기 때문에
//          현재 시각이 00시가 넘었다면 어제 예보한 데이터를 가져와야함
            add(Calendar.DATE, -1)
        }
    }
    val baseDateFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREA) // 현재 날짜
    baseDateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
    val timeFormat = SimpleDateFormat("HH", Locale.KOREA)
    timeFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")

    val baseDate = baseDateFormat.format(cal.time)
    val timeNow = timeFormat.format(cal.time) // 현재 시간
    Log.d("time", timeNow)
    val baseTime = getTime(timeNow)
    cal.add(Calendar.DATE, 1)
    val baseD1 = baseDateFormat.format(cal.time)
    cal.add(Calendar.DATE, 1)
    val baseD2 = baseDateFormat.format(cal.time)


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
                    Log.d("timeCheck", "basetime: $baseTime, basedate: $baseDate")

                    response.body()!!.response.body.items.item.forEach {
                        roomInput[it.fcstTime] = roomInput.getOrDefault(
                            key = it.fcstTime,
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
                    roomInput.forEach {
                        db.userDao().insertForecastFactors(
                            it.value
                        )
                    }
                    Log.d("onSuccess", "Success $baseTime")
                    withContext(Dispatchers.Main) {
                    }
                }
            }
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
    var result = ""
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

//좌표변환 함수
fun getXY(x: Double, y: Double): Pair<String, String> {
    val Re = 6371.00877
    val grid = 5.0; // 격자간격 (km)
    val slat1 = 30.0; // 표준위도 1
    val slat2 = 60.0; // 표준위도 2
    val olon = 126.0; // 기준점 경도
    val olat = 38.0; // 기준점 위도
    val xo = 210 / grid; // 기준점 X좌표
    val yo = 675 / grid; // 기준점 Y좌표
    val Degrad = Math.PI / 180.0
    val re = Re / grid
    val slat1d = slat1 * Degrad
    val slat2d = slat2 * Degrad
    val olond = olon * Degrad
    val olatd = olat * Degrad

    var sn = Math.tan(Math.PI * 0.25 + slat2d * 0.5) / Math.tan(Math.PI * 0.25 + slat1d * 0.5)
    sn = Math.log(Math.cos(slat1d) / Math.cos(slat2d)) / Math.log(sn)
    var sf = Math.tan(Math.PI * 0.25 + slat1d * 0.5)
    sf = Math.pow(sf, sn) * Math.cos(slat1d) / sn
    var ro = Math.tan(Math.PI * 0.25 + olatd * 0.5)
    ro = re * sf / Math.pow(ro, sn)

    var ra = Math.tan(Math.PI * 0.25 + (x) * Degrad * 0.5)
    ra = re * sf / Math.pow(ra, sn)
    var theta = y * Degrad - olond
    if (theta > Math.PI) theta -= 2.0 * Math.PI
    if (theta < -Math.PI) theta += 2.0 * Math.PI
    theta *= sn

    val xReturn = (ra * Math.sin(theta) + xo + 0.5).toInt().toString()
    val yReturn = (ro - ra * Math.cos(theta) + yo + 0.5).toInt().toString()

    return Pair(xReturn, yReturn)
}

@Composable
fun Main() {
    MainScreen()
}

@Composable
fun MainScreen() {
    val context = LocalContext.current as? Activity
    val contextDB = LocalContext.current
    val db = remember {
        AppDatabase.getDatabase(contextDB)
    }

    var todaySwitchCheck by remember {
        mutableStateOf(false)
    }


    var nx = "55"
    var ny = "127"
//    var viewTemp by remember {
//        mutableStateOf(0)`
//    }

    setWeather(nx, ny, db)
    val viewTemp = db.userDao().getTempAvg().collectAsState(initial = -1)
    val viewRain = db.userDao().getRainMax().collectAsState(initial = -1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(text = "date")
            Text(text = "location")
        }
        Spacer(modifier = Modifier.size(50.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "오늘",
                fontSize = 20.sp,
                modifier = Modifier.padding(8.dp)
            )
            Spacer(modifier = Modifier.size(20.dp))
            Switch(
                checked = todaySwitchCheck,
                onCheckedChange = { todaySwitchCheck = it },
//                colors = SwitchDefaults.colors()
            )
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
                    imageID = R.drawable.cloth_penguin,
                    describeText = viewRain.value.toString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(10.dp)
                )
                IconBox(
                    imageID = R.drawable.cloth_penguin,
                    describeText = viewTemp.value.toString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.size(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconBox(
                    imageID = R.drawable.cloth_penguin,
                    describeText = "cloth",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(10.dp)
                )
                IconBox(
                    imageID = R.drawable.cloth_penguin,
                    describeText = "상세보기",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.size(50.dp))
            Button(onClick = {
                setWeather(nx, ny, db)
            }) {
                Text(text = "새로고침")
            }

        }
    }

}

@Composable
fun IconBox(imageID: Int, describeText: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = imageID),
            contentDescription = "test",
//            contentScale =  ContentScale.FillWidth
        )
        Spacer(modifier = Modifier.size(20.dp))
        Text(text = describeText)
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    WeatherProjectTheme {
        MainScreen()
    }
}