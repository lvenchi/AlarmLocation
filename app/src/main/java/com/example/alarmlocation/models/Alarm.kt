package com.example.alarmlocation.models

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "Alarm", indices = [Index(value = ["key"], unique = true)])
data class Alarm (
    @PrimaryKey
    @NonNull
    @ColumnInfo(name="key") val key: String,
    @ColumnInfo(name = "latitude") val latitude: String,
    @ColumnInfo(name = "longitude") val longitude: String,
    @ColumnInfo(name = "range") val range: String,
    @ColumnInfo(name = "elapse") val elapse: String,
    @ColumnInfo(name = "active") var isActive: Boolean,
    @ColumnInfo(name = "timestamp") val timestamp: Long
)
