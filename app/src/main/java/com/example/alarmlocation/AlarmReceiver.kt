package com.example.alarmlocation;

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.os.Vibrator
import android.util.Log
import androidx.core.content.ContextCompat.*
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

            val activityIntnt = Intent(p0, RemoveAlarmActivity::class.java)
                .setAction("com.example.alarmlocation.SHOW_ALARM")
                .putExtra("should_stop", false)
                .putExtra("key", key).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            Thread {
                Thread.sleep(5000)
                if(isAppForeground(p0))
                    startForegroundService(p0, intnt)
                else {
                    val pm = p0.getSystemService(Context.POWER_SERVICE) as PowerManager
                    val wl = pm.newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                        "myalarmapp:alarm."
                    )
                    wl.acquire(600000)
                    startActivity(p0, activityIntnt, null)
                    wl.release()

                }
            }.start()
            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            Log.i("TAG", triggeringGeofences.toString())
        } else {

            // Log the error.
            Log.e("TAG", geofencingEvent.errorCode.toString())
        }
    }

    fun isAppForeground(mContext: Context): Boolean {
        val myKM = mContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return !myKM.isDeviceLocked
    }

}
