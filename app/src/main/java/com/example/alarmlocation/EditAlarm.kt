package com.example.alarmlocation

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.example.alarmlocation.databinding.FragmentEditAlarmBinding
import com.example.alarmlocation.models.Alarm
import com.example.alarmlocation.viewmodels.MapViewModel
import com.google.android.gms.common.internal.Constants
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_edit_alarm.view.*
import java.util.*


class EditAlarm : Fragment() {

    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mapViewModel: MapViewModel? = null
    private var locationManager: LocationManager? = null
    lateinit var geofencingClient: GeofencingClient
    private var circle: Circle? = null
    private var currMarker: Marker? = null
    private var alarmRepository: AlarmRepository? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alarmRepository = AlarmRepository(activity!!.application)
        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        geofencingClient = LocationServices.getGeofencingClient(activity!!)
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mapViewModel = activity?.run {
            ViewModelProviders.of(this).get( MapViewModel::class.java )
        }

        mapViewModel?.circleRadius?.observe(this, Observer { value ->
            googleMap?.let {
                circle?.radius = value.toDouble()
            }
        })

        val editAlarmBinding: FragmentEditAlarmBinding =
            DataBindingUtil.inflate( inflater, R.layout.fragment_edit_alarm, container, false)
        editAlarmBinding.lifecycleOwner = this
        editAlarmBinding.viewmodel = mapViewModel

        var isShown = false
        (childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment? ).also {
            it?.getMapAsync { resMap ->
                googleMap = resMap
                with(googleMap){
                    this?.isMyLocationEnabled = true
                    this?.setOnMarkerClickListener {tappedMarker ->
                        if(!isShown){
                            isShown = true
                            tappedMarker.showInfoWindow()

                        } else {
                            isShown = false
                            tappedMarker.hideInfoWindow()
                        }
                        true
                    }
                    this?.setOnMapLongClickListener { res ->
                        currMarker?.let { marker ->
                            circle?.remove()
                            circle = null
                            marker.remove()
                        }
                        isShown = false
                        currMarker = googleMap?.addMarker(
                            MarkerOptions().
                                position(res).
                                draggable(true).
                                title("Your Alarm").
                                snippet("The alarm will ring around here").
                                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).
                                alpha(0.8F)
                        )

                        circle = googleMap?.addCircle(
                            CircleOptions().center(currMarker!!.position)
                                .radius(mapViewModel?.circleRadius?.value!!.toDouble())
                                .clickable(false)
                                .strokeColor(R.color.colorPrimaryDark)
                        )

                        googleMap?.setOnMarkerDragListener( object : GoogleMap.OnMarkerDragListener{
                            override fun onMarkerDragEnd(p0: Marker?) {

                            }

                            override fun onMarkerDragStart(p0: Marker?) {

                            }

                            override fun onMarkerDrag(p0: Marker?) {
                                circle?.center = p0?.position
                            }
                        })
                        mapViewModel?.latlLng?.postValue(res)
                    }
                }
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location : Location? ->
                        if( location != null ) {
                            val myLoc = LatLng(location.latitude, location.longitude)
                            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 14F))
                        }
                    }
            }
        }
        return editAlarmBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bar = activity?.findViewById<SeekBar>(R.id.seekBar)
        bar?.let {
            it.progress = mapViewModel?.circleRadius?.value?.toInt()?.div(30) ?: 0
            it.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    mapViewModel?.circleRadius?.postValue(p1.toFloat()*30 + 1)
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }
            })
        }
        activity?.findViewById<FloatingActionButton>( R.id.confirm_button)?.setOnClickListener {
            if (currMarker != null && mapViewModel != null) {
                alarmRepository?.insertAlarm(
                    Alarm(currMarker!!.position.toString(),
                        currMarker!!.position.latitude.toString() ,
                        currMarker!!.position.longitude.toString(),
                        mapViewModel!!.circleRadius.value.toString(),
                        "", false, Calendar.getInstance().timeInMillis),
                    mapViewModel!!.viewModelScope
                )
                activity?.onBackPressed()
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }
}
