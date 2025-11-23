package com.example.myapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Personalizar el fragment para mostrar información de pedidos
        val tvTitulo = view.findViewById<TextView>(R.id.tvTituloHome)
        tvTitulo.text = "Mis Pedidos"

        val tvDescripcion = view.findViewById<TextView>(R.id.tvDescripcionHome)
        tvDescripcion.text = "Aquí podrás ver el historial de todos tus pedidos realizados"

        return view
    }
}