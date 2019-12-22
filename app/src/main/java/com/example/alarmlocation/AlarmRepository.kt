package com.example.alarmlocation

import android.app.Application
import android.location.Address
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import androidx.paging.toLiveData
import androidx.room.Room
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.alarmlocation.models.Alarm
import com.example.alarmlocation.models.database.AlarmDAO
import com.example.alarmlocation.models.database.AlarmDatabase
import com.example.alarmlocation.utils.addGeofencesOfAlarm
import com.example.alarmlocation.workers.GeocodingWorker
import com.example.alarmlocation.workers.UpdateAlarmWorker
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AlarmRepository (val application: Application){

    private var alarmDataBase: AlarmDatabase? = null
    private var alarmDao: AlarmDAO? = null
    private var geofencingClient: GeofencingClient
    private var workManager: WorkManager = WorkManager.getInstance(application)

    init {
        alarmDataBase = alarmDataBase?: Room.databaseBuilder(application, AlarmDatabase::class.java, "AlarmDatabase")
            .fallbackToDestructiveMigration() //temporary
            .build()
        alarmDao = alarmDataBase?.alarmDAO()
        geofencingClient = LocationServices.getGeofencingClient(application)
    }

    fun getAlarms() : LiveData<PagedList<Alarm>>{
        return alarmDao!!.getAlarms().toLiveData(30)
    }

    fun translateCoordinates( latLng: LatLng, key: String) : Address?{

        val req = OneTimeWorkRequest.Builder(GeocodingWorker::class.java)
        val req1 = OneTimeWorkRequest.Builder(UpdateAlarmWorker::class.java)
        req1.setInputData(Data.Builder().putString("key", key).build())
        req.setInputData(
            Data.Builder().
                putDouble("lat", latLng.latitude).
                putDouble("long", latLng.longitude).
                build())
        workManager.beginWith(req.build()).then(req1.build()).enqueue()

        return null
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

    fun updateAlarmAddressByKey( key: String, address: String?, viewModelScope: CoroutineScope){
        viewModelScope.launch(Dispatchers.IO) {
            alarmDao?.updateAlarmAddressByKey(key, address)
        }
    }

    fun updateAlarmAddressByKey( key: String, address: String?){
        //viewModelScope.launch(Dispatchers.IO) {
            alarmDao?.updateAlarmAddressByKey(key, address)
        //}
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