package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication.R
import com.example.myapplication.managers.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var navController: NavController
    private lateinit var emergencyLayout: LinearLayout
    private val auth = Firebase.auth

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "🚀 === MAINACTIVITY ONCREATE INICIADO ===")

        try {
            enableEdgeToEdge()
            setContentView(R.layout.activity_main)
            Log.d(TAG, "✅ Layout cargado exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR CRÍTICO en setContentView: ${e.message}", e)
            showToast("Error cargando la aplicación")
            createMinimalLayout()
            return
        }

        // Inicializar vistas básicas
        try {
            bottomNav = findViewById(R.id.bottom_nav)
            emergencyLayout = findViewById(R.id.emergency_layout)
            Log.d(TAG, "✅ Vistas básicas inicializadas")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR inicializando vistas: ${e.message}", e)
            createMinimalLayout()
            return
        }

        // Validación de sesión
        try {
            Log.d(TAG, "🔐 Validando sesión...")
            val currentUser = auth.currentUser
            val sessionEmail = SessionManager.getCurrentUserEmail(this)

            if (currentUser == null || sessionEmail.isNullOrEmpty()) {
                Log.w(TAG, "🚨 Sesión inválida - Firebase: ${currentUser?.email}, SessionManager: $sessionEmail")
                showToast("Sesión inválida, redirigiendo al login")
                redirectToLogin()
                return
            }

            Log.d(TAG, "✅ Sesión válida - Usuario: ${currentUser.email}")
            showToast("Bienvenido ${currentUser.email}")

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR en validación de sesión: ${e.message}", e)
            redirectToLogin()
            return
        }

        // ✅ Configurar navegación UNA SOLA VEZ
        setupNavigation()

        Log.d(TAG, "✅ === MAINACTIVITY CONFIGURADO EXITOSAMENTE ===")
    }

    private fun setupNavigation() {
        try {
            Log.d(TAG, "🔄 Configurando navegación...")

            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            if (navHostFragment == null) {
                Log.e(TAG, "❌ NavHostFragment no encontrado")
                showEmergencyScreen("Error de navegación - NavHostFragment no encontrado")
                return
            }

            // Obtener NavController
            try {
                navController = navHostFragment.navController
                Log.d(TAG, "✅ NavController obtenido exitosamente")
            } catch (e: IllegalStateException) {
                if (e.message?.contains("SavedStateProvider") == true) {
                    Log.e(TAG, "❌ ERROR: SavedStateProvider ya registrado - Usando estrategia alternativa", e)
                    return
                } else {
                    throw e
                }
            }

            // Configurar BottomNavigationView
            try {
                bottomNav.setupWithNavController(navController)
                Log.d(TAG, "✅ BottomNavigationView configurado correctamente")
            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR configurando BottomNavigationView: ${e.message}", e)
            }

            // Configurar AppBar
            try {
                val appBarConfiguration = AppBarConfiguration(
                    setOf(
                        R.id.homeFragment,
                        R.id.productsFragment,
                        R.id.carritoFragment,
                        R.id.profileFragment
                    )
                )
                setupActionBarWithNavController(navController, appBarConfiguration)
                Log.d(TAG, "✅ AppBar configurada correctamente")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ AppBar no configurada: ${e.message}")
            }

            // 🔹 Listener de cambio de destino
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.homeFragment -> Log.d(TAG, "📌 Navegando a Home")
                    R.id.productsFragment -> Log.d(TAG, "📌 Navegando a Productos")
                    R.id.carritoFragment -> Log.d(TAG, "📌 Navegando a Carrito")
                    R.id.profileFragment -> Log.d(TAG, "📌 Navegando a Perfil")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR CRÍTICO en setupNavigation: ${e.message}", e)
            showEmergencyScreen("Error crítico en navegación")
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

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "📱 MainActivity onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "📱 MainActivity onDestroy")
    }

    private fun createMinimalLayout() {
        try {
            Log.d(TAG, "🔄 Creando layout mínimo de emergencia...")
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(resources.getColor(R.color.white, theme))
                gravity = android.view.Gravity.CENTER
            }

            val textView = TextView(this).apply {
                text = "Error cargando la aplicación"
                textSize = 18f
                setTextColor(resources.getColor(R.color.black, theme))
            }

            val button = Button(this).apply {
                text = "Cerrar Sesión"
                setOnClickListener {
                    Log.d(TAG, "🖱️ Clic en cerrar sesión desde layout mínimo")
                    cerrarSesion()
                }
            }

            layout.addView(textView)
            layout.addView(button)
            setContentView(layout)
            Log.d(TAG, "✅ Layout mínimo creado exitosamente")

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR CRÍTICO: No se puede crear layout mínimo", e)
            redirectToLogin()
        }
    }

    private fun showEmergencyScreen(message: String) {
        try {
            Log.d(TAG, "🆘 Mostrando pantalla de emergencia: $message")
            val tvEmergencyMessage = findViewById<TextView>(R.id.tv_emergency_message)
            val btnEmergencyLogout = findViewById<Button>(R.id.btn_emergency_logout)

            tvEmergencyMessage.text = message
            emergencyLayout.visibility = View.VISIBLE

            btnEmergencyLogout.setOnClickListener {
                Log.d(TAG, "🖱️ Clic en cerrar sesión desde pantalla de emergencia")
                cerrarSesion()
            }

            bottomNav.visibility = View.GONE
            findViewById<androidx.fragment.app.FragmentContainerView>(R.id.nav_host_fragment)?.visibility = View.GONE

            Log.d(TAG, "✅ Pantalla de emergencia activada")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR en pantalla de emergencia", e)
            createMinimalLayout()
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
            Log.e(TAG, "❌ ERROR redirigiendo a login", e)
            finishAffinity()
        }
    }

    private fun cerrarSesion() {
        try {
            Log.d(TAG, "🔒 Cerrando sesión...")
            auth.signOut()
            SessionManager.logout(this)
            Log.d(TAG, "✅ Sesión cerrada en Firebase y SessionManager")
            redirectToLogin()
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR cerrando sesión", e)
            redirectToLogin()
        }
    }

    private fun showToast(message: String) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            Log.d(TAG, "💬 Toast mostrado: $message")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error mostrando toast: ${e.message}", e)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return try {
            if (::navController.isInitialized) {
                navController.navigateUp() || super.onSupportNavigateUp()
            } else {
                super.onSupportNavigateUp()
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en onSupportNavigateUp: ${e.message}", e)
            super.onSupportNavigateUp()
        }
    }
}
