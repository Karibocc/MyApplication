package com.example.myapplication.fragments

import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapters.ProductoAdapter
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.managers.SessionManager
import com.example.myapplication.models.Producto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductoAdapter
    private var listaProductos: MutableList<Producto> = mutableListOf()
    private lateinit var db: DatabaseHelper
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var esAdministrador: Boolean = false

    companion object {
        private const val TAG = "ProductosFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val vista = inflater.inflate(R.layout.fragment_productos, container, false)

        recyclerView = vista.findViewById(R.id.recyclerViewProductos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        db = DatabaseHelper(requireContext())

        verificarRolUsuario()
        cargarProductos()

        return vista
    }

    private fun verificarRolUsuario() {
        try {
            val userRole = SessionManager.getUserRole(requireContext())
            esAdministrador = userRole?.equals("admin", ignoreCase = true) ?: false
            Log.d(TAG, "Rol del usuario: $userRole, Es admin: $esAdministrador")
        } catch (e: Exception) {
            esAdministrador = false
            Log.e(TAG, "Error verificando rol", e)
        }
    }

    private fun cargarProductos() {
        coroutineScope.launch {
            try {
                Log.d(TAG, "Iniciando carga de productos...")

                val productos = withContext(Dispatchers.IO) {
                    val cursor = db.obtenerTodosLosProductos()
                    val lista = cursor.toProductoList()
                    cursor.close()
                    lista
                }

                Log.d(TAG, "Productos cargados: ${productos.size}")

                listaProductos.clear()
                listaProductos.addAll(productos)

                configurarAdapter()

                if (productos.isEmpty()) {
                    Toast.makeText(requireContext(), "No hay productos disponibles", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e(TAG, "ERROR cargando productos: ${e.message}", e)
                Toast.makeText(requireContext(), "Error cargando productos: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun configurarAdapter() {
        adapter = ProductoAdapter(
            context = requireContext(),
            productos = listaProductos,
            onItemClick = { producto ->
                if (esAdministrador) {
                    mostrarDetallesProducto(producto)
                } else {
                    agregarAlCarrito(producto)
                }
            },
            onEditClick = { producto ->
                if (esAdministrador) {
                    mostrarDialogoEditarProducto(producto)
                }
            },
            onDeleteClick = { producto ->
                if (esAdministrador) {
                    mostrarDialogoConfirmarEliminacion(producto)
                }
            },
            isAdmin = esAdministrador
        )

        recyclerView.adapter = adapter
        Log.d(TAG, "Adapter configurado con ${listaProductos.size} productos")
    }

    private fun agregarAlCarrito(producto: Producto) {
        coroutineScope.launch {
            try {
                Log.d(TAG, "Intentando agregar al carrito: ${producto.nombre}")

                if (producto.stock > 0) {
                    val resultado = withContext(Dispatchers.IO) {
                        db.agregarAlCarrito(producto.id, 1)
                    }

                    if (resultado > 0) {
                        Toast.makeText(requireContext(), "${producto.nombre} agregado al carrito", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Producto agregado al carrito exitosamente")
                    } else {
                        Toast.makeText(requireContext(), "Error al agregar al carrito", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Error al agregar producto al carrito")
                    }
                } else {
                    Toast.makeText(requireContext(), "Producto sin stock disponible", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "ERROR agregando al carrito: ${e.message}", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDetallesProducto(producto: Producto) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Detalles del Producto")
            .setMessage(
                "Nombre: ${producto.nombre}\n" +
                        "Descripción: ${producto.descripcion}\n" +
                        "Precio: $${"%.2f".format(producto.precio)}\n" +
                        "Stock: ${producto.stock}\n" +
                        "Categoría: ${producto.categoria}"
            )
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun mostrarDialogoEditarProducto(producto: Producto) {
        Toast.makeText(requireContext(), "Funcionalidad de edición en desarrollo", Toast.LENGTH_SHORT).show()
    }

    private fun mostrarDialogoConfirmarEliminacion(producto: Producto) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext()).apply {
            setTitle("Confirmar eliminación")
            setMessage("¿Está seguro que desea eliminar \"${producto.nombre}\"?")
            setPositiveButton("Eliminar") { _, _ ->
                eliminarProducto(producto)
            }
            setNegativeButton("Cancelar", null)
            create()
            show()
        }
    }

    private fun eliminarProducto(producto: Producto) {
        coroutineScope.launch {
            try {
                val filasAfectadas = withContext(Dispatchers.IO) {
                    db.eliminarProducto(producto.id)
                }

                if (filasAfectadas > 0) {
                    Toast.makeText(requireContext(), "Producto eliminado", Toast.LENGTH_SHORT).show()
                    listaProductos.removeAll { it.id == producto.id }
                    adapter.updateProductos(listaProductos.toList())
                    Log.d(TAG, "Producto eliminado: ${producto.nombre}")
                } else {
                    Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "ERROR eliminando producto: ${e.message}", e)
                Toast.makeText(requireContext(), "Error eliminando producto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun Cursor.toProductoList(): List<Producto> {
        val productos = mutableListOf<Producto>()
        try {
            if (moveToFirst()) {
                do {
                    try {
                        val id = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                        val nombre = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOMBRE)) ?: "Sin nombre"
                        val descripcion = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPCION)) ?: "Sin descripción"
                        val precio = getDouble(getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRECIO))
                        val imagenPath = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGEN_PATH)) ?: ""
                        val stock = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_STOCK))
                        val cantidad = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_CANTIDAD))
                        val categoria = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORIA)) ?: "General"

                        productos.add(Producto(
                            id = id,
                            nombre = nombre,
                            descripcion = descripcion,
                            precio = precio,
                            imagen_path = imagenPath,
                            stock = stock,
                            cantidad = cantidad,
                            categoria = categoria
                        ))

                        Log.d(TAG, "Producto cargado: $nombre (ID: $id)")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error procesando fila del cursor", e)
                    }
                } while (moveToNext())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en toProductoList", e)
        }
        return productos
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume - Recargando productos")
        cargarProductos()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            db.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error cerrando DB", e)
        }
    }
}