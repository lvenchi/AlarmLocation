package com.example.alarmlocation.models

import android.view.View
import androidx.annotation.NonNull
import androidx.room.*

@Entity(tableName = "Alarm", indices = [Index(value = ["key"], unique = true), Index(value = ["timestamp"], unique = true)])
data class Alarm (

    @ColumnInfo(name="key") val key: String,
    @ColumnInfo(name = "latitude") val latitude: String,
    @ColumnInfo(name = "longitude") val longitude: String,
    @ColumnInfo(name = "range") val range: String,
    @ColumnInfo(name = "elapse") val elapse: String,
    @ColumnInfo(name = "active") var isActive: Boolean,

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "timestamp") val timestamp: Long
)
