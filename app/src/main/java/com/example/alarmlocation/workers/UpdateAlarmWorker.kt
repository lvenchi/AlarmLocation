package com.example.alarmlocation.workers

import android.app.Application
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.alarmlocation.MainActivity
import kotlinx.coroutines.GlobalScope

class UpdateAlarmWorker( val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val address = inputData.getString("address")
        val key = inputData.getString("key") ?: ""
        MainActivity.getRepository( context as Application).updateAlarmAddressByKey(key, address)
        return Result.success()
    }
}