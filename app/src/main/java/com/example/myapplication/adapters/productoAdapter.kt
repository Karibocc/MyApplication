package com.example.myapplication.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.producto

class productoAdapter(
    private val context: Context,
    private var productos: List<producto>,
    private val onItemClick: (producto) -> Unit,
    private val onEditClick: (producto) -> Unit,
    private val onDeleteClick: (producto) -> Unit
) : RecyclerView.Adapter<productoAdapter.ProductoViewHolder>() {

    inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvDescripcion: TextView = itemView.findViewById(R.id.tvProductDescription)
        private val tvPrecio: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(producto: producto) {
            tvNombre.text = producto.nombre
            tvDescripcion.text = producto.descripcion
            tvPrecio.text = "$${"%.2f".format(producto.precio)}"

            itemView.setOnClickListener { onItemClick(producto) }
            btnEdit.setOnClickListener { onEditClick(producto) }
            btnDelete.setOnClickListener { onDeleteClick(producto) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(productos[position])
    }

    override fun getItemCount() = productos.size

    fun updateProductos(newProductos: List<producto>) {
        productos = newProductos
        notifyDataSetChanged()
    }
}