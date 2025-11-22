package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapters.ProductoAdapter
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.managers.SessionManager
import com.example.myapplication.models.Producto
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductoAdapter
    private lateinit var db: DatabaseHelper
    private lateinit var fabAgregar: FloatingActionButton
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var isAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productos)

        isAdmin = SessionManager.getCurrentUserRole(this) == "admin"

        setupUI()
        setupDatabase()
        setupRecyclerView()
        cargarProductos()
    }

    private fun setupUI() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (isAdmin) "Gestión de Productos" else "Catálogo de Productos"

        fabAgregar = findViewById(R.id.fabAgregar)
        fabAgregar.visibility = if (isAdmin) View.VISIBLE else View.GONE
        fabAgregar.setOnClickListener { navegarAAgregarProducto() }
    }

    private fun setupDatabase() {
        db = DatabaseHelper(this)
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewProductos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ProductoAdapter(
            context = this,
            productos = emptyList(),
            onItemClick = { producto ->
                if (isAdmin) {
                    editarProducto(producto)
                } else {
                    agregarAlCarrito(producto)
                }
            },
            onEditClick = { producto -> editarProducto(producto) },
            onDeleteClick = { producto -> mostrarDialogoEliminar(producto) },
            isAdmin = isAdmin
        )
        recyclerView.adapter = adapter
    }

    private fun cargarProductos() {
        coroutineScope.launch {
            try {
                val productos = withContext(Dispatchers.IO) {
                    val cursor = db.obtenerTodosLosProductos()
                    cursor.toProductoList()
                }
                adapter.updateProductos(productos)
            } catch (e: Exception) {
                Toast.makeText(this@ProductosActivity, "Error cargando productos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navegarAAgregarProducto() {
        val intent = Intent(this, GestionProductoActivity::class.java)
        startActivity(intent)
    }

    private fun editarProducto(producto: Producto) {
        val intent = Intent(this, GestionProductoActivity::class.java)
        intent.putExtra("MODO_EDICION", true)
        intent.putExtra("PRODUCTO_ID", producto.id)
        startActivity(intent)
    }

    private fun agregarAlCarrito(producto: Producto) {
        coroutineScope.launch {
            try {
                val resultado = withContext(Dispatchers.IO) {
                    db.agregarAlCarrito(producto.id, 1)
                }
                val mensaje = if (resultado > 0)
                    "${producto.nombre} agregado al carrito"
                else
                    "No hay stock disponible de ${producto.nombre}"
                Toast.makeText(this@ProductosActivity, mensaje, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@ProductosActivity, "Error agregando al carrito", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogoEliminar(producto: Producto) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Eliminar Producto")
            .setMessage("¿Estás seguro de eliminar ${producto.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarProducto(producto)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarProducto(producto: Producto) {
        coroutineScope.launch {
            try {
                val eliminado = withContext(Dispatchers.IO) {
                    db.eliminarProducto(producto.id) > 0
                }
                if (eliminado) {
                    adapter.eliminarProducto(producto.id)
                    Toast.makeText(this@ProductosActivity, "${producto.nombre} eliminado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProductosActivity, "Error eliminando producto", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProductosActivity, "Error eliminando producto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarProductos()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        db.close()
    }

    private fun android.database.Cursor.toProductoList(): List<Producto> {
        val productoList = mutableListOf<Producto>()
        try {
            if (moveToFirst()) {
                do {
                    try {
                        val producto = Producto(
                            id = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                            nombre = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOMBRE)),
                            descripcion = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPCION)),
                            precio = getDouble(getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRECIO)),
                            imagen_path = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGEN_PATH)) ?: "",
                            stock = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_STOCK))
                        )
                        productoList.add(producto)
                    } catch (e: Exception) {
                    }
                } while (moveToNext())
            }
        } catch (e: Exception) {
        } finally {
            close()
        }
        return productoList
    }
}