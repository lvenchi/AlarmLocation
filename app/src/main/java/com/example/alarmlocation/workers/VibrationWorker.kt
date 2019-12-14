package com.example.alarmlocation.workers

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.work.Worker
import androidx.work.WorkerParameters

class VibrationWorker(val context: Context, params: WorkerParameters) : Worker( context, params) {

    override fun doWork(): Result {
        val vib = (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
        while(!this.isStopped){
            if(!this.isStopped) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vib.vibrate(
                        VibrationEffect.createOneShot(
                            1000,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else vib.vibrate(1000)

                try{
                    if (!this.isStopped) {
                        Thread.sleep(2000)
                    }
                } catch (ex: InterruptedException){
                    println(ex.toString())
                }
            }
        }
        return Result.success()
    }
}
