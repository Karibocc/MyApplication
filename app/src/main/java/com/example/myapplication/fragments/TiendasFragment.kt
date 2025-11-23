package com.example.myapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapters.TiendasAdapter
import com.example.myapplication.databinding.FragmentTiendasBinding
import com.example.myapplication.models.Tienda

class TiendasFragment : Fragment() {

    private var _binding: FragmentTiendasBinding? = null
    private val binding get() = _binding!!
    private lateinit var tiendasAdapter: TiendasAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTiendasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        loadTiendas()
    }

    private fun setupRecyclerView() {
        tiendasAdapter = TiendasAdapter(emptyList()) { tienda ->
            onTiendaClicked(tienda)
        }

        binding.rvTiendas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tiendasAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.btnCurrentLocation.setOnClickListener {
            obtenerUbicacionActual()
        }

        binding.btnVerMapaGeneral.setOnClickListener {
            verTodasTiendasEnMapa()
        }
    }

    private fun loadTiendas() {
        val tiendasList = listOf(
            Tienda(
                id = "1",
                nombre = "Tienda Centro",
                direccion = "Av. Principal #123, Ciudad",
                horario = "Lunes a Viernes: 9:00 - 20:00\nSábados: 10:00 - 18:00",
                telefono = "(123) 456-7890",
                latitud = 19.4326,
                longitud = -99.1332
            ),
            Tienda(
                id = "2",
                nombre = "Tienda Norte",
                direccion = "Plaza Norte #456, Zona Norte",
                horario = "Lunes a Domingo: 8:00 - 22:00",
                telefono = "(123) 456-7891",
                latitud = 19.4902,
                longitud = -99.1197
            ),
            Tienda(
                id = "3",
                nombre = "Tienda Sur",
                direccion = "Centro Comercial Sur #789",
                horario = "Lunes a Sábado: 10:00 - 21:00\nDomingo: 11:00 - 17:00",
                telefono = "(123) 456-7892",
                latitud = 19.3556,
                longitud = -99.1614
            ),
            Tienda(
                id = "4",
                nombre = "Tienda Este",
                direccion = "Boulevard Este #321",
                horario = "Todos los días: 7:00 - 23:00",
                telefono = "(123) 456-7893",
                latitud = 19.4194,
                longitud = -99.0731
            )
        )

        tiendasAdapter.updateTiendas(tiendasList)
        binding.tvTotalTiendas.text = "Total: ${tiendasList.size} tiendas"
    }

    private fun onTiendaClicked(tienda: Tienda) {
        abrirTiendaEnMapa(tienda)
    }

    private fun obtenerUbicacionActual() {
        binding.tvLocationStatus.text = "Obteniendo tu ubicación..."
        binding.tvLocationStatus.text = "Ubicación: 19.4326, -99.1332"
        mostrarTiendasCercanas()
    }

    private fun mostrarTiendasCercanas() {
        binding.tvFiltroActual.text = "Mostrando tiendas cercanas a tu ubicación"
    }

    private fun verTodasTiendasEnMapa() {
        binding.tvLocationStatus.text = "Abriendo mapa con todas las tiendas..."
    }

    private fun abrirTiendaEnMapa(tienda: Tienda) {
        binding.tvLocationStatus.text = "Abriendo: ${tienda.nombre}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}