package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.models.Producto

class CarritoAdapter(
    private var productos: MutableList<Producto>,
    private val onDeleteClick: (Producto) -> Unit,
    private val onQuantityChange: (Producto, Int) -> Unit
) : RecyclerView.Adapter<CarritoAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        private val btnMinus: ImageButton = itemView.findViewById(R.id.btnMinus)
        private val btnPlus: ImageButton = itemView.findViewById(R.id.btnPlus)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(producto: Producto) {
            tvName.text = producto.nombre
            tvPrice.text = "$${"%.2f".format(producto.precio)}"

            // Si tu modelo no tiene campo cantidad, usamos una cantidad base 1
            val cantidadActual = producto.javaClass.getDeclaredFields()
                .find { it.name == "cantidad" }
                ?.let {
                    it.isAccessible = true
                    (it.get(producto) as? Int) ?: 1
                } ?: 1

            tvQuantity.text = cantidadActual.toString()

            Glide.with(itemView.context)
                .load(producto.imagen_path)
                .placeholder(R.drawable.ic_image_placeholder)
                .into(ivImage)

            // Botón eliminar producto
            btnDelete.setOnClickListener { onDeleteClick(producto) }

            // Botón restar cantidad
            btnMinus.setOnClickListener {
                val currentQuantity = tvQuantity.text.toString().toIntOrNull() ?: 1
                if (currentQuantity > 1) {
                    val newQuantity = currentQuantity - 1
                    tvQuantity.text = newQuantity.toString()
                    onQuantityChange(producto, newQuantity)
                }
            }

            // Botón aumentar cantidad
            btnPlus.setOnClickListener {
                val currentQuantity = tvQuantity.text.toString().toIntOrNull() ?: 1
                val newQuantity = currentQuantity + 1
                tvQuantity.text = newQuantity.toString()
                onQuantityChange(producto, newQuantity)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrito, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(productos[position])
    }

    override fun getItemCount(): Int = productos.size

    fun updateProductos(newProductos: List<Producto>) {
        productos.clear()
        productos.addAll(newProductos)
        notifyDataSetChanged()
    }
}
