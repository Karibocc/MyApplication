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

    private fun cargarCarrito() {
        val productosEnCarrito = db.obtenerCarrito().toProductoList()

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

    private fun eliminarDelCarrito(producto: Producto) {
        val filasAfectadas = db.eliminarDelCarrito(producto.id)
        if (filasAfectadas > 0) {
            Toast.makeText(requireContext(), "${producto.nombre} eliminado", Toast.LENGTH_SHORT).show()
            cargarCarrito()
        }
    }

    private fun actualizarCantidad(producto: Producto, nuevaCantidad: Int) {
        val stockDisponible = db.obtenerStockProducto(producto.id)

        if (nuevaCantidad <= stockDisponible) {
            db.actualizarCantidadEnCarrito(producto.id, nuevaCantidad)
            actualizarTotal()
        } else {
            Toast.makeText(requireContext(), "No hay suficiente stock", Toast.LENGTH_SHORT).show()
            cargarCarrito() // Recargar para mostrar cantidad actual
        }
    }

    private fun actualizarTotal() {
        val total = db.calcularTotalCarrito()
        tvTotal.text = formatPrice(total)
    }

    private fun formatPrice(price: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        return format.format(price)
    }

    private fun Cursor.toProductoList(): List<Producto> {
        return mutableListOf<Producto>().apply {
            if (moveToFirst()) {
                do {
                    add(Producto(
                        id = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                        nombre = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOMBRE)),
                        descripcion = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPCION)),
                        precio = getDouble(getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRECIO)),
                        imagen_path = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGEN_PATH)),
                        stock = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_STOCK))
                    ))
                } while (moveToNext())
            }
            close()
        }
    }

    override fun onResume() {
        super.onResume()
        cargarCarrito()
    }
}