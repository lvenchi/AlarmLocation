package com.example.alarmlocation.viewmodels

import android.app.Application
import android.view.View
import androidx.lifecycle.*
import androidx.paging.PagedList
import com.example.alarmlocation.AlarmRepository
import com.example.alarmlocation.MainActivity
import com.example.alarmlocation.models.Alarm

class HomeViewModel(application: Application) : AndroidViewModel(application) {


    private var repository: AlarmRepository = MainActivity.getRepository(application)

    var isListEdit: MutableLiveData<Int> = MutableLiveData(View.GONE)

    fun getAlarms(): LiveData<PagedList<Alarm>>?{
        return repository.getAlarms()
    }

    fun updateAlarm(alarm: Alarm){
        repository.updateAlarm(alarm, viewModelScope)
    }

    fun removeAlarms( alarm: Alarm){
        repository.deleteAlarm(alarm, viewModelScope)
    }

}