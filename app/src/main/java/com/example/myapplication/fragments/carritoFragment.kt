package com.example.myapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapters.productoAdapter
import com.example.myapplication.models.producto

class carritoFragment : Fragment() {

    private val carrito = mutableListOf<producto>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_carrito, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.rvCarrito)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = productoAdapter(
            context = requireContext(),
            productos = carrito,
            onDeleteClick = { /* TODO: lógica eliminar */ },
            onEditClick = { /* TODO: lógica editar */ },
            onItemClick = { /* TODO: lógica seleccionar */ }
        )
    }

    fun agregarProducto(producto: producto) {
        carrito.add(producto)
    }
}
