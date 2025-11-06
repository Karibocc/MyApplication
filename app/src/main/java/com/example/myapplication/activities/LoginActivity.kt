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
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    private lateinit var dbHelper: DatabaseHelper
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 🔥 FORZAR LOG INICIAL
        println("🔥 === LOGIN ACTIVITY CREADA ===")
        Log.e(TAG, "🔥 === LOGIN ACTIVITY CREADA ===") // Usar Log.e para que sea más visible

        // Inicializamos vistas
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)

        // Inicializamos base de datos
        dbHelper = DatabaseHelper(this)

        // Listeners
        btnLogin.setOnClickListener {
            println("🔥 BOTÓN LOGIN PRESIONADO")
            Log.e(TAG, "🔥 BOTÓN LOGIN PRESIONADO")
            loginUser()
        }
        btnRegister.setOnClickListener { navigateToRegister() }
    }

    override fun onStart() {
        super.onStart()
        println("🔥 LOGIN ACTIVITY ONSTART")
        Log.e(TAG, "🔥 LOGIN ACTIVITY ONSTART")
        // Verificar si ya hay una sesión activa al iniciar la actividad
        checkActiveSession()
    }

    private fun checkActiveSession() {
        println("🔥 CHECKING ACTIVE SESSION")
        Log.e(TAG, "🔥 CHECKING ACTIVE SESSION")
        if (SessionManager.isUserLoggedIn(this)) {
            coroutineScope.launch {
                try {
                    val username = SessionManager.getUsername(this@LoginActivity)
                    val rol = SessionManager.getUserRole(this@LoginActivity)

                    println("🔥 SESIÓN ACTIVA ENCONTRADA: $username, Rol: $rol")
                    Log.e(TAG, "🔥 SESIÓN ACTIVA ENCONTRADA: $username, Rol: $rol")

                    redirigirSegunRol(username, rol, desdeSesionActiva = true)

                } catch (e: Exception) {
                    println("❌ ERROR SESIÓN ACTIVA: ${e.message}")
                    Log.e(TAG, "❌ ERROR SESIÓN ACTIVA: ${e.message}")
                    SessionManager.clearSession(this@LoginActivity)
                }
            }
        } else {
            println("🔥 NO HAY SESIÓN ACTIVA")
            Log.e(TAG, "🔥 NO HAY SESIÓN ACTIVA")
        }
    }

    private fun loginUser() {
        val username = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // 🔥 LOG INICIAL FORZADO
        println("🎯 === LOGIN INICIADO - Usuario: $username ===")
        Log.e(TAG, "🎯 === LOGIN INICIADO - Usuario: $username ===")

        // Validaciones
        if (username.isEmpty()) {
            etEmail.error = "Usuario requerido"
            println("❌ USUARIO VACÍO")
            Log.e(TAG, "❌ USUARIO VACÍO")
            return
        }

        if (password.isEmpty() || password.length < 6) {
            etPassword.error = "Contraseña inválida"
            println("❌ CONTRASEÑA INVÁLIDA")
            Log.e(TAG, "❌ CONTRASEÑA INVÁLIDA")
            return
        }

        if (!checkNetworkConnection()) {
            showToast("Sin conexión a internet")
            println("❌ SIN CONEXIÓN")
            Log.e(TAG, "❌ SIN CONEXIÓN")
            return
        }

        btnLogin.isEnabled = false
        btnLogin.text = "Iniciando sesión..."

        println("🔥 INICIANDO CORRUTINA DE LOGIN")
        Log.e(TAG, "🔥 INICIANDO CORRUTINA DE LOGIN")

        coroutineScope.launch {
            try {
                // 🔥 DIAGNÓSTICO MÁXIMO
                println("🔍 INICIANDO DIAGNÓSTICO EN CORRUTINA")
                Log.e(TAG, "🔍 INICIANDO DIAGNÓSTICO EN CORRUTINA")

                // MÉTODO 1: Verificar con DatabaseHelper
                println("📊 EJECUTANDO DatabaseHelper.usuarioExiste...")
                Log.e(TAG, "📊 EJECUTANDO DatabaseHelper.usuarioExiste...")

                val usuarioExisteDB = withContext(Dispatchers.IO) {
                    try {
                        val resultado = dbHelper.usuarioExiste(username)
                        println("📊 RESULTADO DatabaseHelper.usuarioExiste('$username') = $resultado")
                        Log.e(TAG, "📊 RESULTADO DatabaseHelper.usuarioExiste('$username') = $resultado")
                        resultado
                    } catch (e: Exception) {
                        println("❌ ERROR DatabaseHelper.usuarioExiste: ${e.message}")
                        Log.e(TAG, "❌ ERROR DatabaseHelper.usuarioExiste: ${e.message}")
                        false
                    }
                }

                // MÉTODO 2: Verificar con modelo Usuario
                println("📊 EJECUTANDO Usuario.usuarioExiste...")
                Log.e(TAG, "📊 EJECUTANDO Usuario.usuarioExiste...")

                val usuarioExisteModelo = withContext(Dispatchers.IO) {
                    try {
                        val resultado = Usuario.usuarioExiste(this@LoginActivity, username)
                        println("📊 RESULTADO Usuario.usuarioExiste('$username') = $resultado")
                        Log.e(TAG, "📊 RESULTADO Usuario.usuarioExiste('$username') = $resultado")
                        resultado
                    } catch (e: Exception) {
                        println("❌ ERROR Usuario.usuarioExiste: ${e.message}")
                        Log.e(TAG, "❌ ERROR Usuario.usuarioExiste: ${e.message}")
                        false
                    }
                }

                // MÉTODO 3: Obtener todos los usuarios
                println("📊 EJECUTANDO Usuario.obtenerTodosLosUsuarios...")
                Log.e(TAG, "📊 EJECUTANDO Usuario.obtenerTodosLosUsuarios...")

                val todosUsuarios = withContext(Dispatchers.IO) {
                    try {
                        val usuarios = Usuario.obtenerTodosLosUsuarios(this@LoginActivity)
                        println("📊 RESULTADO Usuario.obtenerTodosLosUsuarios() = ${usuarios.size} usuarios")
                        Log.e(TAG, "📊 RESULTADO Usuario.obtenerTodosLosUsuarios() = ${usuarios.size} usuarios")

                        // Mostrar cada usuario
                        usuarios.forEachIndexed { index, usuario ->
                            println("👤 USUARIO $index: '${usuario.username}' - Rol: ${usuario.rol}")
                            Log.e(TAG, "👤 USUARIO $index: '${usuario.username}' - Rol: ${usuario.rol}")
                        }

                        usuarios
                    } catch (e: Exception) {
                        println("❌ ERROR obteniendo todos los usuarios: ${e.message}")
                        Log.e(TAG, "❌ ERROR obteniendo todos los usuarios: ${e.message}")
                        emptyList()
                    }
                }

                // RESUMEN FINAL
                println("📊 RESUMEN FINAL:")
                println("📊 - Usuario buscado: '$username'")
                println("📊 - Existe en DatabaseHelper: $usuarioExisteDB")
                println("📊 - Existe en Modelo Usuario: $usuarioExisteModelo")
                println("📊 - Total usuarios en sistema: ${todosUsuarios.size}")

                Log.e(TAG, "📊 RESUMEN FINAL:")
                Log.e(TAG, "📊 - Usuario buscado: '$username'")
                Log.e(TAG, "📊 - Existe en DatabaseHelper: $usuarioExisteDB")
                Log.e(TAG, "📊 - Existe en Modelo Usuario: $usuarioExisteModelo")
                Log.e(TAG, "📊 - Total usuarios en sistema: ${todosUsuarios.size}")

                val usuarioExiste = usuarioExisteDB || usuarioExisteModelo

                if (!usuarioExiste) {
                    println("❌ USUARIO NO ENCONTRADO EN NINGÚN MÉTODO: '$username'")
                    Log.e(TAG, "❌ USUARIO NO ENCONTRADO EN NINGÚN MÉTODO: '$username'")
                    showToast("Usuario '$username' no encontrado. Regístrese primero.")
                    resetLoginButton()
                    return@launch
                }

                println("✅ USUARIO ENCONTRADO, VALIDANDO CREDENCIALES...")
                Log.e(TAG, "✅ USUARIO ENCONTRADO, VALIDANDO CREDENCIALES...")

                // Continuar con validación de contraseña...
                var passwordValida = withContext(Dispatchers.IO) {
                    try {
                        val resultado = dbHelper.validarUsuario(username, password)
                        println("🔐 DatabaseHelper.validarUsuario('$username') = $resultado")
                        Log.e(TAG, "🔐 DatabaseHelper.validarUsuario('$username') = $resultado")
                        resultado
                    } catch (e: Exception) {
                        println("❌ ERROR validando contraseña: ${e.message}")
                        Log.e(TAG, "❌ ERROR validando contraseña: ${e.message}")
                        false
                    }
                }

                if (!passwordValida) {
                    showToast("Usuario o contraseña incorrectos")
                    resetLoginButton()
                    return@launch
                }

                // Obtener rol
                val rol = withContext(Dispatchers.IO) {
                    try {
                        val resultado = dbHelper.obtenerRol(username) ?: "cliente"
                        println("🎭 Rol obtenido: '$resultado'")
                        Log.e(TAG, "🎭 Rol obtenido: '$resultado'")
                        resultado
                    } catch (e: Exception) {
                        println("❌ ERROR obteniendo rol: ${e.message}")
                        Log.e(TAG, "❌ ERROR obteniendo rol: ${e.message}")
                        "cliente"
                    }
                }

                println("✅ LOGIN EXITOSO - Redirigiendo...")
                Log.e(TAG, "✅ LOGIN EXITOSO - Redirigiendo...")

                SessionManager.saveUserSession(this@LoginActivity, username, rol)
                redirigirSegunRol(username, rol, desdeSesionActiva = false)

            } catch (e: Exception) {
                println("💥 ERROR CRÍTICO EN CORRUTINA: ${e.message}")
                Log.e(TAG, "💥 ERROR CRÍTICO EN CORRUTINA: ${e.message}")
                showToast("Error: ${e.message}")
                resetLoginButton()
            }
        }
    }

    private fun redirigirSegunRol(username: String?, rol: String?, desdeSesionActiva: Boolean = false) {
        try {
            println("🔄 REDIRIGIENDO - Usuario: $username, Rol: $rol")
            Log.e(TAG, "🔄 REDIRIGIENDO - Usuario: $username, Rol: $rol")

            val intent = when (rol?.lowercase()) {
                "admin", "administrador" -> Intent(this, AdminActivity::class.java)
                else -> Intent(this, MainActivity::class.java)
            }

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

        } catch (e: Exception) {
            println("💥 ERROR REDIRIGIENDO: ${e.message}")
            Log.e(TAG, "💥 ERROR REDIRIGIENDO: ${e.message}")
            resetLoginButton()
            showToast("Error: ${e.message}")
        }
    }

    private fun resetLoginButton() {
        runOnUiThread {
            btnLogin.isEnabled = true
            btnLogin.text = "Iniciar Sesión"
        }
    }

    private fun checkNetworkConnection(): Boolean {
        return try {
            val runtime = Runtime.getRuntime()
            val ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
            val exitValue = ipProcess.waitFor()
            exitValue == 0
        } catch (e: Exception) {
            false
        }
    }

    private fun navigateToRegister() {
        startActivity(Intent(this, RegistroActivity::class.java))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            dbHelper.close()
        } catch (e: Exception) {
            println("❌ ERROR CERRANDO DB: ${e.message}")
        }
    }
}


