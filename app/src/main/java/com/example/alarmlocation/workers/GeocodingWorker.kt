package com.example.alarmlocation.workers

import android.content.Context
import android.location.Geocoder
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.*

class GeocodingWorker( val context: Context, val params: WorkerParameters) : Worker(context, params){

    override fun doWork(): Result {
        val res = Geocoder(context, Locale.ITALIAN).
            getFromLocation(params.inputData.getDouble("lat", 0.0),
                inputData.getDouble("long", 0.0), 5)[0].getAddressLine(0)
        return Result.success(Data.Builder().putString("address",res).build())
    }

}