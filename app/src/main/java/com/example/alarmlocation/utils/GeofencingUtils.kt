package com.example.alarmlocation.utils

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import com.example.alarmlocation.AlarmReceiver
import com.example.alarmlocation.models.Alarm
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest

fun GeofencingClient.addGeofencesOfAlarm(application: Application, alarm: Alarm){

    addGeofences(
        GeofencingRequest.Builder().addGeofence(
            Geofence.Builder()
                .setCircularRegion(
                    alarm.latitude.toDouble(),
                    alarm.longitude.toDouble(),
                    alarm.range.toFloat())
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
                .setRequestId(alarm.key)
                .setNotificationResponsiveness(1000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setLoiteringDelay(1000)
                .build())
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_DWELL )
            .build(),
        PendingIntent.getBroadcast(
            application,
            1234,
            Intent(application, AlarmReceiver::class.java).putExtra("key", alarm.key),
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_ONE_SHOT
        )
    )
}