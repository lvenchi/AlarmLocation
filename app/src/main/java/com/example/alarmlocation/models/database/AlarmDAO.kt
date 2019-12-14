package com.example.alarmlocation.models.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.example.alarmlocation.models.Alarm

@Dao
interface AlarmDAO {

    @Insert( onConflict =  OnConflictStrategy.REPLACE)
    fun insertAlarm( alarm: Alarm )

    @Query("SELECT * FROM Alarm WHERE `key` LIKE :inKey LIMIT 1")
    fun getAlarmByKey( inKey: String ) : Alarm

    @Delete
    fun deleteAlarm( alarm: Alarm )

    @Query("DELETE FROM Alarm WHERE `key` LIKE :inKey")
    fun deleteAlarmByKey( inKey: String)

    @Query("SELECT * FROM Alarm ORDER BY timestamp")
    fun getAlarms(): LiveData<List<Alarm>>

    @Update
    fun updateAlarm( alarm: Alarm )

    @Query("UPDATE Alarm SET active = :status WHERE `key` LIKE :key")
    fun updateAlarmStatus( key: String, status: Boolean)


}