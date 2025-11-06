package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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

class MainActivity : AppCompatActivity() {

    private lateinit var tvUserInfo: TextView
    private lateinit var tvWelcome: TextView
    private lateinit var btnVerProductos: Button
    private lateinit var btnVerCarrito: Button
    private lateinit var btnMisPedidos: Button
    private lateinit var btnCerrarSesion: Button
    private lateinit var btnAdmin: Button
    private lateinit var auth: FirebaseAuth
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "🚀 MainActivity onCreate iniciado")

        try {
            setContentView(R.layout.activity_main)
            Log.d(TAG, "✅ Layout activity_main_simple cargado exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR cargando layout: ${e.message}", e)
            showToast("Error cargando la aplicación")
            crearLayoutMinimoExtremo()
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
            val sessionEmail = SessionManager.getUsername(this)

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
            Log.d(TAG, "✅ MainActivity configurada exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR configurando MainActivity: ${e.message}", e)
            showToast("Error configurando la aplicación")
            crearLayoutMinimoExtremo()
        }
    }

    private fun initializeViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        tvUserInfo = findViewById(R.id.tvUserInfo)
        btnVerProductos = findViewById(R.id.btnVerProductos)
        btnVerCarrito = findViewById(R.id.btnVerCarrito)
        btnMisPedidos = findViewById(R.id.btnMisPedidos)
        btnCerrarSesion = findViewById(R.id.btnLogout)
        btnAdmin = findViewById(R.id.btnAdmin)

        Log.d(TAG, "✅ Vistas inicializadas correctamente")
    }

    private fun setupUserInfo() {
        coroutineScope.launch {
            try {
                val currentUser = auth.currentUser
                val email = currentUser?.email ?: SessionManager.getUsername(this@MainActivity) ?: ""

                if (email.isNotEmpty()) {
                    // ✅ Verificar usuario tanto en SQLite (DatabaseHelper) como en Firebase
                    val usuarioLocal = withContext(Dispatchers.IO) {
                        Usuario.obtenerUsuarioPorNombre(this@MainActivity, email)
                    }

                    val rol = if (usuarioLocal != null) {
                        // Usuario encontrado en base local
                        usuarioLocal.rol
                    } else {
                        // Si no existe localmente, usar rol por defecto
                        "cliente"
                    }

                    // Configurar interfaz según el rol
                    configurarInterfazSegunRol(rol, email)

                    if (usuarioLocal != null) {
                        Log.d(TAG, "✅ Usuario encontrado en SQLite: ${usuarioLocal.username} - Rol: $rol")
                    } else {
                        Log.w(TAG, "⚠️ Usuario no encontrado en SQLite, usando rol por defecto")
                    }
                } else {
                    tvUserInfo.text = "No se pudo cargar la información del usuario"
                    Log.w(TAG, "⚠️ No se pudo obtener email del usuario")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR cargando información del usuario: ${e.message}", e)
                // Fallback: mostrar información básica desde la sesión
                try {
                    val sessionEmail = SessionManager.getUsername(this@MainActivity)
                    val sessionRole = SessionManager.getUserRole(this@MainActivity)
                    if (!sessionEmail.isNullOrEmpty()) {
                        configurarInterfazSegunRol(sessionRole ?: "cliente", sessionEmail)
                    } else {
                        configurarInterfazSegunRol("cliente", "Usuario")
                    }
                } catch (e2: Exception) {
                    tvUserInfo.text = "Error cargando información"
                }
            }
        }
    }

    private fun configurarInterfazSegunRol(rol: String, email: String) {
        runOnUiThread {
            val userInfo = "Usuario: $email\nRol: $rol"
            tvUserInfo.text = userInfo

            // Mostrar u ocultar botón de administrador según el rol
            if (rol.lowercase() == "admin") {
                btnAdmin.visibility = View.VISIBLE
                tvWelcome.text = "¡Bienvenido Administrador!"
            } else {
                btnAdmin.visibility = View.GONE
                tvWelcome.text = "¡Bienvenido Cliente!"
            }
        }
    }

    private fun setupClickListeners() {
        // 🔹 Botón Ver Productos
        btnVerProductos.setOnClickListener {
            try {
                Log.d(TAG, "🖱️ Clic en Ver Productos")
                abrirProductos()
            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR navegando a productos: ${e.message}", e)
                showToast("Error abriendo productos")
            }
        }

        // 🔹 Botón Ver Carrito
        btnVerCarrito.setOnClickListener {
            try {
                Log.d(TAG, "🖱️ Clic en Ver Carrito")
                abrirCarrito()
            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR en botón carrito: ${e.message}", e)
                showToast("Error abriendo carrito")
            }
        }

        // 🔹 Botón Mis Pedidos
        btnMisPedidos.setOnClickListener {
            try {
                Log.d(TAG, "🖱️ Clic en Mis Pedidos")
                abrirMisPedidos()
            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR en botón pedidos: ${e.message}", e)
                showToast("Error abriendo pedidos")
            }
        }

        // 🔹 Botón Panel Administrador (solo visible para admins)
        btnAdmin.setOnClickListener {
            try {
                Log.d(TAG, "🖱️ Clic en Panel Administrador")
                abrirPanelAdmin()
            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR en botón admin: ${e.message}", e)
                showToast("Error abriendo panel de administrador")
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
     * 🔹 Método para abrir productos
     */
    private fun abrirProductos() {
        try {
            Log.d(TAG, "🔄 Abriendo productos...")

            // TODO: Implementar Activity de productos
            showToast("Funcionalidad de productos en desarrollo")
            Log.d(TAG, "📦 Abriendo productos (pendiente de implementar)")

            // Ejemplo de cómo sería cuando implementes ProductsActivity:
            // val intent = Intent(this, ProductsActivity::class.java)
            // startActivity(intent)

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR abriendo productos: ${e.message}", e)
            showToast("No se pudo abrir la lista de productos")
        }
    }

    /**
     * 🔹 Método para abrir el carrito
     */
    private fun abrirCarrito() {
        try {
            Log.d(TAG, "🔄 Abriendo carrito...")

            // TODO: Implementar Activity de carrito
            showToast("Funcionalidad de carrito en desarrollo")
            Log.d(TAG, "🛒 Abriendo carrito (pendiente de implementar)")

            // Ejemplo de cómo sería cuando implementes CarritoActivity:
            // val intent = Intent(this, CarritoActivity::class.java)
            // startActivity(intent)

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR abriendo carrito: ${e.message}", e)
            showToast("Error abriendo carrito")
        }
    }

    /**
     * 🔹 Método para abrir mis pedidos
     */
    private fun abrirMisPedidos() {
        try {
            Log.d(TAG, "🔄 Abriendo mis pedidos...")

            // TODO: Implementar Activity de pedidos
            showToast("Funcionalidad de pedidos en desarrollo")
            Log.d(TAG, "📋 Abriendo mis pedidos (pendiente de implementar)")

            // Ejemplo de cómo sería cuando implementes PedidosActivity:
            // val intent = Intent(this, PedidosActivity::class.java)
            // startActivity(intent)

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR abriendo pedidos: ${e.message}", e)
            showToast("Error abriendo pedidos")
        }
    }

    /**
     * 🔹 Método para abrir panel de administrador
     */
    private fun abrirPanelAdmin() {
        try {
            Log.d(TAG, "🔄 Abriendo panel de administrador...")

            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
            Log.d(TAG, "✅ AdminActivity iniciada")

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR abriendo panel admin: ${e.message}", e)
            showToast("Error: No se pudo abrir el panel de administrador")
        }
    }

    private fun cerrarSesion() {
        coroutineScope.launch {
            try {
                Log.d(TAG, "🔒 Cerrando sesión...")
                auth.signOut()
                SessionManager.logout(this@MainActivity)
                SessionManager.clearSession(this@MainActivity)
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

    private fun crearLayoutMinimoExtremo() {
        try {
            Log.d(TAG, "🔄 Creando layout mínimo extremo...")

            val textView = TextView(this).apply {
                text = "Sesión Activa\n(Pantalla básica)"
                textSize = 16f
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
            Log.d(TAG, "✅ Layout mínimo extremo creado")

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
            moveTaskToBack(true)
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR en onBackPressed: ${e.message}", e)
            super.onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "📱 MainActivity onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "📱 MainActivity onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "📱 MainActivity onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "📱 MainActivity onDestroy")
    }
}