package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.fragments.CarritoFragment
import com.example.myapplication.fragments.ProductosFragment
import com.example.myapplication.fragments.homeFragment
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeFirebase()
        validateSession()
        initializeViews()
        setupUserInfo()
        setupClickListeners()
    }

    private fun initializeFirebase() {
        auth = Firebase.auth
    }

    private fun validateSession() {
        val currentUser = auth.currentUser
        val sessionEmail = SessionManager.getUsername(this)

        if (currentUser == null && sessionEmail.isNullOrEmpty()) {
            showToast("Sesión no válida")
            redirectToLogin()
            return
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
    }

    private fun setupUserInfo() {
        coroutineScope.launch {
            try {
                val currentUser = auth.currentUser
                val email = currentUser?.email ?: SessionManager.getUsername(this@MainActivity) ?: ""

                if (email.isNotEmpty()) {
                    val usuarioLocal = withContext(Dispatchers.IO) {
                        Usuario.obtenerUsuarioPorNombre(this@MainActivity, email)
                    }

                    val rol = usuarioLocal?.rol ?: "cliente"
                    configurarInterfazSegunRol(rol, email)

                } else {
                    tvUserInfo.text = "Error cargando información"
                }
            } catch (e: Exception) {
                handleUserInfoError()
            }
        }
    }

    private fun handleUserInfoError() {
        val sessionEmail = SessionManager.getUsername(this)
        val sessionRole = SessionManager.getUserRole(this)

        if (!sessionEmail.isNullOrEmpty()) {
            configurarInterfazSegunRol(sessionRole ?: "cliente", sessionEmail)
        } else {
            tvUserInfo.text = "Error cargando información"
        }
    }

    private fun configurarInterfazSegunRol(rol: String, email: String) {
        runOnUiThread {
            val userInfo = "Usuario: $email\nRol: $rol"
            tvUserInfo.text = userInfo

            if (rol.equals("admin", ignoreCase = true)) {
                btnAdmin.visibility = android.view.View.VISIBLE
                tvWelcome.text = "¡Bienvenido Administrador!"
            } else {
                btnAdmin.visibility = android.view.View.GONE
                tvWelcome.text = "¡Bienvenido!"
            }
        }
    }

    private fun setupClickListeners() {
        btnVerProductos.setOnClickListener {
            abrirProductos()
        }

        btnVerCarrito.setOnClickListener {
            abrirCarrito()
        }

        btnMisPedidos.setOnClickListener {
            abrirMisPedidos()
        }

        btnAdmin.setOnClickListener {
            abrirPanelAdmin()
        }

        btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }
    }

    private fun abrirProductos() {
        try {
            val fragment = ProductosFragment()
            loadFragment(fragment, "ProductosFragment")
        } catch (e: Exception) {
            showToast("Error abriendo productos")
        }
    }

    private fun abrirCarrito() {
        try {
            val fragment = CarritoFragment()
            loadFragment(fragment, "CarritoFragment")
        } catch (e: Exception) {
            showToast("Error abriendo carrito")
        }
    }

    private fun abrirMisPedidos() {
        try {
            val fragment = homeFragment()
            loadFragment(fragment, "homeFragment")
        } catch (e: Exception) {
            showToast("Error abriendo pedidos")
        }
    }

    private fun loadFragment(fragment: Fragment, tag: String) {
        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .addToBackStack(tag)
                .commit()
        } catch (e: Exception) {
            showToast("Error cargando la pantalla")
        }
    }

    private fun abrirPanelAdmin() {
        try {
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            showToast("Error abriendo panel de administrador")
        }
    }

    private fun cerrarSesion() {
        coroutineScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    auth.signOut()
                    SessionManager.logout(this@MainActivity)
                    SessionManager.clearSession(this@MainActivity)
                }
                redirectToLogin()
            } catch (e: Exception) {
                redirectToLogin()
            }
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            moveTaskToBack(true)
        }
    }
}