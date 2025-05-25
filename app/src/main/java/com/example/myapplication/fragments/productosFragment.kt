package com.example.myapplication.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapters.productoAdapter
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.models.producto
import com.google.android.material.floatingactionbutton.FloatingActionButton

class productosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: productoAdapter
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var fabAdd: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_productos, container, false)

        // Inicializar vistas
        recyclerView = view.findViewById(R.id.recyclerViewProductos)
        fabAdd = view.findViewById(R.id.fabAddProducto)
        dbHelper = DatabaseHelper(requireContext())

        // Configurar RecyclerView
        setupRecyclerView()

        // Cargar productos
        loadProductos()

        // Configurar listeners
        setupListeners()

        return view
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = productoAdapter(requireContext(), mutableListOf(),
            onItemClick = { producto -> showProductDetails(producto) },
            onEditClick = { producto -> showEditProductDialog(producto) },
            onDeleteClick = { producto -> showDeleteConfirmation(producto) }
        )
        recyclerView.adapter = adapter
    }

    private fun loadProductos() {
        val productos = mutableListOf<producto>()
        val cursor = dbHelper.obtenerTodosLosProductos()

        cursor?.use {
            while (it.moveToNext()) {
                productos.add(producto(
                    id = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                    nombre = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOMBRE)),
                    descripcion = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPCION)),
                    precio = it.getDouble(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRECIO))
                ))
            }
        }

        adapter.updateProductos(productos)
    }

    private fun setupListeners() {
        fabAdd.setOnClickListener {
            showAddProductDialog()
        }
    }

    private fun showAddProductDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_producto, null)

        val etNombre = dialogView.findViewById<EditText>(R.id.tvProductName)
        val etDescripcion = dialogView.findViewById<EditText>(R.id.tvProductDescription)
        val etPrecio = dialogView.findViewById<EditText>(R.id.tvProductPrice)

        AlertDialog.Builder(requireContext())
            .setTitle("Agregar Producto")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = etNombre.text.toString()
                val descripcion = etDescripcion.text.toString()
                val precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0

                if (nombre.isNotEmpty() && descripcion.isNotEmpty()) {
                    dbHelper.insertarProducto(nombre, descripcion, precio)
                    loadProductos()
                    Toast.makeText(requireContext(), "Producto agregado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Complete todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .show()
    }

    private fun showEditProductDialog(producto: producto) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_producto, null)

        val etNombre = dialogView.findViewById<EditText>(R.id.tvProductName)
        val etDescripcion = dialogView.findViewById<EditText>(R.id.tvProductDescription)
        val etPrecio = dialogView.findViewById<EditText>(R.id.tvProductPrice)

        etNombre.setText(producto.nombre)
        etDescripcion.setText(producto.descripcion)
        etPrecio.setText(producto.precio.toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Producto")
            .setView(dialogView)
            .setPositiveButton("Actualizar") { _, _ ->
                val nombre = etNombre.text.toString()
                val descripcion = etDescripcion.text.toString()
                val precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0

                if (nombre.isNotEmpty() && descripcion.isNotEmpty()) {
                    dbHelper.actualizarProducto(producto.id, nombre, descripcion, precio)
                    loadProductos()
                    Toast.makeText(requireContext(), "Producto actualizado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Complete todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .show()
    }

    private fun showDeleteConfirmation(producto: producto) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Producto")
            .setMessage("¿Estás seguro de eliminar ${producto.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                dbHelper.eliminarProducto(producto.id)
                loadProductos()
                Toast.makeText(requireContext(), "Producto eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .show()
    }

    private fun showProductDetails(producto: producto) {
        AlertDialog.Builder(requireContext())
            .setTitle(producto.nombre)
            .setMessage("""
                Descripción: ${producto.descripcion}
                Precio: $${producto.precio}
            """.trimIndent())
            .setPositiveButton("Aceptar", null)
            .create()
            .show()
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}