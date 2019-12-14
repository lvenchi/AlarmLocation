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

fun NotificationManager.sendNotification(context: Context, title: String, body: String){

    val remoteViews = RemoteViews("com.example.alarmlocation", R.layout.alarm_screen)

    val pendingIntent: PendingIntent = Intent(context, AlarmService::class.java).let {
        PendingIntent.getService(context, 100, it, 0)
    }
    val notificationBuilder = NotificationCompat
        .Builder( context, context.getString(R.string.alarmid))
        .setCustomBigContentView(remoteViews)
        .setContentTitle(title)
        .addAction(R.drawable.ic_launcher_foreground, "Stop Alarm", pendingIntent)
        .setContentText(body)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setSound(RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM))
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setStyle(NotificationCompat.BigTextStyle())

    notify(notificationId, notificationBuilder.build())
}

fun NotificationManager.createNotification(context: Context, title: String, body: String, key: String): Notification {

    val pendingIntent: PendingIntent = Intent(context, AlarmService::class.java).let {
        it.putExtra("should_stop", true)
        it.action = AlarmService.action_launch_alarm
        it.putExtra("key", key)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(context, 100, it, 0)
        } else {
            PendingIntent.getService(context, 100, it, 0)
        }
    }

    val pendingIntent1: PendingIntent = Intent(context, MainActivity::class.java).let {
        it.putExtra("val", title)
        it.putExtra("key", key)
        it.action = "com.example.alarmlocation.CANCEL_ALARM"
        it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        it.addCategory(Intent.CATEGORY_LAUNCHER)
        PendingIntent.getActivity(context, 32, it, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    val notificationBuilder = NotificationCompat
        .Builder( context, context.getString(R.string.alarmid))
        .setContentTitle(title)
        .setContentIntent(pendingIntent1)
        .addAction(R.drawable.ic_launcher_foreground, "Stop Alarm", pendingIntent)
        .setContentText(body)
        .setAutoCancel(true)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setSound(RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM))
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setStyle(NotificationCompat.BigTextStyle())

    return notificationBuilder.build()
}

fun NotificationManager.buildAndCreateChannel(context: Context){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel: NotificationChannel = NotificationChannel(
            context.getString(R.string.alarmid),
            context.getString(R.string.alarmid),
            NotificationManager.IMPORTANCE_HIGH
        ).also {

            it.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            it.enableLights(true)
            it.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000, 1000)
            it.setBypassDnd(true)
            it.lightColor = Color.GREEN
            it.enableVibration(true)
            it.description = "Booking Notification"
        }
        createNotificationChannel(notificationChannel)
    }
}