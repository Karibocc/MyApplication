package com.example.myapplication.fragments

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapters.CarritoAdapter
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.models.Producto
import java.text.NumberFormat
import java.util.Locale

class CarritoFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CarritoAdapter
    private lateinit var db: DatabaseHelper
    private lateinit var tvTotal: TextView
    private lateinit var tvEmpty: TextView

    // Lista global para mantener los productos del carrito
    private var productosEnCarrito: MutableList<Producto> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_carrito, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewCarrito)
        tvTotal = view.findViewById(R.id.tvTotal)
        tvEmpty = view.findViewById(R.id.tvEmpty)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        db = DatabaseHelper(requireContext())

        cargarCarrito()

        return view
    }

    /**
     * Carga los productos del carrito desde la base de datos y configura el adapter.
     */
    private fun cargarCarrito() {
        val cursor: Cursor? = try {
            db.obtenerCarrito()
        } catch (e: Exception) {
            null
        }

        productosEnCarrito = if (cursor != null) {
            cursorToProductoList(cursor).toMutableList()
        } else {
            mutableListOf()
        }

        if (productosEnCarrito.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            tvTotal.text = formatPrice(0.0)
        } else {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            adapter = CarritoAdapter(
                productos = productosEnCarrito,
                onDeleteClick = { producto -> eliminarDelCarrito(producto) },
                onQuantityChange = { producto, nuevaCantidad ->
                    actualizarCantidad(producto, nuevaCantidad)
                }
            )

            recyclerView.adapter = adapter
            actualizarTotal()
        }
    }

    /**
     * Elimina un producto del carrito y actualiza la UI.
     */
    private fun eliminarDelCarrito(producto: Producto) {
        val filasAfectadas = db.eliminarDelCarrito(producto.id)
        if (filasAfectadas > 0) {
            Toast.makeText(requireContext(), "${producto.nombre} eliminado", Toast.LENGTH_SHORT).show()
            cargarCarrito()
        } else {
            Toast.makeText(requireContext(), "Error al eliminar ${producto.nombre}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Actualiza la cantidad en la BD y en la lista local (sin romper la lógica existente).
     */
    private fun actualizarCantidad(producto: Producto, nuevaCantidad: Int) {
        val stockDisponible = db.obtenerStockProducto(producto.id)

        if (nuevaCantidad <= stockDisponible && nuevaCantidad > 0) {
            val filas = db.actualizarCantidadEnCarrito(producto.id, nuevaCantidad)
            if (filas > 0) {
                val index = productosEnCarrito.indexOfFirst { it.id == producto.id }
                if (index >= 0) {
                    productosEnCarrito[index].cantidad = nuevaCantidad
                    adapter.notifyItemChanged(index)
                }
                actualizarTotal()
            }
        } else if (nuevaCantidad <= 0) {
            eliminarDelCarrito(producto)
        } else {
            Toast.makeText(requireContext(), "No hay suficiente stock disponible", Toast.LENGTH_SHORT).show()
            cargarCarrito()
        }
    }

    /**
     * Calcula y muestra el total del carrito.
     */
    private fun actualizarTotal() {
        val total = try {
            productosEnCarrito.sumOf { it.precio * it.cantidad }
        } catch (e: Exception) {
            0.0
        }
        tvTotal.text = formatPrice(total)
    }

    /**
     * Formatea un número a formato de moneda (ej. $1,234.56).
     */
    private fun formatPrice(price: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        return format.format(price)
    }

    /**
     * Convierte un Cursor (resultado de obtenerCarrito) en una lista de Producto.
     * Incluye imagen_path, stock y cantidad sincronizados.
     */
    private fun cursorToProductoList(cursor: Cursor): List<Producto> {
        val productos = mutableListOf<Producto>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOMBRE))
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPCION))
                val precio = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRECIO))
                val imagen = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGEN_PATH)) ?: ""
                val stock = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STOCK))
                val cantidad = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CANTIDAD))

                productos.add(
                    Producto(
                        id = id,
                        nombre = nombre,
                        descripcion = descripcion,
                        precio = precio,
                        imagen_path = imagen,
                        stock = stock,
                        cantidad = cantidad // ✅ sincroniza con BD
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return productos
    }

    override fun onResume() {
        super.onResume()
        cargarCarrito()
    }
}








