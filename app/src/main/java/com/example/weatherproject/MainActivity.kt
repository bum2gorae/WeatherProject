package com.example.weatherproject

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherproject.ui.theme.WeatherProjectTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherProjectTheme {
                Main()
            }
        }
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

data class Weather(
    val rainRatio: String,     // 강수 확률
    val rainType: String,      // 강수 형태
    val humidity: String,      // 습도
    val sky: String,           // 하능 상태
    val temp: Int,          // 기온
    val test: Any
)

fun setWeather(nx: String,
               ny: String,
               Context: Context?,
               db : AppDatabase,
               onSuccess: (Weather) -> Unit
               ) {
//    val cal = Calendar.getInstance().apply {
//        // 동네예보  API는 3시간마다 현재시간+4시간 뒤의 날씨 예보를 알려주기 때문에
//        // 현재 시각이 00시가 넘었다면 어제 예보한 데이터를 가져와야함
//        if (time.hours >= 20) {
//            add(Calendar.DATE, -1)
//        }
//    }
//    val baseTime = when (cal.time.hours) {
//        in 0..2 -> "2000"    // 00~02
//        in 3..5 -> "2300"    // 03~05
//        in 6..8 -> "0200"    // 06~08
//        in 9..11 -> "0500"    // 09~11
//        in 12..14 -> "0800"    // 12~14
//        in 15..17 -> "1100"    // 15~17
//        in 18..20 -> "1400"    // 18~20
//        else -> "1700"             // 21~23
//    }
//
//    val baseDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time) // 현재 날짜




    // 준비 단계 : base_date(발표 일자), base_time(발표 시각)
    // 현재 날짜, 시간 정보 가져오기

    val cal = Calendar.getInstance()
    var baseDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time) // 현재 날짜
    val time = SimpleDateFormat("HH", Locale.getDefault()).format(cal.time) // 현재 시간
    // API 가져오기 적당하게 변환
    val baseTime = getTime(time)
    // 동네예보  API는 3시간마다 현재시간+4시간 뒤의 날씨 예보를 알려주기 때문에
    // 현재 시각이 00시가 넘었다면 어제 예보한 데이터를 가져와야함

    if (baseTime >= "2000") {
        cal.add(Calendar.DATE, -1).toString()
        baseDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
    }
    // 날씨 정보 가져오기
    // (응답 자료 형식-"JSON", 한 페이지 결과 수 = 10, 페이지 번호 = 1, 발표 날싸, 발표 시각, 예보지점 좌표)
    val call = ApiObject.retrofitService.getWeather(
        50,
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
                var rainRatio = ""      // 강수 확률
                var rainType = ""       // 강수 형태
                var humidity = ""       // 습도
                var sky = ""            // 하능 상태
                var temp = 0            // 기온
                var test: Any = "fail"
//                 날씨 정보 가져오기
                response.body()!!.response.body.items.item.forEach {
                    when (it.category) {
                        "POP" -> rainRatio = it.fcstValue    // 강수 기온
                        "PTY" -> rainType = it.fcstValue     // 강수 형태
                        "REH" -> humidity = it.fcstValue     // 습도
                        "SKY" -> sky = it.fcstValue          // 하늘 상태
                        "TMP" -> {temp = it.fcstValue.toInt()
                            test=it}       // 기온
                    }
                }
                db.userDao().insertAll(
                    WeatherRoomColumns(
                        basetime = baseTime,
                        basedate = baseDate,
                        humidity = humidity,
                        temp = temp,
                        rainratio = rainRatio,
                        raintype = rainType,
                    )
                )
//                var it: List<Item> = response.body()!!.response.body.items.item
//                Log.d("WeatherProoject", "$it")
//                for (i in 0..49) {
//                    when (it[i].category) {
//                        "POP" -> rainRatio = it[i].fcstValue    // 강수 기온
//                        "PTY" -> rainType = it[i].fcstValue     // 강수 형태
//                        "REH" -> humidity = it[i].fcstValue     // 습도
//                        "SKY" -> sky = it[i].fcstValue          // 하늘 상태
//                        "TMP" -> {temp = it[i].fcstValue.toInt()
//                            test=it[i]}       // 기온
//                        else -> continue
//                    }
//                }
//                Toast.makeText(
//                    applicationContext,
//                    it[0].fcstDate + ", " + it[0].fcstTime + "의 날씨 정보입니다.",
//                    Toast.LENGTH_SHORT
//                ).show()
                onSuccess(
                    Weather(rainRatio, rainType, humidity, sky, temp, test)
                )
                Log.d("WeatherProjecct", "onSuccess ${baseDate}, ${baseTime}")
                Log.d("WeatherProjecct", "onSuccess ${humidity}, ${sky}")
            }
        }

        // 응답 실패 시
        override fun onFailure(call: Call<WeatherClass>, t: Throwable) {
            Log.d("api fail", t.message.toString())
            Log.d("WeatherProjecct", "onFailure ${baseDate}, ${baseTime}")
        }
    })
}

// 시간 설정하기
// 동네 예보 API는 3시간마다 현재시각+4시간 뒤의 날씨 예보를 보여줌
// 따라서 현재 시간대의 날씨를 알기 위해서는 아래와 같은 과정이 필요함. 자세한 내용은 함께 제공된 파일 확인
fun getTime(time: String): String {
    var result = ""
    when (time) {
        in "00".."02" -> result = "2000"    // 00~02
        in "03".."05" -> result = "2300"    // 03~05
        in "06".."08" -> result = "0200"    // 06~08
        in "09".."11" -> result = "0500"    // 09~11
        in "12".."14" -> result = "0800"    // 12~14
        in "15".."17" -> result = "1100"    // 15~17
        in "18".."20" -> result = "1400"    // 18~20
        else -> result = "1700"             // 21~23
    }
    return result
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
    var weather by remember { mutableStateOf(Weather("", "", "", "", 0, "")) }
    var viewTemp by remember {
        mutableStateOf(0)
    }

    setWeather(nx, ny, context, db) {
        weather = it
        Log.d("WeatherProject", weather.test.toString())
        CoroutineScope(Dispatchers.IO).launch {
            viewTemp = db.userDao().getTemp()
        }
    }

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
                    imageID = R.drawable.ic_launcher_background,
                    describeText = weather.rainRatio
                )
                IconBox(
                    imageID = R.drawable.ic_launcher_background,
                    describeText = viewTemp.toString()
                )
            }
            Spacer(modifier = Modifier.size(40.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconBox(
                    imageID = R.drawable.ic_launcher_background,
                    describeText = "cloth")
                IconBox(
                    imageID = R.drawable.ic_launcher_background,
                    describeText = "상세보기"
                )
            }
            Spacer(modifier = Modifier.size(50.dp))
            Button(onClick = {
                setWeather(nx, ny, context, db) {
                    weather = it
                }
            }) {
                Text(text = "새로고침")
            }

        }
    }

}

@Composable
fun IconBox(imageID: Int, describeText: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = imageID),
            contentDescription = "test",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(150.dp)
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