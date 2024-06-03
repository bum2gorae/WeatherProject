package com.example.weatherproject

import android.os.Bundle
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherproject.ui.theme.WeatherProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherProjectTheme {
                Main(
                )
            }
        }
    }
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
            Text(text = "오늘",
                fontSize = 20.sp,
                modifier = Modifier.padding(8.dp))
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
                IconBox(imageID = R.drawable.ic_launcher_background, describeText = "umbrella")
                IconBox(imageID = R.drawable.ic_launcher_background, describeText = "mask")
            }
            Spacer(modifier = Modifier.size(40.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconBox(imageID = R.drawable.ic_launcher_background, describeText = "cloth")
                IconBox(imageID = R.drawable.ic_launcher_background, describeText = "detail")
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
        Image(painter = painterResource(id = imageID),
            contentDescription = "test",
            modifier = Modifier.size(150.dp))
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