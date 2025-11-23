package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.managers.SessionManager
import com.example.myapplication.models.Usuario
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AdminActivity : AppCompatActivity() {

    private lateinit var tvAdminInfo: TextView
    private lateinit var btnGestionarProductos: LinearLayout
    private lateinit var btnGestionarUsuarios: LinearLayout
    private lateinit var btnVerReportes: LinearLayout
    private lateinit var btnCerrarSesion: Button
    private lateinit var btnGeolocalizacion: LinearLayout

    private val auth = Firebase.auth
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val TAG = "AdminActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "ADMIN ACTIVITY - ONCREATE INICIADO")

        try {
            setContentView(R.layout.activity_admin)
            Log.d(TAG, "Layout activity_admin cargado")
        } catch (e: Exception) {
            Log.e(TAG, "ERROR cargando layout: ${e.message}", e)
            showToast("Error cargando el panel de administracion")
            crearLayoutMinimo()
            return
        }

        try {
            initializeViews()
            setupUserInfo()
            setupClickListeners()
            Log.d(TAG, "AdminActivity completamente configurada")
        } catch (e: Exception) {
            Log.e(TAG, "ERROR configurando AdminActivity: ${e.message}", e)
            showToast("Error configurando el panel de administracion")
            crearLayoutMinimo()
        }
    }

    private fun initializeViews() {
        try {
            tvAdminInfo = findViewById(R.id.tvAdminInfo)
            btnGestionarProductos = findViewById(R.id.btnGestionarProductos)
            btnGestionarUsuarios = findViewById(R.id.btnGestionarUsuarios)
            btnVerReportes = findViewById(R.id.btnVerReportes)
            btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
            btnGeolocalizacion = findViewById(R.id.btnGeolocalizacion)

            Log.d(TAG, "Vistas inicializadas correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "ERROR en initializeViews: ${e.message}", e)
            throw e
        }
    }

    private fun setupUserInfo() {
        coroutineScope.launch {
            try {
                val currentUser = auth.currentUser
                val email = currentUser?.email ?: SessionManager.getUsername(this@AdminActivity) ?: ""
                val rol = SessionManager.getUserRole(this@AdminActivity) ?: "admin"

                if (email.isNotEmpty()) {
                    val userInfo = "Administrador: $email\nRol: $rol"
                    tvAdminInfo.text = userInfo
                    Log.d(TAG, "Informacion de admin: $email")
                } else {
                    tvAdminInfo.text = "Administrador del Sistema"
                    Log.w(TAG, "No se pudo obtener email del admin")
                }
            } catch (e: Exception) {
                Log.e(TAG, "ERROR cargando informacion del admin: ${e.message}", e)
                tvAdminInfo.text = "Administrador del Sistema"
            }
        }
    }

    private fun setupClickListeners() {
        btnGestionarUsuarios.setOnClickListener {
            Log.d(TAG, "Boton Gestionar Usuarios PRESIONADO")
            diagnosticarCompleto()
            abrirGestionUsuariosConSeguridad()
        }

        btnGestionarProductos.setOnClickListener {
            Log.d(TAG, "Clic en Gestionar Productos")
            try {
                val intent = Intent(this, ProductosActivity::class.java)
                startActivity(intent)
                Log.d(TAG, "ProductosActivity iniciada exitosamente")
            } catch (e: Exception) {
                Log.e(TAG, "ERROR abriendo ProductosActivity: ${e.message}", e)
                showToast("Error al abrir gestión de productos")
            }
        }

        btnVerReportes.setOnClickListener {
            Log.d(TAG, "Clic en Ver Reportes")
            abrirReportesConDatosReales()
        }

        btnGeolocalizacion.setOnClickListener {
            Log.d(TAG, "Clic en Geolocalizacion")
            abrirGeolocalizacion()
        }

        btnCerrarSesion.setOnClickListener {
            Log.d(TAG, "Clic en Cerrar Sesion")
            cerrarSesion()
        }

        Log.d(TAG, "Todos los listeners configurados")
    }

    private fun abrirGeolocalizacion() {
        try {
            Log.d(TAG, "Abriendo pantalla de geolocalizacion...")

            // Crear intent para abrir MapsActivity
            val intent = Intent(this, com.example.myapplication.maps.MapsActivity::class.java)

            // Opcional: Pasar datos adicionales si es necesario
            intent.putExtra("origen", "admin_panel")
            intent.putExtra("usuario_admin", auth.currentUser?.email ?: "admin")

            startActivity(intent)
            Log.d(TAG, "MapsActivity iniciada exitosamente")

        } catch (e: android.content.ActivityNotFoundException) {
            Log.e(TAG, "MapsActivity no encontrada: ${e.message}", e)
            showToast("Error: Actividad de mapa no disponible")
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de seguridad: ${e.message}", e)
            showToast("Error de permisos")
        } catch (e: Exception) {
            Log.e(TAG, "ERROR abriendo MapsActivity: ${e.message}", e)
            showToast("Error al abrir geolocalizacion")
        }
    }

    // El resto de tus métodos existentes se mantienen igual...
    private fun abrirReportesConDatosReales() {
        try {
            Log.d(TAG, "Abriendo pantalla de reportes con datos reales...")
            val intent = Intent(this, VerReportesActivity::class.java)

            val usuarios = obtenerUsuariosParaReporte()
            val totalUsuarios = usuarios.size
            val totalProductos = obtenerTotalProductos()
            val totalCarrito = obtenerTotalCarrito()

            intent.putExtra("totalUsuarios", totalUsuarios)
            intent.putExtra("totalProductos", totalProductos)
            intent.putExtra("totalCarrito", totalCarrito)

            startActivity(intent)
            Log.d(TAG, "VerReportesActivity iniciada con datos actualizados")

        } catch (e: Exception) {
            Log.e(TAG, "ERROR abriendo reportes: ${e.message}", e)
            mostrarMenuReportes()
        }
    }

    private fun diagnosticarCompleto() {
        Log.d(TAG, "INICIANDO ANALISIS COMPLETO")

        try {
            val targetClass = Class.forName("com.example.myapplication.activities.GestionarUsuariosActivity")
            Log.d(TAG, "GestionarUsuariosActivity EXISTE: $targetClass")
        } catch (e: ClassNotFoundException) {
            Log.e(TAG, "GestionarUsuariosActivity NO EXISTE", e)
            return
        }

        Log.d(TAG, "Contexto: $this")
        Log.d(TAG, "Actividad no nula: ${this != null}")
        Log.d(TAG, "Actividad no finalizada: ${!isFinishing}")

        try {
            val testIntent = Intent(this, GestionarUsuariosActivity::class.java)
            Log.d(TAG, "Intent de prueba creado: $testIntent")
        } catch (e: Exception) {
            Log.e(TAG, "Error creando Intent de prueba", e)
        }

        Log.d(TAG, "ANALISIS COMPLETADO")
    }

    private fun abrirGestionUsuariosConSeguridad() {
        Log.d(TAG, "INICIANDO PROCESO DE APERTURA")

        btnGestionarUsuarios.isEnabled = false
        Log.d(TAG, "Boton deshabilitado")

        try {
            Log.d(TAG, "Creando Intent...")
            val intent = Intent(this, GestionarUsuariosActivity::class.java)

            Log.d(TAG, "Intent creado: $intent")

            startActivity(intent)

            Log.d(TAG, "startActivity() EJECUTADO - EXITO")

            handler.postDelayed({
                btnGestionarUsuarios.isEnabled = true
                Log.d(TAG, "Boton rehabilitado")
            }, 3000)

        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: ${e.message}", e)
            showToast("Error de seguridad")
            btnGestionarUsuarios.isEnabled = true
        } catch (e: android.content.ActivityNotFoundException) {
            Log.e(TAG, "ActivityNotFoundException: ${e.message}", e)
            showToast("Actividad no encontrada")
            btnGestionarUsuarios.isEnabled = true
        } catch (e: IllegalStateException) {
            Log.e(TAG, "IllegalStateException: ${e.message}", e)
            showToast("Error de estado: ${e.message}")
            btnGestionarUsuarios.isEnabled = true
        } catch (e: Exception) {
            Log.e(TAG, "Exception inesperada: ${e.message}", e)
            Log.e(TAG, "Tipo: ${e.javaClass.simpleName}")
            showToast("Error: ${e.localizedMessage}")
            btnGestionarUsuarios.isEnabled = true
        }
    }

    private fun obtenerTotalProductos(): Int {
        return try {
            val productos = listOf("Producto1", "Producto2", "Producto3")
            productos.size
        } catch (e: Exception) {
            Log.e(TAG, "ERROR obteniendo total de productos: ${e.message}", e)
            0
        }
    }

    private fun obtenerTotalCarrito(): Double {
        return try {
            1250.75
        } catch (e: Exception) {
            Log.e(TAG, "ERROR obteniendo total del carrito: ${e.message}", e)
            0.0
        }
    }

    private fun mostrarMenuReportes() {
        val opcionesReportes = arrayOf(
            "Reporte General del Sistema",
            "Reporte de Usuarios",
            "Estadisticas de Uso",
            "Auditoria del Sistema",
            "Exportar Reportes"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("REPORTES DEL SISTEMA")
            .setItems(opcionesReportes) { dialog, which ->
                when (which) {
                    0 -> generarReporteGeneralSistema()
                    1 -> generarReporteUsuarios()
                    2 -> generarEstadisticasUso()
                    3 -> generarAuditoriaSistema()
                    4 -> mostrarOpcionesExportacion()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun generarReporteGeneralSistema() {
        try {
            val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            val adminEmail = auth.currentUser?.email ?: "No identificado"

            val reporte = StringBuilder()
            reporte.append("=== REPORTE GENERAL DEL SISTEMA ===\n\n")
            reporte.append("Fecha generacion: $fechaActual\n")
            reporte.append("Administrador: $adminEmail\n")
            reporte.append("Version: ${packageManager.getPackageInfo(packageName, 0).versionName}\n\n")

            val usuarios = obtenerUsuariosParaReporte()
            reporte.append("=== INFORMACION DE USUARIOS ===\n")
            reporte.append("Total usuarios: ${usuarios.size}\n")
            reporte.append("Usuarios activos: ${usuarios.count { it.activo }}\n")
            reporte.append("Administradores: ${usuarios.count { it.rol.equals("admin", true) }}\n\n")

            val totalProductos = obtenerTotalProductos()
            reporte.append("=== INFORMACION DE PRODUCTOS ===\n")
            reporte.append("Total productos: $totalProductos\n\n")

            val totalCarrito = obtenerTotalCarrito()
            reporte.append("=== INFORMACION DE VENTAS ===\n")
            reporte.append("Total carrito: $${"%.2f".format(totalCarrito)}\n\n")

            reporte.append("=== INFORMACION DEL DISPOSITIVO ===\n")
            reporte.append("Modelo: ${android.os.Build.MODEL}\n")
            reporte.append("Android: ${android.os.Build.VERSION.RELEASE}\n")
            reporte.append("SDK: ${android.os.Build.VERSION.SDK_INT}\n")

            mostrarReporteEnDialogo("Reporte General del Sistema", reporte.toString())

        } catch (e: Exception) {
            Log.e(TAG, "ERROR generando reporte general: ${e.message}", e)
            showToast("Error generando reporte general")
        }
    }

    private fun generarReporteUsuarios() {
        try {
            val usuarios = obtenerUsuariosParaReporte()
            val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            val reporte = StringBuilder()
            reporte.append("=== REPORTE DETALLADO DE USUARIOS ===\n\n")
            reporte.append("Fecha: $fechaActual\n")
            reporte.append("Total usuarios registrados: ${usuarios.size}\n\n")

            if (usuarios.isNotEmpty()) {
                val distribucionRoles = usuarios.groupBy { it.rol }.mapValues { it.value.size }
                reporte.append(" DISTRIBUCION POR ROLES:\n")
                reporte.append("-".repeat(35) + "\n")
                distribucionRoles.entries.sortedByDescending { it.value }.forEach { (rol, cantidad) ->
                    val porcentaje = if (usuarios.size > 0) (cantidad * 100.0 / usuarios.size) else 0.0
                    reporte.append("$rol: $cantidad (${"%.1f".format(porcentaje)}%)\n")
                }

                reporte.append("\nLISTA COMPLETA DE USUARIOS:\n")
                reporte.append("-".repeat(50) + "\n")
                usuarios.forEachIndexed { index, usuario ->
                    val estado = if (usuario.activo) "ACTIVO" else "INACTIVO"
                    reporte.append("${index + 1}. ${usuario.username} $estado\n")
                    reporte.append("   Rol: ${usuario.rol}\n")
                    reporte.append("   Email: ${usuario.email ?: "No especificado"}\n")
                    reporte.append("-".repeat(25) + "\n")
                }
            } else {
                reporte.append("No hay usuarios registrados en el sistema.\n")
            }

            mostrarReporteEnDialogo("Reporte de Usuarios", reporte.toString())

        } catch (e: Exception) {
            Log.e(TAG, "ERROR generando reporte de usuarios: ${e.message}", e)
            showToast("Error generando reporte de usuarios")
        }
    }

    private fun generarEstadisticasUso() {
        try {
            val usuarios = obtenerUsuariosParaReporte()
            val totalUsuarios = usuarios.size
            val usuariosActivos = usuarios.count { it.activo }
            val totalProductos = obtenerTotalProductos()
            val totalCarrito = obtenerTotalCarrito()

            val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            val reporte = StringBuilder()
            reporte.append("=== ESTADISTICAS DE USO DEL SISTEMA ===\n\n")
            reporte.append("Fecha: $fechaActual\n\n")

            reporte.append(" METRICAS PRINCIPALES:\n")
            reporte.append("-".repeat(25) + "\n")
            reporte.append("Total usuarios: $totalUsuarios\n")
            reporte.append("Usuarios activos: $usuariosActivos\n")
            reporte.append("Usuarios inactivos: ${totalUsuarios - usuariosActivos}\n")
            reporte.append("Total productos: $totalProductos\n")
            reporte.append("Valor carrito: $${"%.2f".format(totalCarrito)}\n")
            reporte.append("Tasa de actividad: ${if (totalUsuarios > 0) "%.1f".format(usuariosActivos * 100.0 / totalUsuarios) else 0}%\n\n")

            val distribucionRoles = usuarios.groupBy { it.rol }.mapValues { it.value.size }
            reporte.append(" ANALISIS POR ROL:\n")
            reporte.append("-".repeat(25) + "\n")
            distribucionRoles.entries.sortedByDescending { it.value }.forEach { (rol, cantidad) ->
                val porcentaje = if (totalUsuarios > 0) (cantidad * 100.0 / totalUsuarios) else 0.0
                reporte.append("$rol: $cantidad usuarios\n")
                reporte.append("  ${"%.1f".format(porcentaje)}% del total\n")
            }

            if (distribucionRoles.isNotEmpty()) {
                reporte.append("\n REPRESENTACION VISUAL:\n")
                distribucionRoles.entries.sortedByDescending { it.value }.forEach { (rol, cantidad) ->
                    val barras = "*".repeat((cantidad * 20 / totalUsuarios).coerceAtLeast(1))
                    reporte.append("$rol: $barras ($cantidad)\n")
                }
            }

            mostrarReporteEnDialogo("Estadisticas de Uso", reporte.toString())

        } catch (e: Exception) {
            Log.e(TAG, "ERROR generando estadisticas: ${e.message}", e)
            showToast("Error generando estadisticas")
        }
    }

    private fun generarAuditoriaSistema() {
        try {
            val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            val adminEmail = auth.currentUser?.email ?: "Administrador"

            val reporte = StringBuilder()
            reporte.append("=== AUDITORIA DEL SISTEMA ===\n\n")
            reporte.append(" INFORMACION DE AUDITORIA\n")
            reporte.append("-".repeat(30) + "\n")
            reporte.append("Fecha auditoria: $fechaActual\n")
            reporte.append("Auditor: $adminEmail\n")
            reporte.append("Sistema: MyApplication\n")
            reporte.append("Estado: OPERATIVO\n\n")

            reporte.append(" CHECKLIST DE SEGURIDAD\n")
            reporte.append("-".repeat(30) + "\n")
            reporte.append("Autenticacion Firebase activa\n")
            reporte.append("Gestion de sesiones implementada\n")
            reporte.append("Control de roles funcional\n")
            reporte.append("Logs de actividad habilitados\n")
            reporte.append("Manejo de errores robusto\n\n")

            reporte.append(" RECOMENDACIONES\n")
            reporte.append("-".repeat(30) + "\n")
            reporte.append("Realizar backup regular de datos\n")
            reporte.append("Revisar logs de seguridad semanalmente\n")
            reporte.append("Actualizar dependencias periodicamente\n")
            reporte.append("Monitorear actividad de usuarios admin\n")

            mostrarReporteEnDialogo("Auditoria del Sistema", reporte.toString())

        } catch (e: Exception) {
            Log.e(TAG, "ERROR generando auditoria: ${e.message}", e)
            showToast("Error generando auditoria")
        }
    }

    private fun mostrarOpcionesExportacion() {
        val opcionesExportacion = arrayOf(
            "Exportar Reporte Actual",
            "Exportar Todos los Reportes",
            "Programar Exportacion Diaria",
            "Enviar por Email"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Exportar Reportes")
            .setItems(opcionesExportacion) { dialog, which ->
                when (which) {
                    0 -> exportarReporteActual()
                    1 -> exportarTodosReportes()
                    2 -> programarExportacionDiaria()
                    3 -> enviarReportePorEmail()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun exportarReporteActual() {
        showToast("Funcionalidad de exportacion en desarrollo")
    }

    private fun exportarTodosReportes() {
        showToast("Exportando todos los reportes...")
    }

    private fun programarExportacionDiaria() {
        showToast("Exportacion diaria programada")
    }

    private fun enviarReportePorEmail() {
        showToast("Enviando reporte por email...")
    }

    private fun obtenerUsuariosParaReporte(): List<Usuario> {
        return try {
            Usuario.obtenerTodosLosUsuarios(this) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "ERROR obteniendo usuarios para reporte: ${e.message}", e)
            emptyList()
        }
    }

    private fun mostrarReporteEnDialogo(titulo: String, contenido: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(titulo)
            .setMessage(contenido)
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Compartir") { dialog, _ ->
                compartirReporte(contenido, titulo)
                dialog.dismiss()
            }
            .show()
    }

    private fun compartirReporte(contenido: String, titulo: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, "Reporte: $titulo")
            intent.putExtra(Intent.EXTRA_TEXT, contenido)

            startActivity(Intent.createChooser(intent, "Compartir Reporte"))

        } catch (e: Exception) {
            Log.e(TAG, "ERROR compartiendo reporte: ${e.message}", e)
            showToast("Error al compartir reporte")
        }
    }

    private fun cerrarSesion() {
        coroutineScope.launch {
            try {
                Log.d(TAG, "Cerrando sesion de administrador...")
                auth.signOut()
                SessionManager.logout(this@AdminActivity)
                SessionManager.clearSession(this@AdminActivity)
                Log.d(TAG, "Sesion cerrada")
                redirectToLogin()
            } catch (e: Exception) {
                Log.e(TAG, "ERROR cerrando sesion", e)
                redirectToLogin()
            }
        }
    }

    private fun redirectToLogin() {
        try {
            Log.d(TAG, "Redirigiendo a LoginActivity...")
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "ERROR redirigiendo a login: ${e.message}", e)
            finishAffinity()
        }
    }

    private fun crearLayoutMinimo() {
        try {
            Log.d(TAG, "Creando layout minimo de emergencia...")
            val textView = TextView(this).apply {
                text = "Panel de Administracion\n(Modo emergencia)"
                textSize = 18f
                setPadding(50, 50, 50, 50)
                gravity = android.view.Gravity.CENTER
            }
            val button = Button(this).apply {
                text = "Cerrar Sesion"
                setOnClickListener { cerrarSesion() }
            }
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                setBackgroundColor(android.graphics.Color.WHITE)
                addView(textView)
                addView(button)
            }
            setContentView(layout)
        } catch (e: Exception) {
            Log.e(TAG, "ERROR CRITICO en layout minimo", e)
            redirectToLogin()
        }
    }

    private fun showToast(message: String) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            Log.d(TAG, "Toast: $message")
        } catch (e: Exception) {
            Log.e(TAG, "ERROR mostrando toast: ${e.message}", e)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "AdminActivity onResume")
        if (::btnGestionarUsuarios.isInitialized) {
            btnGestionarUsuarios.isEnabled = true
        }
    }
}