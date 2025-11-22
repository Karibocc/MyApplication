package com.example.myapplication.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.models.Producto

class ProductoAdapter(
    private val context: Context,
    private var productos: List<Producto>,
    private val onItemClick: (Producto) -> Unit,
    private val onEditClick: (Producto) -> Unit,
    private val onDeleteClick: (Producto) -> Unit,
    private val isAdmin: Boolean = false
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvDescripcion: TextView = itemView.findViewById(R.id.tvProductDescription)
        private val tvPrecio: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val ivImagen: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(producto: Producto) {
            tvNombre.text = producto.nombre
            tvDescripcion.text = producto.descripcion
            tvPrecio.text = "$${"%.2f".format(producto.precio)}"

            if (producto.imagen_path?.isNotEmpty() == true) {
                try {
                    Glide.with(context)
                        .load(producto.imagen_path)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .centerCrop()
                        .into(ivImagen)
                } catch (e: Exception) {
                    ivImagen.setImageResource(R.drawable.ic_image_placeholder)
                }
            } else {
                ivImagen.setImageResource(R.drawable.ic_image_placeholder)
            }

            if (isAdmin) {
                btnEdit.visibility = View.VISIBLE
                btnDelete.visibility = View.VISIBLE

                btnEdit.setOnClickListener {
                    onEditClick(producto)
                }
                btnDelete.setOnClickListener {
                    onDeleteClick(producto)
                }

                itemView.setOnClickListener {
                    onItemClick(producto)
                }
            } else {
                btnEdit.visibility = View.GONE
                btnDelete.visibility = View.GONE

                itemView.setOnClickListener {
                    onItemClick(producto)
                }
            }
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

    override fun getItemCount(): Int = productos.size

    fun updateProductos(newProductos: List<Producto>) {
        productos = newProductos
        notifyDataSetChanged()
    }

    fun eliminarProducto(productoId: Int) {
        val nuevaLista = productos.toMutableList()
        val index = nuevaLista.indexOfFirst { it.id == productoId }
        if (index != -1) {
            nuevaLista.removeAt(index)
            productos = nuevaLista
            notifyItemRemoved(index)
        }
    }
}