package com.example.googlemapsgoogleplaces

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil.setContentView
import com.example.googlemapsgoogleplaces.databinding.ActivityMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var binding:ActivityMapBinding

    private val TAG = "MapActivity"

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    private val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    private val COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    private val LOCATION_PERMISSION_REQUEST_CODE = 1234
    private val DEFAULT_ZOOM = 16f

    private var mLocationPermissionsGranted = false
    private var mMap: GoogleMap? = null

    private var editText: EditText? = null

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.activity_map)
        getLocationPermission()
        getDeviceLocation()
        editText = findViewById(R.id.search_bar)
        init()
    }

    private fun getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions")
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (ContextCompat.checkSelfPermission(
                this.applicationContext, FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (ContextCompat.checkSelfPermission(
                    this.applicationContext,
                    COURSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mLocationPermissionsGranted = true
                initMap()

            } else {
                ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                permissions,
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionsResult: called.")
        mLocationPermissionsGranted = false

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    var i = 0
                    while (i < grantResults.size) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false
                            Log.d(
                                TAG,
                                "onRequestPermissionsResult: permission failed"
                            )
                            return
                        }
                        i++
                    }
                    Log.d(
                        TAG,
                        "onRequestPermissionsResult: permission granted"
                    )
                    mLocationPermissionsGranted = true
                    //initialize our map
                    initMap()
                }
            }
        }
    }

    private fun initMap() {
        Log.d(TAG, "initMap: initializing map")
        val mapFragment = supportFragmentManager.findFragmentById(
            R.id.map
        ) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show()
        Log.e(TAG, "onMapReady: map is ready")
        mMap = googleMap

        if(mLocationPermissionsGranted) {
            getDeviceLocation()
            mMap!!.isMyLocationEnabled = true
            mMap!!.uiSettings.isMyLocationButtonEnabled = false
        }
    }

    private fun getDeviceLocation() {
        Log.e(TAG, "getDeviceLocation: getting device current location")
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            if(mLocationPermissionsGranted) {
                var location = fusedLocationProviderClient?.lastLocation
                location?.addOnCompleteListener {it ->
                    if(it.isSuccessful) {
                        Log.e(TAG, "onComplete: Found location")
                   if(it != null) {
                       val currentLocation = it.result as Location
                       moveCamera(LatLng(currentLocation.latitude, currentLocation.longitude),
                           DEFAULT_ZOOM, "My Location")
                   }
//                        mMap!!.isMyLocationEnabled = true


                    } else {
                        Log.e(TAG, "onComplete: current location is null")
                        Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "getDeviceLocation: " + e.message)
        }

    }

    private fun moveCamera(latLng: LatLng, zoom: Float, title: String ) {
        Log.e(TAG, "moveCamera: moving camera to : $latLng")
//        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        val cameraPosition =
            CameraPosition.Builder().target(latLng).zoom(zoom).build()
        mMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        var markerOptions = MarkerOptions()
            .position(latLng)
            .title(title)

        mMap?.addMarker(markerOptions)


    }

    private fun init() {
        Log.e(TAG, "init: initializing")
        binding.inputSearch.setOnEditorActionListener { v, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER
            ) { //execute our method for searching
                Toast.makeText(this, "onSearch", Toast.LENGTH_SHORT)
                    .show()
                geoLocate()

            }
            false
        }

        binding.icGps.setOnClickListener {
            getDeviceLocation()
        }

    }

    private fun geoLocate() {
        Log.e(TAG, "geoLocate: geolocating")
        var searchString = binding.inputSearch.text.toString()
        var geocoder = Geocoder(this)
        var list: ArrayList<Address>

        try {
            list = geocoder.getFromLocationName(searchString, 1) as ArrayList<Address>
            if(list.isNotEmpty()) {
                var address = list[0]
                Log.e(TAG, "geoLocate: found a location$address")
                moveCamera(LatLng(address.latitude, address.longitude),
                    DEFAULT_ZOOM, address.getAddressLine(0))
            }
        } catch (e: IOException) {
            Log.e(TAG, "geoLocate: exception" + e.message)

        }
    }


}