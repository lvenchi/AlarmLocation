package com.example.alarmlocation.workers

import android.content.Context
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.preference.RingtonePreference
import androidx.work.Worker
import androidx.work.WorkerParameters

class RingtoneWorker(val context: Context, params: WorkerParameters) : Worker( context, params) {

    val mediaPlayer = MediaPlayer.create(context, RingtoneManager.getActualDefaultRingtoneUri(context,RingtoneManager.TYPE_ALARM ))

    override fun doWork(): Result {
        mediaPlayer.isLooping = true
        mediaPlayer.start()

        while(!this.isStopped){
          try {
                Thread.sleep(500)
            } catch (ex: InterruptedException){}
        }

        mediaPlayer.isLooping = false
        mediaPlayer.stop()

        return Result.success()
    }

}