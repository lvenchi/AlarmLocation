package com.example.alarmlocation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng

class MapViewModel(application: Application) : AndroidViewModel(application) {

    val latlLng = MutableLiveData<LatLng>()
    val circleRadius: MutableLiveData<Float> = MutableLiveData(1F)

}