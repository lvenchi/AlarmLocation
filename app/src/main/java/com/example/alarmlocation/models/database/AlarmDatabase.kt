package com.example.alarmlocation.models.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.alarmlocation.models.Alarm

@Database(entities = [Alarm::class], version = 5, exportSchema = false)
abstract class AlarmDatabase : RoomDatabase() {

    abstract fun alarmDAO(): AlarmDAO

}