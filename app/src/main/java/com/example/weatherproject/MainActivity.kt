package com.example.weatherproject

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
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
import com.example.weatherproject.ui.theme.WeatherProjectTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.weatherproject.WEATHER

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

data class WEATHER(val response: RESPONSE)
data class RESPONSE(val header: HEADER, val body: BODY)
data class HEADER(val resultCode: Int, val resultMsg: String)
data class BODY(val dataType: String, val items: ITEMS)
data class ITEMS(val item: List<ITEM>)

// category : 자료 구분 코드, fcstDate : 예측 날짜, fcstTime : 예측 시간, fcstValue : 예보 값
data class ITEM(
    val category: String,
    val fcstDate: String,
    val fcstTime: String,
    val fcstValue: String
)

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
    val temp: String,          // 기온
)

fun setWeather(nx: String, ny: String, applicationContext: Context?, onSuccess: (Weather) -> Unit) {
    // 준비 단계 : base_date(발표 일자), base_time(발표 시각)
    // 현재 날짜, 시간 정보 가져오기

    val cal = Calendar.getInstance()
    var base_date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time) // 현재 날짜
    val time = SimpleDateFormat("HH", Locale.getDefault()).format(cal.time) // 현재 시간
    // API 가져오기 적당하게 변환
    val base_time = getTime(time)
    // 동네예보  API는 3시간마다 현재시간+4시간 뒤의 날씨 예보를 알려주기 때문에
    // 현재 시각이 00시가 넘었다면 어제 예보한 데이터를 가져와야함

    if (base_time >= "2000") {
        cal.add(Calendar.DATE, -1).toString()
        base_date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
    }
    // 날씨 정보 가져오기
    // (응답 자료 형식-"JSON", 한 페이지 결과 수 = 10, 페이지 번호 = 1, 발표 날싸, 발표 시각, 예보지점 좌표)
    val call = ApiObject.retrofitService.getWeather(
        1,
        10,
        "JSON",
        base_date,
        base_time,
        nx,
        ny
    )
    call.enqueue(object : Callback<String> {
        override fun onResponse(call: Call<String>, response: Response<String>) {
            Log.d("WeatherProjecct", "${response.isSuccessful}")
//            TODO("Not yet implemented")
        }

        override fun onFailure(call: Call<String>, t: Throwable) {
            Log.d("WeatherProjecct", "onFailure ${base_date}, ${base_time}")
//            TODO("Not yet implemented")
        }

    })
}
//    call.enqueue(object : retrofit2.Callback<WEATHER> {
//        // 응답 성공 시
//        override fun onResponse(call: Call<WEATHER>, response: Response<WEATHER>) {
//            if (response.isSuccessful) {
//                var rainRatio = ""      // 강수 확률
//                var rainType = ""       // 강수 형태
//                var humidity = ""       // 습도
//                var sky = ""            // 하능 상태
//                var temp = ""           // 기온
//                // 날씨 정보 가져오기
//                var it: List<ITEM> = response.body()!!.response.body.items.item
//                for (i in 0..9) {
//                    when (it[i].category) {
//                        "POP" -> rainRatio = it[i].fcstValue    // 강수 기온
//                        "PTY" -> rainType = it[i].fcstValue     // 강수 형태
//                        "REH" -> humidity = it[i].fcstValue     // 습도
//                        "SKY" -> sky = it[i].fcstValue          // 하늘 상태
//                        "T3H" -> temp = it[i].fcstValue         // 기온
//                        else -> continue
//                    }
//                }
//
//                Toast.makeText(
//                    applicationContext,
//                    it[0].fcstDate + ", " + it[0].fcstTime + "의 날씨 정보입니다.",
//                    Toast.LENGTH_SHORT
//                ).show()
//
//                onSuccess(
//                    Weather(rainRatio, rainType, humidity, sky, temp)
//                )
//            }
//        }
//
//        // 응답 실패 시
//        override fun onFailure(call: Call<WEATHER>, t: Throwable) {
//            Log.d("api fail", t.message.toString())
//            Log.d("WeatherProjecct", "onFailure ${base_date}, ${base_time}")
//        }
//    })
//}

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
    Main_Layout()
}

@Composable
fun Main_Layout() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var todaySwitchCheck by remember {
            mutableStateOf(false)
        }
        val context = LocalContext.current as? Activity
        var nx = "55"
        var ny = "127"
        var weather by remember { mutableStateOf(Weather("", "", "", "", "")) }

        setWeather(nx, ny, context) {
            weather = it
        }
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
                    describeText = "${weather.rainRatio}"
                )
                IconBox(imageID = R.drawable.ic_launcher_background, describeText = "mask")
            }
            Spacer(modifier = Modifier.size(40.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconBox(imageID = R.drawable.ic_launcher_background, describeText = "cloth")
                IconBox(
                    imageID = R.drawable.ic_launcher_background,
                    describeText = "detail"
                )
            }
            Spacer(modifier = Modifier.size(50.dp))
            Button(onClick = { /*TODO*/ }) {
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
        Main_Layout()
    }
}