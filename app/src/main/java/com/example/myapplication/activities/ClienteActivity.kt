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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClienteActivity : AppCompatActivity() {

    private lateinit var tvUserInfo: TextView
    private lateinit var btnVerProductos: Button
    private lateinit var btnVerCarrito: Button
    private lateinit var btnMisPedidos: Button
    private lateinit var btnCerrarSesion: Button
    private lateinit var auth: FirebaseAuth
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

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

        // ✅ Validar sesión activa
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
        tvUserInfo = findViewById(R.id.tvUserInfo)
        btnVerProductos = findViewById(R.id.btnVerProductos)
        btnVerCarrito = findViewById(R.id.btnVerCarrito)
        btnMisPedidos = findViewById(R.id.btnMisPedidos)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
        Log.d(TAG, "✅ Vistas inicializadas correctamente")
    }

    private fun setupUserInfo() {
        coroutineScope.launch {
            try {
                val currentUser = auth.currentUser
                val email = currentUser?.email ?: SessionManager.getCurrentUserEmail(this@ClienteActivity) ?: ""

                if (email.isNotEmpty()) {
                    // ✅ Verificar usuario tanto en SQLite (DatabaseHelper) como en Firebase
                    val usuarioLocal = withContext(Dispatchers.IO) {
                        Usuario.obtenerUsuarioPorNombre(this@ClienteActivity, email)
                    }

                    if (usuarioLocal != null) {
                        // Usuario encontrado en base local
                        val userInfo = "Usuario: $email\nRol: ${usuarioLocal.rol}"
                        tvUserInfo.text = userInfo
                        Log.d(TAG, "✅ Usuario encontrado en SQLite: ${usuarioLocal.username}")
                    } else {
                        // Si no existe localmente, se guarda como cliente predeterminado
                        val nuevoUsuario = Usuario(username = email, password = "", rol = "cliente")
                        val guardado = withContext(Dispatchers.IO) {
                            Usuario.registrarUsuario(this@ClienteActivity, nuevoUsuario)
                        }

                        if (guardado) {
                            Log.d(TAG, "✅ Usuario agregado a SQLite (sin registro previo)")
                            tvUserInfo.text = "Usuario: $email\nRol: cliente"
                        } else {
                            Log.w(TAG, "⚠️ No se pudo registrar localmente al usuario")
                            tvUserInfo.text = "Usuario: $email\nRol: cliente"
                        }
                    }
                } else {
                    tvUserInfo.text = "No se pudo cargar la información del usuario"
                    Log.w(TAG, "⚠️ No se pudo obtener email del usuario")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR cargando información del usuario: ${e.message}", e)
                // Fallback: mostrar información básica desde la sesión
                try {
                    val sessionEmail = SessionManager.getCurrentUserEmail(this@ClienteActivity)
                    val sessionRole = SessionManager.getCurrentUserRole(this@ClienteActivity)
                    if (!sessionEmail.isNullOrEmpty()) {
                        tvUserInfo.text = "Usuario: $sessionEmail\nRol: ${sessionRole ?: "cliente"}"
                    } else {
                        tvUserInfo.text = "Usuario\nRol: cliente"
                    }
                } catch (e2: Exception) {
                    tvUserInfo.text = "Error cargando información"
                }
            }
        }
    }

    private fun setupClickListeners() {
        // 🔹 Botón Ver Productos - Abre la actividad de productos
        btnVerProductos.setOnClickListener {
            try {
                Log.d(TAG, "🖱️ Clic en Ver Productos")
                abrirListaProductos()
            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR navegando a productos: ${e.message}", e)
                showToast("Error abriendo productos")
            }
        }

        btnVerCarrito.setOnClickListener {
            try {
                Log.d(TAG, "🖱️ Clic en Ver Carrito")
                abrirCarrito()
            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR en botón carrito: ${e.message}", e)
                showToast("Error abriendo carrito")
            }
        }

        btnMisPedidos.setOnClickListener {
            try {
                Log.d(TAG, "🖱️ Clic en Mis Pedidos")
                abrirMisPedidos()
            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR en botón pedidos: ${e.message}", e)
                showToast("Error abriendo pedidos")
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
    }

    /**
     * 🔹 CORREGIDO: Método para abrir la lista de productos
     */
    private fun abrirListaProductos() {
        try {
            Log.d(TAG, "🔄 Abriendo lista de productos...")

            // 🔥 CORRECCIÓN CRÍTICA: Cambiar a MainActivity que contiene el fragmento de productos
            val intent = Intent(this, MainActivity::class.java).apply {
                // Opcional: agregar extra para indicar que debe ir directamente a productos
                putExtra("fragment_destination", "productos")
            }
            startActivity(intent)

            Log.d(TAG, "✅ Navegación a productos iniciada")

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR abriendo productos: ${e.message}", e)
            showToast("No se pudo abrir la lista de productos")
        }
    }

    /**
     * 🔹 NUEVO: Método alternativo si prefieres usar la Activity de productos directamente
     */
    private fun abrirProductosActivity() {
        try {
            // Si creaste la ProductosActivity que te sugerí anteriormente
            val intent = Intent(this, ProductosActivity::class.java)
            startActivity(intent)
            Log.d(TAG, "✅ ProductosActivity iniciada")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR abriendo ProductosActivity: ${e.message}", e)
            showToast("Error: Activity de productos no disponible")
        }
    }

    /**
     * 🔹 Método para abrir el carrito (a implementar)
     */
    private fun abrirCarrito() {
        try {
            // TODO: Implementar lógica del carrito
            showToast("Funcionalidad de carrito en desarrollo")
            Log.d(TAG, "📋 Abriendo carrito (pendiente de implementar)")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR abriendo carrito: ${e.message}", e)
            showToast("Error abriendo carrito")
        }
    }

    /**
     * 🔹 Método para abrir mis pedidos (a implementar)
     */
    private fun abrirMisPedidos() {
        try {
            // TODO: Implementar lógica de pedidos
            showToast("Funcionalidad de pedidos en desarrollo")
            Log.d(TAG, "📦 Abriendo mis pedidos (pendiente de implementar)")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR abriendo pedidos: ${e.message}", e)
            showToast("Error abriendo pedidos")
        }
    }

    private fun cerrarSesion() {
        coroutineScope.launch {
            try {
                Log.d(TAG, "🔒 Cerrando sesión...")
                auth.signOut()
                SessionManager.logout(this@ClienteActivity)
                SessionManager.clearSession(this@ClienteActivity)
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
            moveTaskToBack(true)
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR en onBackPressed: ${e.message}", e)
            super.onBackPressed()
        }
    }

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