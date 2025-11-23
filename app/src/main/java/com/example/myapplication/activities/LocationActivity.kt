package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.myapplication.R
import com.example.myapplication.helpers.LocationHelper
import com.example.myapplication.maps.MapsActivity

class LocationActivity : AppCompatActivity() {

    private lateinit var tvLocationStatus: TextView
    private lateinit var loadingSection: androidx.constraintlayout.widget.ConstraintLayout
    private lateinit var btnVerTiendas: Button
    private lateinit var cardCurrentLocation: CardView
    private lateinit var cardStores: CardView
    private lateinit var btnOpenMap: Button

    private lateinit var locationHelper: LocationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        initViews()
        setupClickListeners()
        initializeLocation()
    }

    private fun initViews() {
        tvLocationStatus = findViewById(R.id.tvLocationStatus)
        loadingSection = findViewById(R.id.loadingSection)
        btnVerTiendas = findViewById(R.id.btnVerTiendas)
        cardCurrentLocation = findViewById(R.id.cardCurrentLocation)
        cardStores = findViewById(R.id.cardStores)
        btnOpenMap = findViewById(R.id.btnOpenMap)
    }

    private fun setupClickListeners() {
        btnVerTiendas.setOnClickListener {
            val intent = Intent(this, TiendasActivity::class.java)
            startActivity(intent)
        }

        cardCurrentLocation.setOnClickListener {
            getCurrentLocation()
        }

        cardStores.setOnClickListener {
            val intent = Intent(this, TiendasActivity::class.java)
            startActivity(intent)
        }

        btnOpenMap.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initializeLocation() {
        locationHelper = LocationHelper(this)
        simulateLocationLoading()
    }

    private fun simulateLocationLoading() {
        Handler(Looper.getMainLooper()).postDelayed({
            loadingSection.visibility = android.view.View.GONE
            tvLocationStatus.text = "Ubicación lista - Selecciona una opción"
        }, 2000)
    }

    private fun getCurrentLocation() {
        tvLocationStatus.text = "Obteniendo tu ubicación actual..."

        locationHelper.getCurrentLocation { location ->
            runOnUiThread {
                tvLocationStatus.text =
                    "Ubicación: ${String.format("%.4f", location.latitude)}, " +
                            "${String.format("%.4f", location.longitude)}"
                showNearbyStores(location.latitude, location.longitude)
            }
        }
    }

    private fun showNearbyStores(latitude: Double, longitude: Double) {
        val intent = Intent(this, TiendasActivity::class.java).apply {
            putExtra("filter_nearby", true)
            putExtra("user_lat", latitude)
            putExtra("user_lng", longitude)
        }
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}