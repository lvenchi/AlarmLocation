package com.example.alarmlocation.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.alarmlocation.MainActivity
import com.example.alarmlocation.R
import com.example.alarmlocation.AlarmService

private val notificationId = 19

fun NotificationManager.createNotification(context: Context, title: String, body: String, key: String): Notification {

    val pendingIntent: PendingIntent = Intent(context, AlarmService::class.java).let {aint ->
        aint.putExtra("should_stop", true)
        aint.action = AlarmService.action_launch_alarm
        aint.putExtra("key", key)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(context, 100, aint, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getService(context, 100, aint, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    val pendingIntent1: PendingIntent = Intent(context, MainActivity::class.java).let { int ->
        int.putExtra("val", title)
        int.putExtra("key", key)
        int.action = "com.example.alarmlocation.CANCEL_ALARM"
        int.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        int.addCategory(Intent.CATEGORY_LAUNCHER)
        PendingIntent.getActivity(context, 32, int, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    val notificationBuilder = NotificationCompat
        .Builder( context, context.getString(R.string.alarmid))
        .setContentTitle(title)
        .setAutoCancel(true)
        .setOnlyAlertOnce(false)
        .setContentIntent(pendingIntent1)
        .addAction(R.drawable.ic_launcher_foreground, "Stop Alarm", pendingIntent)
        .setContentText(body)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setSound(RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM))
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setStyle(NotificationCompat.BigTextStyle())

    return notificationBuilder.build()
}

fun NotificationManager.buildAndCreateChannel(context: Context){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){//&& getNotificationChannel( context.getString(R.string.alarmid)) == null) {
        deleteNotificationChannel(context.getString(R.string.alarmid))
        val notificationChannel: NotificationChannel = NotificationChannel(
            context.getString(R.string.alarmid),
            context.getString(R.string.alarmid),
            NotificationManager.IMPORTANCE_HIGH
        ).also {
            it.description = "Peni"
            it.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            it.enableLights(true)
            //it.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000, 1000)
            it.setBypassDnd(true)
            it.lightColor = Color.GREEN
            it.enableVibration(true)
            it.description = "Booking Notification"
        }
        createNotificationChannel(notificationChannel)
    }
}