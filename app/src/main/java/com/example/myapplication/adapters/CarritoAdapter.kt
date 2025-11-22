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
        private val tvSubtotal: TextView = itemView.findViewById(R.id.tvSubtotal)

        fun bind(producto: Producto) {
            tvName.text = producto.nombre
            tvPrice.text = "$${"%.2f".format(producto.precio)}"

            // Usa el campo cantidad directamente (sin reflexión)
            val cantidadActual = if (producto.cantidad > 0) producto.cantidad else 1
            tvQuantity.text = cantidadActual.toString()

            // Calcular y mostrar subtotal
            val subtotal = producto.precio * cantidadActual
            tvSubtotal.text = "Subtotal: $${"%.2f".format(subtotal)}"

            Glide.with(itemView.context)
                .load(producto.imagen_path)
                .placeholder(R.drawable.ic_image_placeholder)
                .into(ivImage)

            // Botón eliminar producto
            btnDelete.setOnClickListener {
                onDeleteClick(producto)
            }

            // Botón restar cantidad
            btnMinus.setOnClickListener {
                val currentQuantity = tvQuantity.text.toString().toIntOrNull() ?: 1
                if (currentQuantity > 1) {
                    val newQuantity = currentQuantity - 1
                    tvQuantity.text = newQuantity.toString()
                    producto.cantidad = newQuantity // ✅ sincroniza el modelo
                    // Actualizar subtotal
                    val nuevoSubtotal = producto.precio * newQuantity
                    tvSubtotal.text = "Subtotal: $${"%.2f".format(nuevoSubtotal)}"
                    onQuantityChange(producto, newQuantity) // ✅ actualiza en BD y UI
                } else {
                    // Si llega a 0, el fragmento manejará la eliminación
                    onQuantityChange(producto, 0)
                }
            }

            // Botón aumentar cantidad
            btnPlus.setOnClickListener {
                val currentQuantity = tvQuantity.text.toString().toIntOrNull() ?: 1
                val newQuantity = currentQuantity + 1
                tvQuantity.text = newQuantity.toString()
                producto.cantidad = newQuantity // ✅ sincroniza el modelo
                // Actualizar subtotal
                val nuevoSubtotal = producto.precio * newQuantity
                tvSubtotal.text = "Subtotal: $${"%.2f".format(nuevoSubtotal)}"
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

    /**
     * Actualiza la lista completa de productos.
     */
    fun updateProductos(newProductos: List<Producto>) {
        productos.clear()
        productos.addAll(newProductos)
        notifyDataSetChanged()
    }

    /**
     * Método para obtener un producto por su posición
     */
    fun getProductoAt(position: Int): Producto? {
        return if (position in 0 until productos.size) {
            productos[position]
        } else {
            null
        }
    }

    /**
     * Método para eliminar un producto específico del carrito
     */
    fun eliminarProducto(productoId: Int) {
        val index = productos.indexOfFirst { it.id == productoId }
        if (index != -1) {
            productos.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    /**
     * Método para actualizar la cantidad de un producto específico
     */
    fun actualizarCantidad(productoId: Int, nuevaCantidad: Int) {
        val index = productos.indexOfFirst { it.id == productoId }
        if (index != -1) {
            productos[index].cantidad = nuevaCantidad
            notifyItemChanged(index)
        }
    }

    /**
     * Método para calcular el total del carrito
     */
    fun calcularTotalCarrito(): Double {
        return productos.sumOf { it.precio * it.cantidad }
    }

    /**
     * Método para obtener la cantidad total de productos en el carrito
     */
    fun obtenerCantidadTotalProductos(): Int {
        return productos.sumOf { it.cantidad }
    }

    /**
     * Método para verificar si el carrito está vacío
     */
    fun estaVacio(): Boolean {
        return productos.isEmpty()
    }

    /**
     * Método para limpiar todo el carrito
     */
    fun limpiarCarrito() {
        val itemCount = productos.size
        productos.clear()
        if (itemCount > 0) {
            notifyItemRangeRemoved(0, itemCount)
        }
    }
}

