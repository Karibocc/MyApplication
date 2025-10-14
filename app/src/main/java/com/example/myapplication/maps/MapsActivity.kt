package com.example.myapplication.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    // Región de Constantes
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val DEFAULT_ZOOM = 12f
        private const val LOCATION_ZOOM = 15f
        private val DEFAULT_LOCATION = LatLng(19.4326, -99.1332) // Ciudad de México
    }

    // Región de Variables
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Región de Ciclo de Vida
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        initializeMap()
        setupLocationClient()
        setupLocationButton()
    }

    // Región de Configuración Inicial
    private fun initializeMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun setupLocationButton() {
        findViewById<FloatingActionButton>(R.id.btnMyLocation).setOnClickListener {
            checkLocationPermissionAndZoom()
        }
    }

    // Región de Mapas
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
            addMarker(
                position = latLng,
                title = "Ubicación seleccionada",
                description = "Lat: ${latLng.latitude}, Lng: ${latLng.longitude}"
            )
            animateCamera(latLng)
        }

        mMap.setOnMapLongClickListener { latLng ->
            addMarker(
                position = latLng,
                title = "Marcador largo",
                color = BitmapDescriptorFactory.HUE_BLUE
            )
        }

        mMap.setOnMarkerClickListener { marker ->
            showToast("Clic en: ${marker.title}")
            false
        }
    }

    // Región de Ubicación
    private fun checkLocationPermission() {
        if (hasLocationPermission()) {
            enableLocationFeatures()
        }
    }

    private fun checkLocationPermissionAndZoom() {
        if (hasLocationPermission()) {
            getLastLocationAndZoom()
        } else {
            requestLocationPermission()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationFeatures() {
        mMap.isMyLocationEnabled = true
        getLastLocationAndZoom()
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocationAndZoom() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    animateCamera(currentLatLng, LOCATION_ZOOM)
                } else {
                    showToast("No se pudo obtener la ubicación")
                }
            }
            .addOnFailureListener { e ->
                showToast("Error al obtener ubicación: ${e.message ?: "Error desconocido"}")
            }
    }

    // Región de Utilidades
    private fun addMarker(position: LatLng, title: String, description: String? = null, color: Float? = null) {
        MarkerOptions().apply {
            this.position(position)
            this.title(title)
            description?.let { snippet(it) }
            color?.let { icon(BitmapDescriptorFactory.defaultMarker(it)) }
        }.also { mMap.addMarker(it) }
    }

    private fun animateCamera(latLng: LatLng, zoom: Float? = null) {
        zoom?.let {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, it))
        } ?: run {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Región de Resultados de Permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableLocationFeatures()
                } else {
                    showToast("Permiso de ubicación denegado")
                }
            }
        }
    }
}