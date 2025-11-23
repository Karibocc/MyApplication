package com.example.myapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.helpers.LocationHelper

class LocationFragment : Fragment() {
    private lateinit var tvLocationStatus: TextView
    private lateinit var btnCurrentLocation: Button
    private lateinit var btnVerTiendas: Button
    private lateinit var locationHelper: LocationHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupLocation()
        setupClickListeners()
    }

    private fun initViews(view: View) {
        tvLocationStatus = view.findViewById(R.id.tvLocationStatus)
        btnCurrentLocation = view.findViewById(R.id.btnCurrentLocation)
        btnVerTiendas = view.findViewById(R.id.btnVerTiendas)
    }

    private fun setupLocation() {
        locationHelper = LocationHelper(requireContext())
        btnCurrentLocation.setOnClickListener {
            locationHelper.getCurrentLocation { location ->
                tvLocationStatus.text = "Ubicación: ${location.latitude}, ${location.longitude}"
            }
        }
    }

    private fun setupClickListeners() {
        btnVerTiendas.setOnClickListener {
            // Navegar a TiendasFragment
        }
    }
}