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

class ProductosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductoAdapter
    private lateinit var listaProductos: List<Producto>
    private lateinit var db: DatabaseHelper

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
        val cursor = db.obtenerTodosLosProductos()
        listaProductos = cursor.toProductoList()

        adapter = ProductoAdapter(
            context = requireContext(),
            productos = listaProductos,
            onItemClick = { producto -> agregarAlCarrito(requireContext(), producto) },
            onEditClick = { producto -> mostrarDialogoEditarProducto(producto) },
            onDeleteClick = { producto -> mostrarDialogoConfirmarEliminacion(producto) }
        )

        recyclerView.adapter = adapter
    }

    private fun agregarAlCarrito(context: Context, producto: Producto) {
        val resultado = db.agregarAlCarrito(producto.id, 1)

        if (resultado > 0) {
            Toast.makeText(context, "Producto agregado al carrito", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "No hay stock disponible", Toast.LENGTH_SHORT).show()
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
        val filasAfectadas = db.actualizarProducto(
            producto.id,
            producto.nombre,
            producto.descripcion,
            producto.precio,
            producto.imagen_path,
            producto.stock
        )

        if (filasAfectadas > 0) {
            Toast.makeText(requireContext(), "Producto actualizado", Toast.LENGTH_SHORT).show()
            cargarProductos()
        } else {
            Toast.makeText(requireContext(), "Error al actualizar", Toast.LENGTH_SHORT).show()
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
        try {
            val filasAfectadas = db.eliminarProducto(producto.id)

            if (filasAfectadas > 0) {
                Toast.makeText(requireContext(), "Producto eliminado", Toast.LENGTH_SHORT).show()
                // Actualizar lista sin recargar toda la base de datos
                listaProductos = listaProductos.filter { it.id != producto.id }
                adapter.updateProductos(listaProductos)
            } else {
                Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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

    override fun onDestroy() {
        db.close()
        super.onDestroy()
    }
}