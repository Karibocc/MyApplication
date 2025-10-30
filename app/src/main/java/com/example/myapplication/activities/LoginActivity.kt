package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.managers.SessionManager
import com.example.myapplication.models.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    private lateinit var dbHelper: DatabaseHelper
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializamos vistas
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)

        dbHelper = DatabaseHelper(this)

        // Click listeners
        btnLogin.setOnClickListener { loginUser() }
        btnRegister.setOnClickListener { navigateToRegister() }
    }

    private fun loginUser() {
        val username = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (username.isEmpty()) {
            etEmail.error = "Usuario requerido"
            return
        }

        if (password.isEmpty() || password.length < 6) {
            etPassword.error = "Contraseña inválida"
            return
        }

        if (!checkNetworkConnection()) {
            showToast("Sin conexión a internet")
            return
        }

        btnLogin.isEnabled = false
        btnLogin.text = "Iniciando sesión..."

        coroutineScope.launch {
            try {
                // Verificamos si el usuario existe
                if (!dbHelper.usuarioExiste(username)) {
                    showToast("Usuario no registrado")
                    resetLoginButton()
                    return@launch
                }

                // Validamos credenciales
                val passwordValida = dbHelper.validarUsuario(username, password)
                if (!passwordValida) {
                    showToast("Usuario o contraseña incorrectos")
                    resetLoginButton()
                    return@launch
                }

                // Obtenemos rol
                val cursor = dbHelper.obtenerUsuarioPorNombre(username)
                var rol = "cliente"
                if (cursor.moveToFirst()) {
                    rol = cursor.getString(cursor.getColumnIndexOrThrow("rol"))
                }
                cursor.close()

                // Guardamos sesión
                SessionManager.saveUserSession(this@LoginActivity, username, rol)

                // Navegamos según rol
                val destino = when (rol.lowercase()) {
                    "admin" -> {
                        showToast("Bienvenido administrador")
                        Intent(this@LoginActivity, AdminActivity::class.java)
                    }
                    else -> {
                        showToast("Bienvenido cliente")
                        Intent(this@LoginActivity, MainActivity::class.java)
                    }
                }

                startActivity(destino)
                finish()

            } catch (e: Exception) {
                Log.e(TAG, "Error login", e)
                showToast("Error inesperado")
                resetLoginButton()
            }
        }
    }

    private fun resetLoginButton() {
        btnLogin.isEnabled = true
        btnLogin.text = "Iniciar Sesión"
    }

    private fun checkNetworkConnection(): Boolean {
        // Puedes mantener tu versión original de checkNetworkConnection()
        return true
    }

    private fun navigateToRegister() {
        startActivity(Intent(this, RegistroActivity::class.java))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}





