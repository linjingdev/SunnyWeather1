package com.example.sunnyweather.logic.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.await
import retrofit2.http.Query
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.ln

object SunnyWeatherNetWork {


    private val weatherService = ServiceCreator.create(WeatherService::class.java);
    //得到await()函数的返回值后将该数据在返回到上一层
    suspend fun getDailyWeather(lng: String,lat: String) = weatherService.getDailyWeather(lng,lat).await();

    suspend fun getRealtimeWeather(lng: String,lat: String) = weatherService.getRealtimeWeather(lng,lat).await();

    private val placeService = ServiceCreator.create(PlaceService::class.java)

    suspend fun searchPlaces(query: String) = placeService.searchPlaces(query).await()

    //将解析出来的数据模型对象取出并返回，同时回复当前协程的执行
    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T>{
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body();
                    if (body != null) continuation.resume(body)
                    else continuation.resumeWithException(RuntimeException("response body is null"))
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t);
                }
            })
        }
    }
}