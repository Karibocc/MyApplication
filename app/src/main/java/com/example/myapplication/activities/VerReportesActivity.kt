package com.example.myapplication.activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_reportes)

        // Inicializar la base de datos
        dbHelper = DatabaseHelper(this)

        // Vincular elementos del layout
        tvTotalUsuarios = findViewById(R.id.tvTotalUsuarios)
        tvTotalProductos = findViewById(R.id.tvTotalProductos)
        tvTotalCarrito = findViewById(R.id.tvTotalCarrito)
        btnActualizarReportes = findViewById(R.id.btnActualizarReportes)

        // Cargar los datos al iniciar la actividad
        actualizarDatos()

        // Botón para refrescar los reportes manualmente
        btnActualizarReportes.setOnClickListener {
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
                val totalUsuarios = withContext(Dispatchers.IO) {
                    dbHelper.obtenerCantidadUsuarios()
                }

                val totalProductos = withContext(Dispatchers.IO) {
                    dbHelper.obtenerCantidadProductos()
                }

                val totalCarrito = withContext(Dispatchers.IO) {
                    dbHelper.calcularTotalCarrito()
                }

                tvTotalUsuarios.text = "Total de usuarios registrados: $totalUsuarios"
                tvTotalProductos.text = "Total de productos registrados: $totalProductos"
                tvTotalCarrito.text = "Total del carrito: $${"%.2f".format(totalCarrito)}"

            } catch (e: Exception) {
                // Manejo de errores
                tvTotalUsuarios.text = "Error cargando usuarios"
                tvTotalProductos.text = "Error cargando productos"
                tvTotalCarrito.text = "Error calculando carrito"
            } finally {
                btnActualizarReportes.isEnabled = true
                btnActualizarReportes.text = "Actualizar Reportes"
            }
        }
    }

    /**
     * 🔹 NUEVO: Método para obtener estadísticas adicionales
     */
    private fun cargarEstadisticasAdicionales() {
        coroutineScope.launch {
            try {
                val cantidadProductosEnCarrito = withContext(Dispatchers.IO) {
                    dbHelper.obtenerCantidadProductosEnCarrito()
                }

                // Puedes agregar más TextView para mostrar estas estadísticas
                // Por ejemplo:
                // tvProductosEnCarrito.text = "Productos en carrito: $cantidadProductosEnCarrito"

            } catch (e: Exception) {
                // Manejar error silenciosamente o mostrar en log
            }
        }
    }

    /**
     * 🔹 NUEVO: Método para formatear números grandes
     */
    private fun formatearNumero(numero: Int): String {
        return when {
            numero >= 1_000_000 -> "${"%.1f".format(numero / 1_000_000.0)}M"
            numero >= 1_000 -> "${"%.1f".format(numero / 1_000.0)}K"
            else -> numero.toString()
        }
    }

    /**
     * 🔹 NUEVO: Método para actualizar datos cuando la actividad se reanuda
     */
    override fun onResume() {
        super.onResume()
        // Actualizar datos cuando el usuario vuelve a esta actividad
        actualizarDatos()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cerrar la conexión de la base de datos
        dbHelper.close()
    }
}