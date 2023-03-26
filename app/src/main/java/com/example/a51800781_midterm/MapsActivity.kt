package com.example.a51800781_midterm

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.a51800781_midterm.PermissionUtils.PermissionDeniedDialog.Companion.newInstance
import com.example.a51800781_midterm.PermissionUtils.isPermissionGranted
import com.example.a51800781_midterm.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.*


class MapsActivity : AppCompatActivity(), GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var permissionDenied = false
    private var start: String = ""
    private var end: String = ""
    private var address: String = ""
    private var key: String = ""
    private var mode: String = ""

    private var markerPoints: ArrayList<LatLng> = ArrayList()
    private val directionAPI: GoogleMapAPI = MapAPI.getInstance().create(GoogleMapAPI::class.java)

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        enableMyLocation()

        val extras = intent.extras
        if (extras != null) {
            mode = extras.getString("mode").toString()
            start = extras.getString("start").toString()
            end = extras.getString("end").toString()
            address = extras.getString("address").toString()
            key = extras.getString("key").toString()
        }

        when (mode) {
            "FIND_TDTU" -> {
                val TDT = LatLng(10.7326689, 106.6997696)
                mMap.addMarker(MarkerOptions().position(TDT).title("Marker in TDTU"))
                val cameraPosition = CameraPosition.Builder()
                    .target(TDT)
                    .zoom(15f)
                    .build()
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
            "FIND_LOCATION" -> {
                runBlocking {
                    launch(coroutineContext) {
                        val result = withContext(Dispatchers.IO) {
                            directionAPI.getGeoCode(address = address, key = key)
                        }
                        val location = LatLng(
                            result.body()?.results?.get(0)?.geometry?.location?.lat
                                ?: 10.7326689,
                            result.body()?.results?.get(0)?.geometry?.location?.lng ?: 106.6997696
                        )
                        val cameraPosition = CameraPosition.Builder()
                            .target(location)
                            .zoom(15f)
                            .build()
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                    }
                }
            }
            "FIND_PATH" -> {
                runBlocking {
                    launch(coroutineContext) {
    //                     Limited to 1 request per day
                        val result = withContext(Dispatchers.IO) {
                            directionAPI.getDirection(origin = start, destination = end, key = key)
                        }
                        Log.d("HUY_DEBUG", result.toString())
                        var polygonEncoded =
                            result.body()?.routes?.get(0)?.overview_polyline?.points.toString()
                        if (polygonEncoded.startsWith("null")) {
                            polygonEncoded = "gfo`AoyfjSnNaAjF[n@A~AIAWO@gBJ{ANiCPcAFkAg@q@YmHeCoBm@aGoBwCkAsCcAo@SyAU}CSsLoAkAKKZo@dH[`DcCW"
                        }

                        val direction: List<LatLng> = PolyUtil.decode(polygonEncoded)
                        val options = PolylineOptions()
                        options.color(Color.RED)
                        options.width(5f)
                        options.addAll(direction)
                        mMap.addPolyline(options)
                        val cameraPosition = CameraPosition.Builder()
                            .target(direction[0])
                            .zoom(15f)
                            .build()
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                    }
                }

            }
        }
        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setOnMyLocationClickListener(this)

        mMap.setOnMapClickListener { latLng ->
            if (markerPoints.size > 1) {
                markerPoints.clear()
                mMap.clear()
            }

            // Adding new item to the ArrayList
            markerPoints.add(latLng)

            // Creating MarkerOptions
            val options = MarkerOptions()

            // Setting the position of the marker
            options.position(latLng)
            if (markerPoints.size == 1) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            } else if (markerPoints.size == 2) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            }

            // Add new marker to the Google Map Android API V2
            mMap.addMarker(options)

            // Checks, whether start and end locations are captured
            if (markerPoints.size >= 2) {
                val origin = markerPoints[0]
                val dest = markerPoints[1]
                // Getting URL to the Google Directions API
                runBlocking {
                    launch(coroutineContext) {
                        val result =
                            directionAPI.getDirection(
                                origin = "${origin.latitude},${origin.longitude}",
                                destination = "${dest.latitude},${dest.longitude}",
                                key = key
                            )
                        val polygonEncoded =
                            result.body()?.routes?.get(0)?.overview_polyline?.points.toString()
                        val direction: List<LatLng> = PolyUtil.decode(polygonEncoded)
                        // what is Name shadowed: options
                        val options = PolylineOptions()
                        options.color(Color.RED)
                        options.width(5f)
                        options.addAll(direction)
                        mMap.addPolyline(options)
                        val cameraPosition = CameraPosition.Builder()
                            .target(direction[0])
                            .zoom(15f)
                            .build()
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {

        // [START maps_check_location_permission]
        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            return
        }

        // 2. If if a permission rationale dialog should be shown
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            PermissionUtils.RationaleDialog.newInstance(
                LOCATION_PERMISSION_REQUEST_CODE, true
            ).show(supportFragmentManager, "dialog")
            return
        }

        // 3. Otherwise, request permission
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
        // [END maps_check_location_permission]
    }


    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT)
            .show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this, "Current location:\n$location", Toast.LENGTH_LONG)
            .show()
    }

    // [START maps_check_location_permission_result]
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
            return
        }

        if (isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true
            // [END_EXCLUDE]
        }
    }

    // [END maps_check_location_permission_result]
    override fun onResumeFragments() {
        super.onResumeFragments()
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError()
            permissionDenied = false
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private fun showMissingPermissionError() {
        newInstance(true).show(supportFragmentManager, "dialog")
    }


    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }


}

