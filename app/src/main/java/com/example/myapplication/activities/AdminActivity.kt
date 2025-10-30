package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.managers.SessionManager
import com.example.myapplication.models.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AdminActivity : AppCompatActivity() {

    private lateinit var tvAdminInfo: TextView
    private lateinit var btnGestionarProductos: Button
    private lateinit var btnGestionarUsuarios: Button
    private lateinit var btnVerReportes: Button
    private lateinit var btnCerrarSesion: Button
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val TAG = "AdminActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_admin)
            Log.d(TAG, "✅ Layout cargado correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error cargando layout: ${e.message}", e)
            Toast.makeText(this, "Error al cargar la interfaz", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        try {
            auth = Firebase.auth
            initializeViews()
            setupAdminInfo()
            setupClickListeners()
            setupBackHandler() // registramos el handler moderno del botón atrás
            Log.d(TAG, "✅ AdminActivity inicializada correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error inicializando AdminActivity: ${e.message}", e)
            Toast.makeText(this, "Error al iniciar la aplicación", Toast.LENGTH_SHORT).show()
            redirectToLogin()
        }
    }

    private fun initializeViews() {
        tvAdminInfo = findViewById(R.id.tvAdminInfo)
        btnGestionarProductos = findViewById(R.id.btnGestionarProductos)
        btnGestionarUsuarios = findViewById(R.id.btnGestionarUsuarios)
        btnVerReportes = findViewById(R.id.btnVerReportes)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
    }

    private fun setupAdminInfo() {
        try {
            val currentUser = auth.currentUser
            val email = currentUser?.email ?: SessionManager.getCurrentUserEmail(this) ?: ""

            if (email.isNotEmpty()) {
                val usuario = Usuario.obtenerUsuarioPorNombre(this, email)

                if (usuario != null) {
                    val adminInfo = "Administrador: ${usuario.username}\nRol: ${usuario.rol}"
                    tvAdminInfo.text = adminInfo
                    Log.d(TAG, "✅ Usuario encontrado localmente: ${usuario.username}")
                } else {
                    tvAdminInfo.text = "Administrador: $email\nRol: admin (solo Firebase)"
                    Log.w(TAG, "⚠️ Usuario no encontrado en DatabaseHelper, solo en Firebase")
                }
            } else {
                tvAdminInfo.text = "No se pudo cargar la información del administrador"
                Log.w(TAG, "⚠️ No se encontró email en sesión ni en Firebase")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo información del administrador: ${e.message}", e)
            tvAdminInfo.text = "Error cargando datos del administrador"
        }
    }

    private fun setupClickListeners() {
        btnGestionarProductos.setOnClickListener {
            Toast.makeText(this, "Gestión de productos en desarrollo", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "🖱️ Clic en Gestión de Productos")
        }

        btnGestionarUsuarios.setOnClickListener {
            Toast.makeText(this, "Gestión de usuarios en desarrollo", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "🖱️ Clic en Gestión de Usuarios")
        }

        btnVerReportes.setOnClickListener {
            Toast.makeText(this, "Reportes en desarrollo", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "🖱️ Clic en Ver Reportes")
        }

        btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }
    }

    private fun cerrarSesion() {
        try {
            Log.d(TAG, "🔒 Cerrando sesión de administrador...")
            auth.signOut()
            SessionManager.logout(this)
            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
            redirectToLogin()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error cerrando sesión: ${e.message}", e)
            Toast.makeText(this, "Error al cerrar sesión", Toast.LENGTH_SHORT).show()
            redirectToLogin()
        }
    }

    private fun redirectToLogin() {
        try {
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error redirigiendo a Login: ${e.message}", e)
            finishAffinity()
        }
    }

    /** Registramos el callback moderno para interceptar el botón 'Back' */
    private fun setupBackHandler() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Mantiene la misma lógica: enviar tarea al fondo en lugar de volver al login
                moveTaskToBack(true)
                Log.d(TAG, "🔙 Back presionado - movimiento a background (Admin)")
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    // Logs del ciclo de vida
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



