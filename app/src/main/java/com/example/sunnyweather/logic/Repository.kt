package com.example.sunnyweather.logic

import androidx.lifecycle.liveData
import com.example.sunnyweather.logic.Repository.isPlaceSaved
import com.example.sunnyweather.logic.Repository.savePlace
import com.example.sunnyweather.logic.dao.PlaceDao
import com.example.sunnyweather.logic.model.Place
import com.example.sunnyweather.logic.model.Weather
import com.example.sunnyweather.logic.network.SunnyWeatherNetWork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.Exception
import java.lang.RuntimeException
import kotlin.coroutines.CoroutineContext

//仓库层统一封装入口
object Repository {

    fun searchPlaces(query: String) = fire(Dispatchers.IO) {
        val placeResponse = SunnyWeatherNetWork.searchPlaces(query);
        if (placeResponse.status == "ok") {
            val places = placeResponse.places;
            Result.success(places);
        } else {
            Result.failure(RuntimeException("response status is ${placeResponse.status}"))
        }
    }

    //Dispatchers.IO:将所有代码都运行在子线程中
    fun refreshWeather(lng: String, lat: String, placeName: String) = fire(Dispatchers.IO) {
        coroutineScope {
            val deferredRealtime = async {
                SunnyWeatherNetWork.getRealtimeWeather(lng, lat);
            }
            val deferredDaily = async {
                SunnyWeatherNetWork.getDailyWeather(lng, lat);
            }
            val realtimeResponse = deferredRealtime.await();
            val dailyResponse = deferredDaily.await();
            if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                val weather =
                    Weather(realtimeResponse.result.realtime, dailyResponse.result.daily);
                Result.success(weather);
            } else {
                Result.failure(
                    RuntimeException(
                        "realtime response status is ${realtimeResponse.status}" +
                                "daily response status is ${dailyResponse.status}"
                    )
                )
            }
        }
    }

    fun savePlace(place: Place) = PlaceDao.savePlace(place)

    fun getSavedPlace() = PlaceDao.getSavedPlace()

    fun isPlaceSaved() = PlaceDao.isPlaceSaved()

    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block();
            } catch (e: Exception) {
                Result.failure<T>(e);
            }
            emit(result);
        }
}