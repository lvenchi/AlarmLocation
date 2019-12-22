package com.example.alarmlocation


import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.alarmlocation.databinding.FragmentEditAlarmBinding
import com.example.alarmlocation.databinding.FragmentEditExistingAlarmBinding
import com.example.alarmlocation.models.Alarm
import com.example.alarmlocation.viewmodels.MapViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


class EditExistingAlarm : Fragment() {


    private var googleMap: GoogleMap? = null
    private var mapViewModel: MapViewModel? = null
    private var locationManager: LocationManager? = null
    private var circle: Circle? = null
    private var currMarker: Marker? = null
    private var alarmRepository: AlarmRepository? = null
    private var alarmKey: String? = null
    private var alarm: Alarm? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        alarmKey = arguments?.getString("alarmkey")
        alarmRepository = MainActivity.getRepository(activity!!.application)
        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mapViewModel = activity?.run {
            ViewModelProviders.of(this).get( MapViewModel::class.java )
        }

        val editAlarmBinding: FragmentEditExistingAlarmBinding =
            DataBindingUtil.inflate( inflater, R.layout.fragment_edit_existing_alarm, container, false)
        editAlarmBinding.lifecycleOwner = this
        editAlarmBinding.viewmodel = mapViewModel

        mapViewModel?.viewModelScope?.launch(Dispatchers.IO) {
            alarm = alarmRepository?.getAlarmByKey(alarmKey ?: "")
            mapViewModel?.viewModelScope?.launch(Dispatchers.Main){handleResult()}
            editAlarmBinding.root.findViewById<SeekBar>(R.id.seekBar)
                .progress = alarm?.range?.toFloat()?.toInt()?.div(30) ?: 0

        }
        return editAlarmBinding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        activity?.findViewById<SeekBar>(R.id.seekBar)?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                mapViewModel?.circleRadius?.postValue(p1.toFloat()*30 + 1)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })
        activity?.findViewById<FloatingActionButton>( R.id.confirm_button)?.setOnClickListener {
            if (currMarker != null && mapViewModel != null) {

                val newAlarm = Alarm(currMarker!!.position.toString(),
                    currMarker!!.position.latitude.toString() ,
                    currMarker!!.position.longitude.toString(),
                    mapViewModel!!.circleRadius.value.toString(),
                    "", false,null, alarm!!.timestamp)


                alarmRepository?.updateAlarm(
                    newAlarm,
                    mapViewModel!!.viewModelScope)

                alarmRepository?.translateCoordinates(currMarker!!.position, newAlarm.key)
                Navigation.findNavController(activity!!, R.id.nav_host_fragment).popBackStack()
                //activity?.onBackPressed()
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    fun handleResult( ){

            if(alarm != null){
                mapViewModel?.latlLng?.postValue( LatLng(alarm!!.latitude.toDouble(), alarm!!.longitude.toDouble()))
                mapViewModel?.circleRadius?.postValue(alarm!!.range.toFloat())

                var isShown = false
                (childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment? ).also {
                    it?.getMapAsync { resMap ->
                        googleMap = resMap
                        with(googleMap){
                            val myLoc = LatLng(alarm!!.latitude.toDouble(), alarm!!.longitude.toDouble())
                            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 14F))
                            this?.isMyLocationEnabled = true

                            mapViewModel?.circleRadius?.observe(activity!!, Observer { value ->
                                googleMap?.let {
                                    circle?.radius = value.toDouble()
                                }
                            })

                            currMarker = googleMap?.addMarker(
                                MarkerOptions().
                                    position(mapViewModel!!.latlLng.value!!).
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
                                    .fillColor(R.color.colorAccent)
                            )

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
                                currMarker?.remove()
                                isShown = false
                                currMarker = googleMap?.addMarker(
                                    MarkerOptions().position(res).draggable(true).title("Your Alarm").snippet(
                                        "The alarm will ring around here"
                                    ).icon(
                                        BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_AZURE
                                        )
                                    ).alpha(0.8F)
                                )

                                if(circle != null) circle?.center = currMarker?.position else circle = googleMap?.addCircle(
                                    CircleOptions().center(currMarker!!.position)
                                        .radius(mapViewModel?.circleRadius?.value!!.toDouble())
                                        .clickable(false)
                                        .strokeColor(R.color.colorPrimaryDark)
                                )
                                mapViewModel?.latlLng?.postValue(res)
                            }

                                googleMap?.setOnMarkerDragListener( object : GoogleMap.OnMarkerDragListener{
                                    override fun onMarkerDragEnd(p0: Marker?) {

                                    }

                                    override fun onMarkerDragStart(p0: Marker?) {

                                    }

                                    override fun onMarkerDrag(p0: Marker?) {
                                        circle?.center = p0?.position
                                    }
                                })

                            }
                        }
                    }
                }
            }
        }

