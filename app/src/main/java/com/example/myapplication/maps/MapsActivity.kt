package com.example.myapplication.maps

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import com.example.myapplication.R
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var tvUbicacionActual: TextView
    private lateinit var btnObtenerUbicacion: Button
    private lateinit var btnVerTiendas: Button

    private var currentLocation: Location? = null
    private var currentMarker: Marker? = null

    private val tiendas = listOf(
        Tienda("Tienda Central", LatLng(19.432608, -99.133209), "Av. Principal #123"),
        Tienda("Sucursal Norte", LatLng(19.442608, -99.143209), "Plaza Norte Local 45"),
        Tienda("Sucursal Sur", LatLng(19.422608, -99.123209), "Centro Comercial Sur")
    )

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

        initializeViews()
        setupClickListeners()
        setupLocationClient()
        setupMapFragment()
    }

    private fun initializeViews() {
        tvUbicacionActual = findViewById(R.id.tvUbicacionActual)
        btnObtenerUbicacion = findViewById(R.id.btnObtenerUbicacion)
        btnVerTiendas = findViewById(R.id.btnVerTiendas)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun setupMapFragment() {
        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.map_container, mapFragment)
        }
        mapFragment.getMapAsync(this)
    }

    private fun setupClickListeners() {
        btnObtenerUbicacion.setOnClickListener {
            obtenerUbicacionActual()
        }

        btnVerTiendas.setOnClickListener {
            mostrarTiendasEnMapa()
        }
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }

        mMap.setOnMarkerClickListener(this)

        val ubicacionDefault = LatLng(19.432608, -99.133209)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionDefault, 12f))

        mMap.addMarker(
            MarkerOptions()
                .position(ubicacionDefault)
                .title("Ubicación inicial")
        )

        solicitarPermisosUbicacion()
    }

    private fun solicitarPermisosUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacionActual()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        }
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacionActual() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (isLocationEnabled()) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        currentLocation = it
                        mostrarUbicacionEnMapa(it)
                        actualizarTextoUbicacion(it)
                    } ?: run {
                        showToast("No se pudo obtener la ubicación actual")
                        mostrarUbicacionPorDefecto()
                    }
                }
            } else {
                promptEnableGPS()
            }
        } else {
            solicitarPermisosUbicacion()
        }
    }

    private fun mostrarUbicacionEnMapa(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)

        currentMarker?.remove()

        currentMarker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Tu ubicación actual")
                .snippet("Lat: ${location.latitude}, Lng: ${location.longitude}")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

        showToast("Ubicación actual obtenida")
    }

    private fun mostrarUbicacionPorDefecto() {
        val ciudadMexico = LatLng(19.432608, -99.133209)
        mMap.addMarker(
            MarkerOptions()
                .position(ciudadMexico)
                .title("Ciudad de México")
                .snippet("Ubicación por defecto")
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ciudadMexico, 10f))
    }

    private fun actualizarTextoUbicacion(location: Location) {
        val texto = "Lat: ${"%.6f".format(location.latitude)}\nLng: ${"%.6f".format(location.longitude)}"
        tvUbicacionActual.text = texto
    }

    private fun mostrarTiendasEnMapa() {
        mMap.clear()

        currentLocation?.let {
            mostrarUbicacionEnMapa(it)
        }

        tiendas.forEach { tienda ->
            mMap.addMarker(
                MarkerOptions()
                    .position(tienda.ubicacion)
                    .title(tienda.nombre)
                    .snippet(tienda.direccion)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
        }

        showToast("${tiendas.size} tiendas mostradas en el mapa")
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        marker.showInfoWindow()
        tiendas.find { it.ubicacion == marker.position }?.let { tienda ->
            showToast("Tienda: ${tienda.nombre}")
        }
        return true
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun promptEnableGPS() {
        CoroutineScope(Dispatchers.Main).launch {
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
                showToast("Error al verificar configuración de ubicación")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationFlow() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        currentLocation = location
                        mostrarUbicacionEnMapa(location)
                        actualizarTextoUbicacion(location)
                    } else showToast("No se pudo obtener la ubicación actual")
                }
            } catch (e: Exception) {
                showToast("Error al iniciar flujo de ubicación")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true
                }
                obtenerUbicacionActual()
            } else {
                showToast("Permiso de ubicación denegado")
            }
        }
    }

    data class Tienda(
        val nombre: String,
        val ubicacion: LatLng,
        val direccion: String
    )
}