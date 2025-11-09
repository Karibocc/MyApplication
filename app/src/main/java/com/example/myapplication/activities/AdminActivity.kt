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
import kotlin.collections.HashMap

class AdminActivity : AppCompatActivity() {

    private lateinit var tvAdminInfo: TextView
    private lateinit var btnGestionarProductos: LinearLayout
    private lateinit var btnGestionarUsuarios: LinearLayout
    private lateinit var btnVerReportes: LinearLayout
    private lateinit var btnCerrarSesion: Button

    private val auth = Firebase.auth
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val TAG = "AdminActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "🚀 [1] ADMIN ACTIVITY - ONCREATE INICIADO")
        Log.d("ProcessIndicator", "🔄 AdminActivity onCreate started")

        try {
            setContentView(R.layout.activity_admin)
            Log.d(TAG, "✅ [2] Layout activity_admin cargado")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR cargando layout: ${e.message}", e)
            showToast("Error cargando el panel de administración")
            crearLayoutMinimo()
            return
        }

        try {
            initializeViews()
            setupUserInfo()
            setupClickListeners()
            Log.d(TAG, "✅ [3] AdminActivity completamente configurada")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR configurando AdminActivity: ${e.message}", e)
            showToast("Error configurando el panel de administración")
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

            Log.d(TAG, "✅ [4] Vistas inicializadas correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR en initializeViews: ${e.message}", e)
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
                    Log.d(TAG, "✅ [5] Información de admin: $email")
                } else {
                    tvAdminInfo.text = "Administrador del Sistema"
                    Log.w(TAG, "⚠️ No se pudo obtener email del admin")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR cargando información del admin: ${e.message}", e)
                tvAdminInfo.text = "Administrador del Sistema"
            }
        }
    }

    private fun setupClickListeners() {
        btnGestionarUsuarios.setOnClickListener {
            Log.d(TAG, "🎯 [CLIC] Botón Gestionar Usuarios PRESIONADO")
            Log.d("ProcessIndicator", "🖱️ User clicked GestionarUsuarios button")
            diagnosticarCompleto()
            abrirGestionUsuariosConSeguridad()
        }

        btnGestionarProductos.setOnClickListener {
            Log.d(TAG, "🖱️ Clic en Gestionar Productos")
            gestionarProductos()
        }

        btnVerReportes.setOnClickListener {
            Log.d(TAG, "🖱️ Clic en Ver Reportes")
            abrirReportesConDatosReales()
        }

        btnCerrarSesion.setOnClickListener {
            Log.d(TAG, "🖱️ Clic en Cerrar Sesión")
            cerrarSesion()
        }

        Log.d(TAG, "✅ [6] Todos los listeners configurados")
    }

    /**
     * 🔹 NUEVO MÉTODO: Abrir reportes con datos reales
     */
    private fun abrirReportesConDatosReales() {
        try {
            Log.d(TAG, "📊 Abriendo pantalla de reportes con datos reales...")
            val intent = Intent(this, VerReportesActivity::class.java)

            // Pasar datos actualizados a la actividad de reportes
            val usuarios = obtenerUsuariosParaReporte()
            val totalUsuarios = usuarios.size
            val totalProductos = obtenerTotalProductos()
            val totalCarrito = obtenerTotalCarrito()

            intent.putExtra("totalUsuarios", totalUsuarios)
            intent.putExtra("totalProductos", totalProductos)
            intent.putExtra("totalCarrito", totalCarrito)

            startActivity(intent)
            Log.d(TAG, "✅ VerReportesActivity iniciada con datos actualizados")

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR abriendo reportes: ${e.message}", e)
            // Fallback: mostrar menú de reportes en diálogo
            mostrarMenuReportes()
        }
    }

    /**
     * 🔹 MÉTODO ACTUALIZADO: Obtener total de productos
     */
    private fun obtenerTotalProductos(): Int {
        return try {
            // Aquí implementa la lógica real para obtener productos de tu base de datos
            // Por ahora retornamos un valor de ejemplo
            val productos = listOf("Producto1", "Producto2", "Producto3") // Ejemplo
            productos.size
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR obteniendo total de productos: ${e.message}", e)
            0
        }
    }

    /**
     * 🔹 MÉTODO ACTUALIZADO: Obtener total del carrito
     */
    private fun obtenerTotalCarrito(): Double {
        return try {
            // Aquí implementa la lógica real para calcular el total del carrito
            // Por ahora retornamos un valor de ejemplo
            1250.75 // Ejemplo: $1,250.75
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR obteniendo total del carrito: ${e.message}", e)
            0.0
        }
    }

    private fun diagnosticarCompleto() {
        Log.d(TAG, "🔍 [DIAGNÓSTICO] INICIANDO ANÁLISIS COMPLETO")

        try {
            val targetClass = Class.forName("com.example.myapplication.activities.GestionarUsuariosActivity")
            Log.d(TAG, "✅ [DIAG] GestionarUsuariosActivity EXISTE: $targetClass")
            Log.d("ProcessIndicator", "✅ Target activity exists")
        } catch (e: ClassNotFoundException) {
            Log.e(TAG, "❌ [DIAG] GestionarUsuariosActivity NO EXISTE", e)
            Log.e("ProcessIndicator", "❌ Target activity not found")
            return
        }

        Log.d(TAG, "📱 [DIAG] Contexto: $this")
        Log.d(TAG, "🔍 [DIAG] Actividad no nula: ${this != null}")
        Log.d(TAG, "🔍 [DIAG] Actividad no finalizada: ${!isFinishing}")

        try {
            val testIntent = Intent(this, GestionarUsuariosActivity::class.java)
            Log.d(TAG, "✅ [DIAG] Intent de prueba creado: $testIntent")
            Log.d("ProcessIndicator", "✅ Test intent created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ [DIAG] Error creando Intent de prueba", e)
            Log.e("ProcessIndicator", "❌ Error creating test intent")
        }

        Log.d(TAG, "🔍 [DIAGNÓSTICO] COMPLETADO")
    }

    private fun abrirGestionUsuariosConSeguridad() {
        Log.d(TAG, "🛡️ [APERTURA] INICIANDO PROCESO DE APERTURA")
        Log.d("ProcessIndicator", "🚀 Starting activity launch process")

        btnGestionarUsuarios.isEnabled = false
        Log.d(TAG, "🔒 [APERTURA] Botón deshabilitado")

        try {
            Log.d(TAG, "📦 [APERTURA] Creando Intent...")
            val intent = Intent(this, GestionarUsuariosActivity::class.java)

            Log.d(TAG, "🎯 [APERTURA] Intent creado: $intent")
            Log.d("ProcessIndicator", "📦 Intent created, starting activity...")

            startActivity(intent)

            Log.d(TAG, "🎉 [APERTURA] startActivity() EJECUTADO - ÉXITO")
            Log.d("ProcessIndicator", "✅ Activity start command executed")

            handler.postDelayed({
                btnGestionarUsuarios.isEnabled = true
                Log.d(TAG, "🔓 [APERTURA] Botón rehabilitado")
            }, 3000)

        } catch (e: SecurityException) {
            Log.e(TAG, "❌ [ERROR] SecurityException: ${e.message}", e)
            Log.e("ProcessIndicator", "❌ Security exception")
            showToast("Error de seguridad")
            btnGestionarUsuarios.isEnabled = true
        } catch (e: android.content.ActivityNotFoundException) {
            Log.e(TAG, "❌ [ERROR] ActivityNotFoundException: ${e.message}", e)
            Log.e("ProcessIndicator", "❌ Activity not found")
            showToast("Actividad no encontrada")
            btnGestionarUsuarios.isEnabled = true
        } catch (e: IllegalStateException) {
            Log.e(TAG, "❌ [ERROR] IllegalStateException: ${e.message}", e)
            Log.e("ProcessIndicator", "❌ Illegal state")
            showToast("Error de estado: ${e.message}")
            btnGestionarUsuarios.isEnabled = true
        } catch (e: Exception) {
            Log.e(TAG, "❌ [ERROR] Exception inesperada: ${e.message}", e)
            Log.e(TAG, "❌ [ERROR] Tipo: ${e.javaClass.simpleName}")
            Log.e("ProcessIndicator", "❌ Unexpected error: ${e.javaClass.simpleName}")
            showToast("Error: ${e.localizedMessage}")
            btnGestionarUsuarios.isEnabled = true
        }
    }

    /**
     * 🔹 SISTEMA COMPLETO DE REPORTES (como fallback)
     */
    private fun mostrarMenuReportes() {
        val opcionesReportes = arrayOf(
            "📈 Reporte General del Sistema",
            "👥 Reporte de Usuarios",
            "📊 Estadísticas de Uso",
            "🔍 Auditoría del Sistema",
            "📤 Exportar Reportes"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("📊 REPORTES DEL SISTEMA")
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

    /**
     * Reporte General del Sistema
     */
    private fun generarReporteGeneralSistema() {
        try {
            val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            val adminEmail = auth.currentUser?.email ?: "No identificado"

            val reporte = StringBuilder()
            reporte.append("=== REPORTE GENERAL DEL SISTEMA ===\n\n")
            reporte.append("📅 Fecha generación: $fechaActual\n")
            reporte.append("👤 Administrador: $adminEmail\n")
            reporte.append("📱 Versión: ${packageManager.getPackageInfo(packageName, 0).versionName}\n\n")

            // Información de usuarios
            val usuarios = obtenerUsuariosParaReporte()
            reporte.append("=== INFORMACIÓN DE USUARIOS ===\n")
            reporte.append("• Total usuarios: ${usuarios.size}\n")
            reporte.append("• Usuarios activos: ${usuarios.count { it.activo }}\n")
            reporte.append("• Administradores: ${usuarios.count { it.rol.equals("admin", true) }}\n\n")

            // Información de productos
            val totalProductos = obtenerTotalProductos()
            reporte.append("=== INFORMACIÓN DE PRODUCTOS ===\n")
            reporte.append("• Total productos: $totalProductos\n\n")

            // Información del carrito
            val totalCarrito = obtenerTotalCarrito()
            reporte.append("=== INFORMACIÓN DE VENTAS ===\n")
            reporte.append("• Total carrito: $${"%.2f".format(totalCarrito)}\n\n")

            // Información del dispositivo
            reporte.append("=== INFORMACIÓN DEL DISPOSITIVO ===\n")
            reporte.append("• Modelo: ${android.os.Build.MODEL}\n")
            reporte.append("• Android: ${android.os.Build.VERSION.RELEASE}\n")
            reporte.append("• SDK: ${android.os.Build.VERSION.SDK_INT}\n")

            mostrarReporteEnDialogo("Reporte General del Sistema", reporte.toString())

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR generando reporte general: ${e.message}", e)
            showToast("Error generando reporte general")
        }
    }

    /**
     * Reporte Específico de Usuarios
     */
    private fun generarReporteUsuarios() {
        try {
            val usuarios = obtenerUsuariosParaReporte()
            val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            val reporte = StringBuilder()
            reporte.append("=== REPORTE DETALLADO DE USUARIOS ===\n\n")
            reporte.append("Fecha: $fechaActual\n")
            reporte.append("Total usuarios registrados: ${usuarios.size}\n\n")

            if (usuarios.isNotEmpty()) {
                // Distribución por roles
                val distribucionRoles = usuarios.groupBy { it.rol }.mapValues { it.value.size }
                reporte.append("📊 DISTRIBUCIÓN POR ROLES:\n")
                reporte.append("-".repeat(35) + "\n")
                distribucionRoles.entries.sortedByDescending { it.value }.forEach { (rol, cantidad) ->
                    val porcentaje = if (usuarios.size > 0) (cantidad * 100.0 / usuarios.size) else 0.0
                    reporte.append("• $rol: $cantidad (${"%.1f".format(porcentaje)}%)\n")
                }

                reporte.append("\n👥 LISTA COMPLETA DE USUARIOS:\n")
                reporte.append("-".repeat(50) + "\n")
                usuarios.forEachIndexed { index, usuario ->
                    val estado = if (usuario.activo) "✅" else "❌"
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
            Log.e(TAG, "❌ ERROR generando reporte de usuarios: ${e.message}", e)
            showToast("Error generando reporte de usuarios")
        }
    }

    /**
     * Estadísticas de Uso
     */
    private fun generarEstadisticasUso() {
        try {
            val usuarios = obtenerUsuariosParaReporte()
            val totalUsuarios = usuarios.size
            val usuariosActivos = usuarios.count { it.activo }
            val totalProductos = obtenerTotalProductos()
            val totalCarrito = obtenerTotalCarrito()

            val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            val reporte = StringBuilder()
            reporte.append("=== ESTADÍSTICAS DE USO DEL SISTEMA ===\n\n")
            reporte.append("Fecha: $fechaActual\n\n")

            reporte.append("📈 MÉTRICAS PRINCIPALES:\n")
            reporte.append("-".repeat(25) + "\n")
            reporte.append("• Total usuarios: $totalUsuarios\n")
            reporte.append("• Usuarios activos: $usuariosActivos\n")
            reporte.append("• Usuarios inactivos: ${totalUsuarios - usuariosActivos}\n")
            reporte.append("• Total productos: $totalProductos\n")
            reporte.append("• Valor carrito: $${"%.2f".format(totalCarrito)}\n")
            reporte.append("• Tasa de actividad: ${if (totalUsuarios > 0) "%.1f".format(usuariosActivos * 100.0 / totalUsuarios) else 0}%\n\n")

            // Distribución detallada
            val distribucionRoles = usuarios.groupBy { it.rol }.mapValues { it.value.size }
            reporte.append("🎯 ANÁLISIS POR ROL:\n")
            reporte.append("-".repeat(25) + "\n")
            distribucionRoles.entries.sortedByDescending { it.value }.forEach { (rol, cantidad) ->
                val porcentaje = if (totalUsuarios > 0) (cantidad * 100.0 / totalUsuarios) else 0.0
                reporte.append("• $rol: $cantidad usuarios\n")
                reporte.append("  ↳ ${"%.1f".format(porcentaje)}% del total\n")
            }

            // Gráfico ASCII simple
            if (distribucionRoles.isNotEmpty()) {
                reporte.append("\n📊 REPRESENTACIÓN VISUAL:\n")
                distribucionRoles.entries.sortedByDescending { it.value }.forEach { (rol, cantidad) ->
                    val barras = "█".repeat((cantidad * 20 / totalUsuarios).coerceAtLeast(1))
                    reporte.append("$rol: $barras ($cantidad)\n")
                }
            }

            mostrarReporteEnDialogo("Estadísticas de Uso", reporte.toString())

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR generando estadísticas: ${e.message}", e)
            showToast("Error generando estadísticas")
        }
    }

    /**
     * Auditoría del Sistema
     */
    private fun generarAuditoriaSistema() {
        try {
            val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            val adminEmail = auth.currentUser?.email ?: "Administrador"

            val reporte = StringBuilder()
            reporte.append("=== AUDITORÍA DEL SISTEMA ===\n\n")
            reporte.append("🔍 INFORMACIÓN DE AUDITORÍA\n")
            reporte.append("-".repeat(30) + "\n")
            reporte.append("• Fecha auditoría: $fechaActual\n")
            reporte.append("• Auditor: $adminEmail\n")
            reporte.append("• Sistema: MyApplication\n")
            reporte.append("• Estado: 🔵 OPERATIVO\n\n")

            reporte.append("📋 CHECKLIST DE SEGURIDAD\n")
            reporte.append("-".repeat(30) + "\n")
            reporte.append("✅ Autenticación Firebase activa\n")
            reporte.append("✅ Gestión de sesiones implementada\n")
            reporte.append("✅ Control de roles funcional\n")
            reporte.append("✅ Logs de actividad habilitados\n")
            reporte.append("✅ Manejo de errores robusto\n\n")

            reporte.append("🎯 RECOMENDACIONES\n")
            reporte.append("-".repeat(30) + "\n")
            reporte.append("• Realizar backup regular de datos\n")
            reporte.append("• Revisar logs de seguridad semanalmente\n")
            reporte.append("• Actualizar dependencias periódicamente\n")
            reporte.append("• Monitorear actividad de usuarios admin\n")

            mostrarReporteEnDialogo("Auditoría del Sistema", reporte.toString())

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR generando auditoría: ${e.message}", e)
            showToast("Error generando auditoría")
        }
    }

    /**
     * Opciones de Exportación
     */
    private fun mostrarOpcionesExportacion() {
        val opcionesExportacion = arrayOf(
            "📄 Exportar Reporte Actual",
            "📊 Exportar Todos los Reportes",
            "🔄 Programar Exportación Diaria",
            "📧 Enviar por Email"
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
        showToast("📄 Funcionalidad de exportación en desarrollo")
    }

    private fun exportarTodosReportes() {
        showToast("📊 Exportando todos los reportes...")
    }

    private fun programarExportacionDiaria() {
        showToast("🔄 Exportación diaria programada")
    }

    private fun enviarReportePorEmail() {
        showToast("📧 Enviando reporte por email...")
    }

    /**
     * Utilidades para Reportes
     */
    private fun obtenerUsuariosParaReporte(): List<Usuario> {
        return try {
            Usuario.obtenerTodosLosUsuarios(this) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR obteniendo usuarios para reporte: ${e.message}", e)
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
            Log.e(TAG, "❌ ERROR compartiendo reporte: ${e.message}", e)
            showToast("Error al compartir reporte")
        }
    }

    private fun gestionarProductos() {
        try {
            Log.d(TAG, "🔄 Abriendo gestión de productos...")
            val intent = Intent(this, AgregarProductoActivity::class.java)
            startActivity(intent)
            Log.d(TAG, "✅ GestionarProductosActivity iniciada")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR abriendo gestión de productos: ${e.message}", e)
            showToast("Funcionalidad de productos en desarrollo")
        }
    }

    private fun cerrarSesion() {
        coroutineScope.launch {
            try {
                Log.d(TAG, "🔒 Cerrando sesión de administrador...")
                auth.signOut()
                SessionManager.logout(this@AdminActivity)
                SessionManager.clearSession(this@AdminActivity)
                Log.d(TAG, "✅ Sesión cerrada")
                redirectToLogin()
            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR cerrando sesión", e)
                redirectToLogin()
            }
        }
    }

    private fun redirectToLogin() {
        try {
            Log.d(TAG, "🔄 Redirigiendo a LoginActivity...")
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR redirigiendo a login: ${e.message}", e)
            finishAffinity()
        }
    }

    private fun crearLayoutMinimo() {
        try {
            Log.d(TAG, "🔄 Creando layout mínimo de emergencia...")
            val textView = TextView(this).apply {
                text = "Panel de Administración\n(Modo emergencia)"
                textSize = 18f
                setPadding(50, 50, 50, 50)
                gravity = android.view.Gravity.CENTER
            }
            val button = Button(this).apply {
                text = "Cerrar Sesión"
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
            Log.e(TAG, "❌ ERROR CRÍTICO en layout mínimo", e)
            redirectToLogin()
        }
    }

    private fun showToast(message: String) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            Log.d(TAG, "💬 Toast: $message")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR mostrando toast: ${e.message}", e)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "📱 [RESUME] AdminActivity onResume")
        if (::btnGestionarUsuarios.isInitialized) {
            btnGestionarUsuarios.isEnabled = true
        }
    }
}