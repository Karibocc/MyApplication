package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
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

class AdminActivity : AppCompatActivity() {

    private lateinit var tvAdminInfo: TextView
    private lateinit var btnGestionarProductos: Button
    private lateinit var btnGestionarUsuarios: Button
    private lateinit var btnVerReportes: Button
    private lateinit var btnCerrarSesion: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        auth = Firebase.auth

        initializeViews()
        setupAdminInfo()
        setupClickListeners()
    }

    private fun initializeViews() {
        tvAdminInfo = findViewById(R.id.tvAdminInfo)
        btnGestionarProductos = findViewById(R.id.btnGestionarProductos)
        btnGestionarUsuarios = findViewById(R.id.btnGestionarUsuarios)
        btnVerReportes = findViewById(R.id.btnVerReportes)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
    }

    private fun setupAdminInfo() {
        val currentUser = auth.currentUser
        val email = currentUser?.email ?: SessionManager.getCurrentUserEmail(this) ?: ""

        if (email.isNotEmpty()) {
            val usuario = Usuario.obtenerUsuarioPorNombre(this, email)
            val adminInfo = "Administrador: $email\nRol: ${usuario?.rol ?: "admin"}"
            tvAdminInfo.text = adminInfo
        } else {
            tvAdminInfo.text = "No se pudo cargar la información del administrador"
        }
    }

    private fun setupClickListeners() {
        btnGestionarProductos.setOnClickListener {
            // Navegar a la actividad de gestión de productos
            Toast.makeText(this, "Gestión de productos en desarrollo", Toast.LENGTH_SHORT).show()
        }

        btnGestionarUsuarios.setOnClickListener {
            // Navegar a la actividad de gestión de usuarios
            Toast.makeText(this, "Gestión de usuarios en desarrollo", Toast.LENGTH_SHORT).show()
        }

        btnVerReportes.setOnClickListener {
            // Navegar a la actividad de reportes
            Toast.makeText(this, "Reportes en desarrollo", Toast.LENGTH_SHORT).show()
        }

        btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }
    }

    private fun cerrarSesion() {
        auth.signOut()
        SessionManager.logout(this)

        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()

        // Redirigir al login
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    override fun onBackPressed() {
        // Evitar que el administrador regrese al login con back button
        moveTaskToBack(true)
    }
}

