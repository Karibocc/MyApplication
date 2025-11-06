package com.example.myapplication.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

class ProductosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductoAdapter
    private lateinit var db: DatabaseHelper
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    companion object {
        private const val TAG = "ProductosActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productos)

        // Configurar toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Catálogo de Productos"

        recyclerView = findViewById(R.id.recyclerViewProductos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        db = DatabaseHelper(this)

        cargarProductos()
    }

    private fun cargarProductos() {
        coroutineScope.launch {
            try {
                val productos = withContext(Dispatchers.IO) {
                    val cursor = db.obtenerTodosLosProductos()
                    cursor.toProductoList()
                }

                adapter = ProductoAdapter(
                    context = this@ProductosActivity,
                    productos = productos,
                    onItemClick = { producto -> agregarAlCarrito(producto) },
                    onEditClick = { producto ->
                        // No permitir edición para clientes
                        Toast.makeText(this@ProductosActivity, "Solo administradores pueden editar", Toast.LENGTH_SHORT).show()
                    },
                    onDeleteClick = { producto ->
                        // No permitir eliminación para clientes
                        Toast.makeText(this@ProductosActivity, "Solo administradores pueden eliminar", Toast.LENGTH_SHORT).show()
                    }
                )

                recyclerView.adapter = adapter
                Log.d(TAG, "✅ ${productos.size} productos cargados")

            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR cargando productos: ${e.message}", e)
                Toast.makeText(this@ProductosActivity, "Error cargando productos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun agregarAlCarrito(producto: Producto) {
        coroutineScope.launch {
            try {
                val resultado = withContext(Dispatchers.IO) {
                    db.agregarAlCarrito(producto.id, 1)
                }

                if (resultado > 0) {
                    Toast.makeText(this@ProductosActivity, "${producto.nombre} agregado al carrito", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProductosActivity, "No hay stock disponible de ${producto.nombre}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProductosActivity, "Error agregando al carrito", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Extensión para convertir Cursor a List<Producto>
    private fun android.database.Cursor.toProductoList(): List<Producto> {
        return mutableListOf<Producto>().apply {
            if (moveToFirst()) {
                do {
                    add(Producto(
                        id = getInt(getColumnIndexOrThrow("id")),
                        nombre = getString(getColumnIndexOrThrow("nombre")),
                        descripcion = getString(getColumnIndexOrThrow("descripcion")),
                        precio = getDouble(getColumnIndexOrThrow("precio")),
                        imagen_path = getString(getColumnIndexOrThrow("imagen_path")),
                        stock = getInt(getColumnIndexOrThrow("stock"))
                    ))
                } while (moveToNext())
            }
            close()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        db.close()
    }
}