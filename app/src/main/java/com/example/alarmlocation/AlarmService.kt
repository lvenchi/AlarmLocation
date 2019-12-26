package com.example.alarmlocation

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.alarmlocation.utils.createNotification
import com.example.alarmlocation.workers.RingtoneWorker
import com.example.alarmlocation.workers.VibrationWorker
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope


class AlarmService : Service( ) {

    companion object {
        val action_launch_alarm = "com.example.alarmlocation.FIRE_ALARM"
        val id = 10
    }

    private val workManager = WorkManager.getInstance(application)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val notificationManager = ContextCompat.getSystemService(
            baseContext,
            NotificationManager::class.java
        ) as NotificationManager

        if(intent != null) {
            if (!intent.getBooleanExtra("should_stop", true)) {
                val key: String = intent.getStringExtra("key") ?: ""
                startForeground(
                    10,
                    notificationManager.createNotification(
                        baseContext,
                        "Wake Up!",
                        "You arrived!",
                        key
                    )
                )

                workManager.beginUniqueWork(
                    "VibrateWorker",
                    ExistingWorkPolicy.REPLACE,
                    OneTimeWorkRequest.from(VibrationWorker::class.java)
                ).enqueue()
                workManager.beginUniqueWork(
                    "RingtoneWorker",
                    ExistingWorkPolicy.REPLACE,
                    OneTimeWorkRequest.from(RingtoneWorker::class.java)
                ).enqueue()

            } else {
                val key: String = intent.getStringExtra("key") ?: ""
                workManager.cancelUniqueWork("VibrateWorker")
                workManager.cancelUniqueWork("RingtoneWorker")
                MainActivity.getRepository(application).updateAlarmByKey(key, GlobalScope)
                stopForeground(true)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        workManager.cancelUniqueWork("VibrateWorker")
        workManager.cancelUniqueWork("RingtoneWorker")
        super.onDestroy()
    }

}