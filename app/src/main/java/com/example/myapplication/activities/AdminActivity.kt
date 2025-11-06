package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.managers.SessionManager
import com.example.myapplication.models.Usuario
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminActivity : AppCompatActivity() {

    private lateinit var tvAdminInfo: TextView
    private lateinit var btnGestionarProductos: Button
    private lateinit var btnGestionarUsuarios: Button
    private lateinit var btnVerReportes: Button
    private lateinit var btnCerrarSesion: Button

    private val auth = Firebase.auth
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    companion object {
        private const val TAG = "AdminActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "🚀 AdminActivity onCreate iniciado")

        try {
            setContentView(R.layout.activity_admin)
            Log.d(TAG, "✅ Layout activity_admin cargado exitosamente")
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
            Log.d(TAG, "✅ AdminActivity configurada exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR configurando AdminActivity: ${e.message}", e)
            showToast("Error configurando el panel de administración")
            crearLayoutMinimo()
        }
    }

    private fun initializeViews() {
        tvAdminInfo = findViewById(R.id.tvAdminInfo)
        btnGestionarProductos = findViewById(R.id.btnGestionarProductos)
        btnGestionarUsuarios = findViewById(R.id.btnGestionarUsuarios)
        btnVerReportes = findViewById(R.id.btnVerReportes)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        Log.d(TAG, "✅ Vistas inicializadas correctamente")
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
                    Log.d(TAG, "✅ Información de admin cargada: $email")
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
        // 🔹 Botón Gestionar Productos
        btnGestionarProductos.setOnClickListener {
            try {
                Log.d(TAG, "🖱️ Clic en Gestionar Productos")
                gestionarProductos()
            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR en botón gestionar productos: ${e.message}", e)
                showToast("Error abriendo gestión de productos")
            }
        }

        // 🔹 Botón Gestionar Usuarios
        btnGestionarUsuarios.setOnClickListener {
            try {
                Log.d(TAG, "🖱️ Clic en Gestionar Usuarios")
                gestionarUsuarios()
            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR en botón gestionar usuarios: ${e.message}", e)
                showToast("Error abriendo gestión de usuarios")
            }
        }

        // 🔹 Botón Ver Reportes
        btnVerReportes.setOnClickListener {
            try {
                Log.d(TAG, "🖱️ Clic en Ver Reportes")
                verReportes()
            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR en botón ver reportes: ${e.message}", e)
                showToast("Error abriendo reportes")
            }
        }

        // 🔹 Botón Cerrar Sesión
        btnCerrarSesion.setOnClickListener {
            try {
                Log.d(TAG, "🖱️ Clic en Cerrar Sesión")
                cerrarSesion()
            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR en botón cerrar sesión: ${e.message}", e)
            }
        }

        Log.d(TAG, "✅ Listeners configurados correctamente")
    }

    /**
     * 🔹 Método para gestionar productos
     */
    private fun gestionarProductos() {
        try {
            Log.d(TAG, "🔄 Abriendo gestión de productos...")

            // TODO: Implementar Activity de gestión de productos
            // Por ahora creamos una actividad básica
            val intent = Intent(this, AgregarProductoActivity::class.java)
            startActivity(intent)
            Log.d(TAG, "✅ GestionarProductosActivity iniciada")

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR abriendo gestión de productos: ${e.message}", e)
            showToast("Funcionalidad de productos en desarrollo")
        }
    }

    /**
     * 🔹 Método para gestionar usuarios
     */
    private fun gestionarUsuarios() {
        try {
            Log.d(TAG, "🔄 Abriendo gestión de usuarios...")

            // TODO: Implementar Activity de gestión de usuarios
            // Por ahora creamos una actividad básica
            val intent = Intent(this, GestionarUsuariosActivity::class.java)
            startActivity(intent)
            Log.d(TAG, "✅ GestionarUsuariosActivity iniciada")

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR abriendo gestión de usuarios: ${e.message}", e)
            showToast("Funcionalidad de usuarios en desarrollo")
        }
    }

    /**
     * 🔹 Método para ver reportes
     */
    private fun verReportes() {
        try {
            Log.d(TAG, "🔄 Abriendo reportes...")

            // TODO: Implementar Activity de reportes
            showToast("Funcionalidad de reportes en desarrollo")
            Log.d(TAG, "📊 Abriendo reportes (pendiente de implementar)")

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR abriendo reportes: ${e.message}", e)
            showToast("Error abriendo reportes")
        }
    }

    private fun cerrarSesion() {
        coroutineScope.launch {
            try {
                Log.d(TAG, "🔒 Cerrando sesión de administrador...")
                auth.signOut()
                SessionManager.logout(this@AdminActivity)
                SessionManager.clearSession(this@AdminActivity)
                Log.d(TAG, "✅ Sesión cerrada en Firebase y SessionManager")
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

            val layout = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                setBackgroundColor(android.graphics.Color.WHITE)
                addView(textView)
                addView(button)
            }

            setContentView(layout)
            Log.d(TAG, "✅ Layout mínimo creado exitosamente")

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR CRÍTICO en layout mínimo", e)
            redirectToLogin()
        }
    }

    private fun showToast(message: String) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            Log.d(TAG, "💬 Toast: $message")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR mostrando toast: ${e.message}", e)
        }
    }

    override fun onBackPressed() {
        try {
            Log.d(TAG, "🔙 Botón back presionado")
            // No hacer nada o mostrar mensaje
            showToast("Use el botón 'Cerrar Sesión' para salir")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR en onBackPressed: ${e.message}", e)
            super.onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "📱 AdminActivity onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "📱 AdminActivity onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "📱 AdminActivity onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "📱 AdminActivity onDestroy")
    }
}