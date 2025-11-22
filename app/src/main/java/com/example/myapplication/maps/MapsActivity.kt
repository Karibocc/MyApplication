package com.example.myapplication.maps

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val DEFAULT_ZOOM = 12f
        private const val LOCATION_ZOOM = 17f
        private val DEFAULT_LOCATION = LatLng(19.4326, -99.1332)
    }

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val enableGpsLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (isLocationEnabled()) startLocationFlow()
            else showToast("La ubicación sigue desactivada")
        } else {
            showToast("La ubicación no fue activada")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        initializeMap()
        setupLocationClient()
        setupLocationButton()
    }

    private fun initializeMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        configureMapSettings()
        setInitialMapLocation()
        setupMapInteractions()
        checkLocationPermission()
    }

    private fun configureMapSettings() {
        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
            isMyLocationButtonEnabled = true
        }
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
    }

    private fun setInitialMapLocation() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM))
    }

    private fun setupMapInteractions() {
        mMap.setOnMapClickListener { latLng ->
            addMarker(latLng, "Ubicación seleccionada", "Lat: ${latLng.latitude}, Lng: ${latLng.longitude}")
            animateCamera(latLng)
        }

        mMap.setOnMapLongClickListener { latLng ->
            addMarker(latLng, "Marcador largo", color = BitmapDescriptorFactory.HUE_BLUE)
        }

        mMap.setOnMarkerClickListener { marker ->
            showToast("Clic en: ${marker.title}")
            false
        }
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun setupLocationButton() {
        findViewById<FloatingActionButton>(R.id.btnMyLocation).apply {
            backgroundTintList = ContextCompat.getColorStateList(this@MapsActivity, R.color.verde)
            setOnClickListener { checkLocationPermissionAndZoom() }
        }
    }

    private fun checkLocationPermission() {
        if (hasLocationPermission()) enableLocationFeatures()
    }

    private fun checkLocationPermissionAndZoom() {
        if (hasLocationPermission()) {
            if (isLocationEnabled()) startLocationFlow()
            else promptEnableGPS()
        } else {
            requestLocationPermission()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun promptEnableGPS() {
        coroutineScope.launch {
            try {
                val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L).build()
                val builder = LocationSettingsRequest.Builder().addLocationRequest(request).setAlwaysShow(true)
                val client = LocationServices.getSettingsClient(this@MapsActivity)
                val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

                task.addOnSuccessListener { startLocationFlow() }
                task.addOnFailureListener { exception ->
                    if (exception is ResolvableApiException) {
                        try {
                            val intentRequest = IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                            enableGpsLauncher.launch(intentRequest)
                        } catch (e: Exception) {
                            showToast("Error al intentar activar la ubicación")
                        }
                    } else showToast("No se puede activar la ubicación automáticamente")
                }
            } catch (e: Exception) {
                Log.e("MapsActivity", "Error en promptEnableGPS: ${e.message}")
                showToast("Error al verificar configuración de ubicación")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationFeatures() {
        try {
            mMap.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            Log.e("MapsActivity", "Error al habilitar ubicación: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationFlow() {
        coroutineScope.launch {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        animateCamera(currentLatLng, LOCATION_ZOOM)
                        setupLocationUpdates()
                        startLocationUpdates()
                    } else showToast("No se pudo obtener la ubicación actual")
                }.addOnFailureListener { e ->
                    Log.e("MapsActivity", "Error al obtener ubicación", e)
                    showToast("Error al obtener ubicación: ${e.message ?: "Desconocido"}")
                }
            } catch (e: Exception) {
                Log.e("MapsActivity", "Error en startLocationFlow: ${e.message}")
                showToast("Error al iniciar flujo de ubicación")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupLocationUpdates() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(2000L).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.lastLocation?.let { loc ->
                    val latLng = LatLng(loc.latitude, loc.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, LOCATION_ZOOM))
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        } catch (e: SecurityException) {
            Log.e("MapsActivity", "Error de permisos en startLocationUpdates: ${e.message}")
            showToast("Error de permisos para actualizaciones de ubicación")
        } catch (e: Exception) {
            Log.e("MapsActivity", "Error en startLocationUpdates: ${e.message}")
            showToast("Error al iniciar actualizaciones de ubicación")
        }
    }

    private fun stopLocationUpdates() {
        try {
            if (::locationCallback.isInitialized) {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        } catch (e: Exception) {
            Log.e("MapsActivity", "Error al detener actualizaciones: ${e.message}")
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun addMarker(position: LatLng, title: String, description: String? = null, color: Float? = null) {
        try {
            MarkerOptions().apply {
                this.position(position)
                this.title(title)
                description?.let { snippet(it) }
                color?.let { icon(BitmapDescriptorFactory.defaultMarker(it)) }
            }.also { mMap.addMarker(it) }
        } catch (e: Exception) {
            Log.e("MapsActivity", "Error al agregar marcador: ${e.message}")
        }
    }

    private fun animateCamera(latLng: LatLng, zoom: Float? = null) {
        try {
            zoom?.let { mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, it)) }
                ?: mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        } catch (e: Exception) {
            Log.e("MapsActivity", "Error al animar cámara: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("MapsActivity", "Error mostrando toast: ${e.message}")
        }
    }

    private fun guardarUbicacionEnDB(latLng: LatLng, titulo: String) {
        Log.d("MapsActivity", "Ubicación guardada: $titulo - Lat: ${latLng.latitude}, Lng: ${latLng.longitude}")
    }

    private fun limpiarMarcadores() {
        try {
            mMap.clear()
        } catch (e: Exception) {
            Log.e("MapsActivity", "Error limpiando marcadores: ${e.message}")
        }
    }

    private fun obtenerUbicacionActualMapa(): LatLng {
        return mMap.cameraPosition.target
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                enableLocationFeatures()
            else showToast("Permiso de ubicación denegado")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }
}

