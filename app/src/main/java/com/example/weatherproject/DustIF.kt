package com.example.weatherproject

import android.app.Service
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface DustIF {


    @GET("getMinuDustFrcstDspth?")
    fun getDust(
        @Query("searchDate") searchDate: String,   // 예보날짜
        @Query("returnType") returnType: String,
        @Query("serviceKey") serviceKey: String,
        @Query("numOfRows") numOfRows: Int,   // 한 페이지 경과 수
        @Query("pageNo") pageNo: Int,          // 페이지 번호


    ): Call<DustParsingClass>


}
