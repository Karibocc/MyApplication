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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ClienteActivity : AppCompatActivity() {

    private lateinit var tvUserInfo: TextView
    private lateinit var btnVerProductos: Button
    private lateinit var btnVerCarrito: Button
    private lateinit var btnMisPedidos: Button
    private lateinit var btnCerrarSesion: Button
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val TAG = "ClienteActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "🚀 ClienteActivity onCreate iniciado")

        try {
            setContentView(R.layout.activity_cliente)
            Log.d(TAG, "✅ Layout activity_cliente cargado exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR cargando layout: ${e.message}", e)
            showToast("Error cargando la aplicación")
            redirectToLogin()
            return
        }

        try {
            auth = Firebase.auth
            Log.d(TAG, "✅ Firebase Auth inicializado")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR inicializando Firebase: ${e.message}", e)
            showToast("Error de configuración")
            redirectToLogin()
            return
        }

        // ✅ MEJORADO: Validar sesión antes de continuar
        try {
            Log.d(TAG, "🔍 Validando sesión del usuario...")
            val currentUser = auth.currentUser
            val sessionEmail = SessionManager.getCurrentUserEmail(this)

            Log.d(TAG, "📊 Firebase user: ${currentUser?.email ?: "null"}")
            Log.d(TAG, "📊 SessionManager: ${sessionEmail ?: "null"}")

            if (currentUser == null && sessionEmail.isNullOrEmpty()) {
                Log.w(TAG, "🚨 No hay sesión activa, redirigiendo a login")
                showToast("Sesión no válida")
                redirectToLogin()
                return
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR validando sesión: ${e.message}", e)
            redirectToLogin()
            return
        }

        try {
            initializeViews()
            setupUserInfo()
            setupClickListeners()
            Log.d(TAG, "✅ ClienteActivity configurada exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR configurando ClienteActivity: ${e.message}", e)
            showToast("Error configurando la aplicación")
            redirectToLogin()
        }
    }

    private fun initializeViews() {
        try {
            tvUserInfo = findViewById(R.id.tvUserInfo)
            btnVerProductos = findViewById(R.id.btnVerProductos)
            btnVerCarrito = findViewById(R.id.btnVerCarrito)
            btnMisPedidos = findViewById(R.id.btnMisPedidos)
            btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
            Log.d(TAG, "✅ Vistas inicializadas correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR inicializando vistas: ${e.message}", e)
            throw e // Relanzar para manejo superior
        }
    }

    private fun setupUserInfo() {
        try {
            val currentUser = auth.currentUser
            val email = currentUser?.email ?: SessionManager.getCurrentUserEmail(this) ?: ""

            if (email.isNotEmpty()) {
                val usuario = Usuario.obtenerUsuarioPorNombre(this, email)
                val userInfo = "Usuario: $email\nRol: ${usuario?.rol ?: "cliente"}"
                tvUserInfo.text = userInfo
                Log.d(TAG, "✅ Información de usuario cargada: $email")
            } else {
                tvUserInfo.text = "No se pudo cargar la información del usuario"
                Log.w(TAG, "⚠️ No se pudo obtener email del usuario")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR cargando información del usuario: ${e.message}", e)
            tvUserInfo.text = "Error cargando información"
        }
    }

    private fun setupClickListeners() {
        try {
            btnVerProductos.setOnClickListener {
                try {
                    Log.d(TAG, "🖱️ Clic en Ver Productos")
                    startActivity(Intent(this, MainActivity::class.java))
                } catch (e: Exception) {
                    Log.e(TAG, "❌ ERROR navegando a productos: ${e.message}", e)
                    showToast("Error abriendo productos")
                }
            }

            btnVerCarrito.setOnClickListener {
                try {
                    Log.d(TAG, "🖱️ Clic en Ver Carrito")
                    showToast("Funcionalidad de carrito en desarrollo")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ ERROR en botón carrito: ${e.message}", e)
                }
            }

            btnMisPedidos.setOnClickListener {
                try {
                    Log.d(TAG, "🖱️ Clic en Mis Pedidos")
                    showToast("Funcionalidad de pedidos en desarrollo")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ ERROR en botón pedidos: ${e.message}", e)
                }
            }

            btnCerrarSesion.setOnClickListener {
                try {
                    Log.d(TAG, "🖱️ Clic en Cerrar Sesión")
                    cerrarSesion()
                } catch (e: Exception) {
                    Log.e(TAG, "❌ ERROR en botón cerrar sesión: ${e.message}", e)
                }
            }

            Log.d(TAG, "✅ Listeners configurados correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR configurando listeners: ${e.message}", e)
        }
    }

    private fun cerrarSesion() {
        try {
            Log.d(TAG, "🔒 Cerrando sesión...")
            auth.signOut()
            SessionManager.logout(this)

            showToast("Sesión cerrada")
            Log.d(TAG, "✅ Sesión cerrada exitosamente")

            redirectToLogin()
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR cerrando sesión: ${e.message}", e)
            showToast("Error cerrando sesión")
            redirectToLogin()
        }
    }

    // ✅ NUEVA FUNCIÓN: Redirección segura a login
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

    // ✅ NUEVA FUNCIÓN: Mostrar toasts con manejo de errores
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
            // Evitar que el usuario regrese al login con back button
            moveTaskToBack(true)
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR en onBackPressed: ${e.message}", e)
            super.onBackPressed()
        }
    }

    // ✅ NUEVO: Logs en métodos del ciclo de vida
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "📱 ClienteActivity onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "📱 ClienteActivity onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "📱 ClienteActivity onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "📱 ClienteActivity onDestroy")
    }
}