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

/**
 * Actividad encargada de mostrar un mapa con soporte de geolocalización,
 * utilizando la API de Google Maps y el proveedor de ubicación de Google (FusedLocationProviderClient).
 */
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val DEFAULT_ZOOM = 12f
        private const val LOCATION_ZOOM = 17f
        private val DEFAULT_LOCATION = LatLng(19.4326, -99.1332) // CDMX como ubicación predeterminada
    }

    // ----------------------- VARIABLES PRINCIPALES -----------------------
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    // Launcher moderno para activar el GPS sin usar startActivityForResult (deprecated)
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

    // ----------------------- CICLO DE VIDA -----------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        initializeMap()         // Inicializa el fragmento del mapa
        setupLocationClient()   // Inicializa el proveedor de ubicación (SOLUCIÓN SUGERIDA)
        setupLocationButton()   // Configura el botón flotante para centrar la ubicación
    }

    // ----------------------- CONFIGURACIÓN DEL MAPA -----------------------
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
        // Establece una ubicación base si el GPS no está disponible
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM))
    }

    private fun setupMapInteractions() {
        // Clic corto: añade marcador con coordenadas
        mMap.setOnMapClickListener { latLng ->
            addMarker(latLng, "Ubicación seleccionada", "Lat: ${latLng.latitude}, Lng: ${latLng.longitude}")
            animateCamera(latLng)
        }

        // Clic largo: marcador azul
        mMap.setOnMapLongClickListener { latLng ->
            addMarker(latLng, "Marcador largo", color = BitmapDescriptorFactory.HUE_BLUE)
        }

        // Clic en marcador: muestra Toast
        mMap.setOnMarkerClickListener { marker ->
            showToast("Clic en: ${marker.title}")
            false
        }
    }

    // ----------------------- CLIENTE DE UBICACIÓN -----------------------
    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    // ----------------------- BOTÓN DE UBICACIÓN -----------------------
    private fun setupLocationButton() {
        findViewById<FloatingActionButton>(R.id.btnMyLocation).apply {
            backgroundTintList = ContextCompat.getColorStateList(this@MapsActivity, R.color.verde)
            setOnClickListener { checkLocationPermissionAndZoom() }
        }
    }

    // ----------------------- PERMISOS Y GPS -----------------------
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

    // Solicita al usuario activar el GPS si está deshabilitado
    private fun promptEnableGPS() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(request).setAlwaysShow(true)
        val client = LocationServices.getSettingsClient(this)
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
    }

    // ----------------------- FUNCIONES DE LOCALIZACIÓN -----------------------
    @SuppressLint("MissingPermission")
    private fun enableLocationFeatures() {
        try {
            mMap.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            Log.e("MapsActivity", "Error al habilitar ubicación: ${e.message}")
        }
    }

    // Obtiene la última ubicación conocida y activa actualizaciones periódicas
    @SuppressLint("MissingPermission")
    private fun startLocationFlow() {
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
    }

    // Configura los parámetros de frecuencia e intervalo de actualizaciones
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
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    // ----------------------- UTILIDADES -----------------------
    private fun addMarker(position: LatLng, title: String, description: String? = null, color: Float? = null) {
        MarkerOptions().apply {
            this.position(position)
            this.title(title)
            description?.let { snippet(it) }
            color?.let { icon(BitmapDescriptorFactory.defaultMarker(it)) }
        }.also { mMap.addMarker(it) }
    }

    private fun animateCamera(latLng: LatLng, zoom: Float? = null) {
        zoom?.let { mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, it)) }
            ?: mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Manejo del resultado de la solicitud de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                enableLocationFeatures()
            else showToast("Permiso de ubicación denegado")
        }
    }
}


