package com.example.myapplication.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.database.DatabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VerReportesActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    private lateinit var tvTotalUsuarios: TextView
    private lateinit var tvTotalProductos: TextView
    private lateinit var tvTotalCarrito: TextView
    private lateinit var btnActualizarReportes: Button

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    companion object {
        private const val TAG = "VerReportesActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_reportes)

        Log.d(TAG, "Iniciando VerReportesActivity")

        // Inicializar la base de datos
        dbHelper = DatabaseHelper(this)

        // Vincular elementos del layout
        tvTotalUsuarios = findViewById(R.id.tvTotalUsuarios)
        tvTotalProductos = findViewById(R.id.tvTotalProductos)
        tvTotalCarrito = findViewById(R.id.tvTotalCarrito)
        btnActualizarReportes = findViewById(R.id.btnActualizarReportes)

        // Verificar si recibimos datos del AdminActivity
        val totalUsuariosIntent = intent.getIntExtra("totalUsuarios", -1)
        val totalProductosIntent = intent.getIntExtra("totalProductos", -1)
        val totalCarritoIntent = intent.getDoubleExtra("totalCarrito", -1.0)

        if (totalUsuariosIntent != -1) {
            // Usar datos del intent
            tvTotalUsuarios.text = "Total de usuarios registrados: $totalUsuariosIntent"
            tvTotalProductos.text = "Total de productos registrados: $totalProductosIntent"
            tvTotalCarrito.text = "Total del carrito: $${"%.2f".format(totalCarritoIntent)}"
            Log.d(TAG, "Datos cargados desde intent")
        } else {
            // Cargar desde base de datos
            actualizarDatos()
        }

        // Botón para refrescar los reportes manualmente
        btnActualizarReportes.setOnClickListener {
            Log.d(TAG, "Botón Actualizar presionado")
            actualizarDatos()
        }
    }

    /**
     * Método para actualizar todos los totales en pantalla
     */
    private fun actualizarDatos() {
        btnActualizarReportes.isEnabled = false
        btnActualizarReportes.text = "Actualizando..."

        coroutineScope.launch {
            try {
                Log.d(TAG, "Iniciando actualización de datos...")

                val totalUsuarios = withContext(Dispatchers.IO) {
                    dbHelper.obtenerCantidadUsuarios()
                }

                val totalProductos = withContext(Dispatchers.IO) {
                    dbHelper.obtenerCantidadProductos()
                }

                val totalCarrito = withContext(Dispatchers.IO) {
                    dbHelper.calcularTotalCarrito()
                }

                Log.d(TAG, "Datos obtenidos - Usuarios: $totalUsuarios, Productos: $totalProductos, Carrito: $totalCarrito")

                // Actualizar la interfaz de usuario
                tvTotalUsuarios.text = "Total de usuarios registrados: $totalUsuarios"
                tvTotalProductos.text = "Total de productos registrados: $totalProductos"
                tvTotalCarrito.text = "Total del carrito: $${"%.2f".format(totalCarrito)}"

                Toast.makeText(this@VerReportesActivity, "Reportes actualizados", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Log.e(TAG, "ERROR actualizando datos: ${e.message}", e)

                // Manejo de errores
                tvTotalUsuarios.text = "Error cargando usuarios: ${e.message}"
                tvTotalProductos.text = "Error cargando productos"
                tvTotalCarrito.text = "Error calculando carrito"

                Toast.makeText(this@VerReportesActivity, "Error actualizando reportes", Toast.LENGTH_LONG).show()
            } finally {
                btnActualizarReportes.isEnabled = true
                btnActualizarReportes.text = "Actualizar Reportes"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume - Actualizando datos")
        // Actualizar datos cuando el usuario vuelve a esta actividad
        actualizarDatos()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy - Cerrando conexión")
        // Cerrar la conexión de la base de datos
        dbHelper.close()
    }
}