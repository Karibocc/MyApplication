package com.example.myapplication.fragments

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapters.ProductoAdapter
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.models.Producto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductoAdapter
    private lateinit var listaProductos: List<Producto>
    private lateinit var db: DatabaseHelper
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val vista = inflater.inflate(R.layout.fragment_productos, container, false)

        recyclerView = vista.findViewById(R.id.recyclerViewProductos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        db = DatabaseHelper(requireContext())
        cargarProductos()

        return vista
    }

    private fun cargarProductos() {
        coroutineScope.launch {
            try {
                val productos = withContext(Dispatchers.IO) {
                    val cursor = db.obtenerTodosLosProductos()
                    cursor.toProductoList()
                }

                listaProductos = productos

                adapter = ProductoAdapter(
                    context = requireContext(),
                    productos = listaProductos,
                    onItemClick = { producto -> agregarAlCarrito(requireContext(), producto) },
                    onEditClick = { producto -> mostrarDialogoEditarProducto(producto) },
                    onDeleteClick = { producto -> mostrarDialogoConfirmarEliminacion(producto) }
                )

                recyclerView.adapter = adapter

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error cargando productos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun agregarAlCarrito(context: Context, producto: Producto) {
        coroutineScope.launch {
            try {
                val resultado = withContext(Dispatchers.IO) {
                    db.agregarAlCarrito(producto.id, 1)
                }

                if (resultado > 0) {
                    Toast.makeText(context, "Producto agregado al carrito", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No hay stock disponible", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error agregando al carrito: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogoEditarProducto(producto: Producto) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Editar Producto")
            .setMessage("¿Desea editar este producto?")
            .setPositiveButton("Guardar") { dialog, _ ->
                actualizarProductoEnDB(producto)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun actualizarProductoEnDB(producto: Producto) {
        coroutineScope.launch {
            try {
                val filasAfectadas = withContext(Dispatchers.IO) {
                    db.actualizarProducto(
                        producto.id,
                        producto.nombre,
                        producto.descripcion,
                        producto.precio,
                        producto.imagen_path,
                        producto.stock
                    )
                }

                if (filasAfectadas > 0) {
                    Toast.makeText(requireContext(), "Producto actualizado", Toast.LENGTH_SHORT).show()
                    cargarProductos()
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error actualizando producto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogoConfirmarEliminacion(producto: Producto) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Confirmar eliminación")
            setMessage("¿Está seguro que desea eliminar ${producto.nombre}?")
            setPositiveButton("Eliminar") { _, _ ->
                eliminarProductoEnDB(producto)
            }
            setNegativeButton("Cancelar", null)
            create()
            show()
        }
    }

    private fun eliminarProductoEnDB(producto: Producto) {
        coroutineScope.launch {
            try {
                val filasAfectadas = withContext(Dispatchers.IO) {
                    db.eliminarProducto(producto.id)
                }

                if (filasAfectadas > 0) {
                    Toast.makeText(requireContext(), "Producto eliminado", Toast.LENGTH_SHORT).show()
                    // Actualizar lista sin recargar toda la base de datos
                    listaProductos = listaProductos.filter { it.id != producto.id }
                    adapter.updateProductos(listaProductos)
                } else {
                    Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error eliminando producto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 🔹 NUEVO: Método para filtrar productos
     */
    fun filtrarProductos(query: String) {
        coroutineScope.launch {
            try {
                val productosFiltrados = withContext(Dispatchers.IO) {
                    if (query.isBlank()) {
                        val cursor = db.obtenerTodosLosProductos()
                        cursor.toProductoList()
                    } else {
                        // Filtrar localmente (podrías implementar filtro en DB si lo prefieres)
                        listaProductos.filter {
                            it.nombre.contains(query, ignoreCase = true) ||
                                    it.descripcion.contains(query, ignoreCase = true)
                        }
                    }
                }
                adapter.updateProductos(productosFiltrados)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error filtrando productos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 🔹 NUEVO: Método para recargar productos
     */
    fun recargarProductos() {
        cargarProductos()
    }

    /**
     * 🔹 NUEVO: Método para obtener estadísticas de productos
     */
    fun obtenerEstadisticasProductos(): Triple<Int, Int, Double> {
        val totalProductos = listaProductos.size
        val productosConStock = listaProductos.count { it.stock > 0 }
        val valorTotalInventario = listaProductos.sumOf { it.precio * it.stock }

        return Triple(totalProductos, productosConStock, valorTotalInventario)
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
        // Recargar productos cuando el fragment se vuelve visible
        cargarProductos()
    }

    override fun onDestroy() {
        super.onDestroy()
        db.close()
    }
}