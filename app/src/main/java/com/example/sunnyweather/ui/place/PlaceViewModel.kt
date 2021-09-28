package com.example.sunnyweather.ui.place

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.sunnyweather.logic.Repository
import com.example.sunnyweather.logic.Repository.getSavedPlace
import com.example.sunnyweather.logic.Repository.savePlace
import com.example.sunnyweather.logic.dao.PlaceDao
import com.example.sunnyweather.logic.model.Place
import retrofit2.http.Query

class PlaceViewModel : ViewModel() {

    private val searchLiveData = MutableLiveData<String>();

    //对界面上显式的城市数据进行缓存，在旋转屏幕时数据不会丢失
    val placeList = ArrayList<Place>();

    val placeLiveData = Transformations.switchMap(searchLiveData){ query->
        Repository.searchPlaces(query);
    }

    fun searchPlaces(query: String){
        searchLiveData.value = query;
    }

    fun savePlace(place: Place) = Repository.savePlace(place)

    fun getSavedPlace() = Repository.getSavedPlace()

    fun isPlaceSaved() = Repository.isPlaceSaved()
}