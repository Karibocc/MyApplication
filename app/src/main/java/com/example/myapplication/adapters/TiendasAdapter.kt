package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.Tienda

class TiendasAdapter(
    private var tiendas: List<Tienda>,
    private val onTiendaClick: (Tienda) -> Unit = {}
) : RecyclerView.Adapter<TiendasAdapter.TiendaViewHolder>() {

    inner class TiendaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombreTienda: TextView = itemView.findViewById(R.id.tvNombreTienda)
        private val tvDireccion: TextView = itemView.findViewById(R.id.tvDireccion)
        private val tvHorario: TextView = itemView.findViewById(R.id.tvHorario)
        private val tvTelefono: TextView = itemView.findViewById(R.id.tvTelefono)
        private val btnVerMapa: Button = itemView.findViewById(R.id.btnVerMapa)

        fun bind(tienda: Tienda) {
            tvNombreTienda.text = tienda.nombre
            tvDireccion.text = tienda.direccion
            tvHorario.text = tienda.horario
            tvTelefono.text = tienda.telefono

            btnVerMapa.setOnClickListener {
                onTiendaClick(tienda)
            }

            itemView.setOnClickListener {
                onTiendaClick(tienda)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TiendaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tienda, parent, false)
        return TiendaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TiendaViewHolder, position: Int) {
        holder.bind(tiendas[position])
    }

    override fun getItemCount(): Int = tiendas.size

    // ✅ AGREGAR ESTE MÉTODO
    fun updateTiendas(newTiendas: List<Tienda>) {
        this.tiendas = newTiendas
        notifyDataSetChanged()
    }
}