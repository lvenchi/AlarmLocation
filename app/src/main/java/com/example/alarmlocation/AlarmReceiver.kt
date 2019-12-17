package com.example.alarmlocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator
import android.util.Log;
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startForegroundService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class AlarmReceiver: BroadcastReceiver() {

    private var vibrator: Vibrator? = null

    override fun onReceive(p0: Context?, intent: Intent?) {

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = geofencingEvent.errorCode.toString()
            return
        }

        vibrator = getSystemService(p0!!, Vibrator::class.java)
        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL  ) {

            val key = intent?.getStringExtra("key")

            val intnt = Intent(p0, AlarmService::class.java)
                .setAction( AlarmService.action_launch_alarm)
                .putExtra("should_stop", false)
                .putExtra("key", key)

            //Thread {
                //Thread.sleep(5000)
                startForegroundService(p0, intnt)
            //}.start()
            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            Log.i("TAG", triggeringGeofences.toString())
        } else {

            // Log the error.
            Log.e("TAG", geofencingEvent.errorCode.toString())
        }
    }



}
