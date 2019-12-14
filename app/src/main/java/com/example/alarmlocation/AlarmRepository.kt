package com.example.alarmlocation

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import com.example.alarmlocation.models.Alarm
import com.example.alarmlocation.models.database.AlarmDAO
import com.example.alarmlocation.models.database.AlarmDatabase
import com.example.alarmlocation.utils.addGeofencesOfAlarm
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmRepository (val application: Application){

    private var alarmDataBase: AlarmDatabase? = null
    private var alarmDao: AlarmDAO? = null
    private var alarmList: LiveData<List<Alarm>> = MutableLiveData()
    private var geofencingClient: GeofencingClient

    init {
        alarmDataBase = alarmDataBase?: Room.databaseBuilder(application, AlarmDatabase::class.java, "AlarmDatabase")
            .fallbackToDestructiveMigration() //temporary
            .build()
        alarmDao = alarmDataBase?.alarmDAO()
        geofencingClient = LocationServices.getGeofencingClient(application)
    }

    fun getAlarms() : LiveData<List<Alarm>>{
        return alarmDao!!.getAlarms()
    }

    fun insertAlarm( alarm: Alarm, viewModelScope: CoroutineScope){
        viewModelScope.launch (Dispatchers.IO){
            alarmDao?.insertAlarm( alarm )
        }
    }

    fun updateAlarmByKey( key: String, scope: CoroutineScope){
        scope.launch (Dispatchers.IO) {
                alarmDao?.updateAlarmStatus( key, false)
                geofencingClient.removeGeofences(listOf(key))
        }
    }

    fun updateAlarm( alarm: Alarm, viewModelScope: CoroutineScope){
        viewModelScope.launch (Dispatchers.IO){
            alarmDao?.updateAlarm( alarm )
            if(!alarm.isActive){
              geofencingClient.removeGeofences(listOf(alarm.key))
            } else{
                geofencingClient.addGeofencesOfAlarm(application, alarm)
            }
        }
    }

    fun deleteAlarm( alarm: Alarm, viewModelScope: CoroutineScope ){
        viewModelScope.launch (Dispatchers.IO){
            alarmDao?.deleteAlarm(alarm)
            geofencingClient.removeGeofences(listOf(alarm.key))
        }
    }

    fun deleteAlarmByKey( key: String, viewModelScope: CoroutineScope ){
        viewModelScope.launch (Dispatchers.IO){
            val toDelete = alarmDao?.getAlarmByKey(key)
            if(toDelete!=null) alarmDao?.deleteAlarm(toDelete)
            geofencingClient.removeGeofences(listOf(key))
        }
    }

    fun getAlarmByKey( key: String): Alarm?{
        return alarmDao?.getAlarmByKey( key )
    }

}