package com.example.alarmlocation

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.alarmlocation.utils.buildAndCreateChannel

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private var repository: AlarmRepository? = null

        fun getRepository( application: Application): AlarmRepository{
            if( repository == null ) repository = AlarmRepository(application)
            return repository!!
        }
    }

    override fun onNewIntent(intent: Intent?) {
        if(intent?.action == "com.example.alarmlocation.CANCEL_ALARM") {
            val key = intent.getStringExtra("key") ?: ""
            val serviceIntent = Intent(application, AlarmService::class.java).let {
                it.putExtra("should_stop", true)
                it.putExtra("key", key)
                it.action = AlarmService.action_launch_alarm
                it
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else startService(serviceIntent)
        }
        super.onNewIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NavigationUI.setupWithNavController(toolbar, Navigation.findNavController(this, R.id.nav_host_fragment))
        (ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager).buildAndCreateChannel(this)
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1676)

        } else {
            // Permission has already been granted
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
