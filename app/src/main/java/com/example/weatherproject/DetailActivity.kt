package com.example.weatherproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherproject.ui.theme.WeatherProjectTheme
import kotlinx.coroutines.flow.Flow

class DetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val contextDB = LocalContext.current
            val dbWeather = AppDatabase.getDatabase(contextDB)
            val userDao = dbWeather.userDao()

            val viewModelFactory = WeatherViewModelFactory(userDao)
            val viewModel: WeatherViewModel = ViewModelProvider(this, viewModelFactory)
                .get(WeatherViewModel::class.java)
            WeatherProjectTheme {
                DetailMain(intent, viewModel)
            }
        }
    }
}

class WeatherViewModelFactory(private val userDao: UserDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(userDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class WeatherViewModel(private val userDao: UserDao) : ViewModel() {
    // Method to get weather summary data by IDs
    fun getWeatherDetails(): Flow<List<WeatherDetails>> {
        return userDao.getAllDetails()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeatherTable(viewModel: WeatherViewModel) {
    val weatherFlow: Flow<List<WeatherDetails>> = viewModel.getWeatherDetails()
    val weatherList by weatherFlow.collectAsState(initial = emptyList())

    LazyVerticalGrid(
        columns = GridCells.Fixed(1), // Adjust the number of columns as needed
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(weatherList) { weather ->
            WeatherRow(weather)
        }
    }
}

@Composable
fun WeatherGridContainer(
    text: String,
    modifier: Modifier,
    size: Int
) {
    Text(
        text = text,
        color = Color.Black,
        modifier = modifier,
        textAlign = TextAlign.Center,
        fontSize = size.sp
    )
}

@Composable
fun WeatherRow(weather: WeatherDetails) {
    Row(
        modifier = Modifier
            .padding(4.dp)
            .background(
                color = Color(0xffebeded),
                shape = RoundedCornerShape(15.dp)
            )
            .padding(8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val weatherIcon: Int
        val tintColor: Color
        when {
            weather.rainRatio >= 60 -> {
                weatherIcon = R.drawable.snow_icon
                tintColor = Color(0xffb8b8b8)
            }

            weather.rainRatio in 30 until 60 -> {
                weatherIcon = R.drawable.cloud_icon
                tintColor = Color(0xffb8b8b8)
            }

            else -> {
                weatherIcon = R.drawable.sunny_icon
                tintColor = Color(0xffFFE05E)
            }
        }
        WeatherGridContainer(
            text = weather.fcstDate,
            modifier = Modifier.weight(2f), 14
        )
        WeatherGridContainer(
            text = weather.fcstTime,
            modifier = Modifier.weight(1f), 14
        )
        WeatherGridContainer(
            text = "${weather.temp}°C",
            modifier = Modifier.weight(1f), 14
        )
        WeatherGridContainer(
            text = "${weather.rainRatio}%",
            modifier = Modifier.weight(1f), 14
        )
        Icon(
            painter = painterResource(id = weatherIcon), // 아이콘 추가
            contentDescription = null,
            tint = tintColor,
            modifier = Modifier
                .size(24.dp)
                .weight(0.5f)
        )
    }
}

@Composable
fun DetailMain(intent: Intent, viewModel: WeatherViewModel) {
    val context = LocalContext.current as Activity?
    val regionName = intent.getStringExtra(("region"))
    DetailScreen(
        regionName.toString(),
        viewModel,
        onClickSuccess = {
            context?.finish()
        }
    )
}


@Composable
fun DetailScreen(
    regionName: String,
    viewModel: WeatherViewModel,
    onClickSuccess: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.size(40.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(
                    color = Color(0xff6cb7f0),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(15.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = regionName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.size(25.dp))
        Row(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp)
                .background(
                    color = Color(0xffadd2ff),
                    shape = RoundedCornerShape(15.dp)
                )
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            WeatherGridContainer(
                text = "예보날짜",
                modifier = Modifier.weight(2f), 12
            )
            WeatherGridContainer(
                text = "예보시각",
                modifier = Modifier.weight(1f), 12
            )
            WeatherGridContainer(
                text = "기온",
                modifier = Modifier.weight(1f), 12
            )
            WeatherGridContainer(
                text = "강수확률",
                modifier = Modifier.weight(1f), 12
            )
            Icon(
                painter = painterResource(id = R.drawable.transparent), // 아이콘 추가
                contentDescription = null,
                tint = Color(0xFFFFE05E),
                modifier = Modifier
                    .size(24.dp)
                    .weight(0.5f)
            )
        }
        WeatherTable(viewModel)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 40.dp)
                .size(40.dp)
                .background(
                    Color(0xff6cb7f0),
                    shape = CircleShape
                )
                .align(Alignment.BottomEnd)
                .padding(end = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painterResource(id = R.drawable.return_icon),
                contentDescription = "",
                tint = Color.White,
                modifier = Modifier.clickable { onClickSuccess() })
        }
    }
}


//@Preview(showBackground = true)
//@Composable
//fun DetailPreview() {
//    WeatherProjectTheme {
//        DetailScreen("서울시", )
//    }
//}