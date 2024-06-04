package com.example.weatherproject

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherIF {
    // getUltraSrtFcst : 초단기 예보 조회 + 인증키
    @GET("getVilageFcst?serviceKey=Z%2BVGgQxuJdpbHl7FH1zN%2FAa3LNv6M4Vyh0VHh6%2BaY6YN1u7%2BNQRX%2FM4A1PuZlx8uVUP4FEd6dODHdZ8Ikg494w%3D%3D")
    fun getWeather(
        @Query("numOfRows") num_of_rows: Int,   // 한 페이지 경과 수
        @Query("pageNo") page_no: Int,          // 페이지 번호
        @Query("dataType") data_type: String,   // 응답 자료 형식
        @Query("base_date") base_date: String,  // 발표 일자
        @Query("base_time") base_time: String,  // 발표 시각
        @Query("nx") nx: String,                // 예보지점 X 좌표
        @Query("ny") ny: String,                 // 예보지점 Y 좌표
    ): Call<String>


}
