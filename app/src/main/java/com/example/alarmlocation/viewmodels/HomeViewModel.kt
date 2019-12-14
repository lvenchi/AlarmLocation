package com.example.alarmlocation.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.alarmlocation.AlarmRepository
import com.example.alarmlocation.MainActivity
import com.example.alarmlocation.models.Alarm

class HomeViewModel(application: Application) : AndroidViewModel(application) {


    private var repository: AlarmRepository = MainActivity.getRepository(application)

    fun getAlarms(): LiveData<List<Alarm>>?{
        return repository.getAlarms()
    }

    fun updateAlarm(alarm: Alarm){
        repository.updateAlarm(alarm, viewModelScope)
    }

    fun removeAlarms( alarm: Alarm){
        repository.deleteAlarm(alarm, viewModelScope)
    }

}