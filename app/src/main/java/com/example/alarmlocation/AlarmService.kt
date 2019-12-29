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


class AlarmService : Service( ) {

    companion object {
        const val action_launch_alarm = "com.example.alarmlocation.FIRE_ALARM"
        const val id = 10
        var isRunning = false
    }

    private val workManager = WorkManager.getInstance(application)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val notificationManager = ContextCompat.getSystemService(
            baseContext,
            NotificationManager::class.java
        ) as NotificationManager

        if(intent != null) {
            if (!intent.getBooleanExtra("should_stop", true)) {
                isRunning = true
                val key: String = intent.getStringExtra("key") ?: ""
                val notification = notificationManager.createNotification(
                    baseContext,
                    "Wake Up!",
                    "You arrived!",
                    key
                )
                startForeground(10, notification )
                notificationManager.notify(10, notification )

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
                if(isRunning) {
                    val key: String = intent.getStringExtra("key") ?: ""
                    workManager.cancelUniqueWork("VibrateWorker")
                    workManager.cancelUniqueWork("RingtoneWorker")
                    MainActivity.getRepository(application).updateAlarmByKey(key, GlobalScope)
                    isRunning = false
                    stopForeground(true)
                    stopSelf()
                } else {
                    val notification = notificationManager.createNotification(
                        baseContext,
                        "Wake Up!",
                        "You arrived!",
                        ""
                    )
                    startForeground(10, notification )
                    stopForeground(true)
                    stopSelf()
                }
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