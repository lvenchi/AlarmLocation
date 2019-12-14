package com.example.alarmlocation.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.alarmlocation.models.Alarm

class SingleAlarmViewModel : ViewModel() {

    val alarm: MutableLiveData<Alarm> = MutableLiveData()

}