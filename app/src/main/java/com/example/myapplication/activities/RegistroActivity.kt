package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.managers.SessionManager
import com.example.myapplication.models.Usuario
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

class RegistroActivity : AppCompatActivity() {

    private lateinit var btnRegistrar: Button
    private lateinit var etUsuario: EditText
    private lateinit var etPassword: EditText
    private lateinit var switchRol: SwitchMaterial
    private lateinit var auth: FirebaseAuth
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    companion object {
        private const val TAG = "RegistroActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        auth = Firebase.auth

        btnRegistrar = findViewById(R.id.btnRegistrar)
        etUsuario = findViewById(R.id.etUsuario)
        etPassword = findViewById(R.id.etPassword)
        switchRol = findViewById(R.id.switchRol)

        btnRegistrar.setOnClickListener {
            registrarUsuario()
        }
    }

    // ==================================================================
    // MÉTODO PRINCIPAL DE REGISTRO
    // ==================================================================

    private fun registrarUsuario() {
        val username = etUsuario.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Usuario.esEmailValido(username)) {
            etUsuario.error = "Formato de email invalido"
            return
        }

        val usernameNormalizado = username.lowercase()
        val rol = if (switchRol.isChecked) "admin" else "cliente"

        Log.d(TAG, "Intentando registrar usuario: $usernameNormalizado")

        btnRegistrar.isEnabled = false
        btnRegistrar.text = "Registrando..."

        coroutineScope.launch {
            try {
                val usuarioExiste = withContext(Dispatchers.IO) {
                    Usuario.usuarioExiste(this@RegistroActivity, usernameNormalizado)
                }

                if (usuarioExiste) {
                    Log.d(TAG, "Usuario ya existe en SQLite, procediendo a redirigir...")
                    mostrarExitoYRedirigir(usernameNormalizado, esRegistroNuevo = false)
                    return@launch
                }

                Log.d(TAG, "Creando usuario en Firebase...")
                val authResult = withContext(Dispatchers.IO) {
                    try {
                        auth.createUserWithEmailAndPassword(usernameNormalizado, password).await()
                    } catch (e: Exception) {
                        throw e
                    }
                }

                if (authResult.user != null) {
                    Log.d(TAG, "Usuario creado en Firebase Auth")

                    val registroExitoso = withContext(Dispatchers.IO) {
                        try {
                            val resultado = Usuario.registrarUsuarioDesdeStrings(
                                this@RegistroActivity,
                                usernameNormalizado,
                                password,
                                rol
                            )

                            if (resultado) {
                                Log.d(TAG, "Metodo principal exitoso")
                                true
                            } else {
                                Log.d(TAG, "Metodo principal fallo, verificando si se guardo...")
                                val usuarioGuardado = Usuario.obtenerUsuarioPorNombre(this@RegistroActivity, usernameNormalizado)
                                usuarioGuardado != null
                            }

                        } catch (e: Exception) {
                            Log.e(TAG, "Error en guardar en SQLite: ${e.message}")
                            false
                        }
                    }

                    val usuarioVerificado = withContext(Dispatchers.IO) {
                        Usuario.obtenerUsuarioPorNombre(this@RegistroActivity, usernameNormalizado) != null
                    }

                    Log.d(TAG, "Verificacion final - Usuario en BD: $usuarioVerificado")

                    if (usuarioVerificado) {
                        Log.d(TAG, "REGISTRO COMPLETADO EXITOSAMENTE")

                        sincronizarConFirestore(usernameNormalizado, rol)
                        mostrarExitoYRedirigir(usernameNormalizado, esRegistroNuevo = true)

                    } else {
                        Log.e(TAG, "Error: No se pudo verificar el usuario en SQLite")

                        limpiarUsuarioFirebase()

                        Toast.makeText(
                            this@RegistroActivity,
                            "Error al guardar en base de datos local",
                            Toast.LENGTH_SHORT
                        ).show()
                        resetBotonRegistro()
                    }

                } else {
                    throw Exception("Usuario de Firebase nulo despues del registro")
                }

            } catch (e: Exception) {
                manejarErrorFirebase(e)
            }
        }
    }

    // ==================================================================
    // MÉTODOS DE SINCRONIZACIÓN CON FIRESTORE
    // ==================================================================

    private fun sincronizarConFirestore(email: String, rol: String) {
        try {
            val firestore = Firebase.firestore
            val datosUsuario = hashMapOf(
                "email" to email,
                "rol" to rol,
                "fechaRegistro" to System.currentTimeMillis()
            )
            firestore.collection("usuarios")
                .document(email)
                .set(datosUsuario)
                .addOnSuccessListener {
                    Log.d(TAG, "Usuario sincronizado con Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al guardar en Firestore: ${e.message}", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error en Firestore: ${e.message}", e)
        }
    }

    // ==================================================================
    // MÉTODOS DE MANEJO DE ERRORES
    // ==================================================================

    private suspend fun limpiarUsuarioFirebase() {
        try {
            auth.currentUser?.delete()?.await()
            auth.signOut()
            Log.d(TAG, "Usuario eliminado de Firebase despues del error")
        } catch (e: Exception) {
            Log.e(TAG, "Error limpiando usuario de Firebase", e)
        }
    }

    private fun manejarErrorFirebase(e: Exception) {
        val errorMessage = when (e) {
            is FirebaseAuthUserCollisionException -> "El email ya esta registrado en Firebase. Puede iniciar sesion directamente."
            is FirebaseAuthInvalidCredentialsException -> "Formato de email invalido"
            else -> e.message ?: "Error al registrar en Firebase"
        }

        Log.e(TAG, "Error en registro: $errorMessage", e)

        if (e is FirebaseAuthUserCollisionException) {
            val username = etUsuario.text.toString().trim().lowercase()
            mostrarExitoYRedirigir(username, esRegistroNuevo = false)
        } else {
            Toast.makeText(this@RegistroActivity, errorMessage, Toast.LENGTH_LONG).show()
            resetBotonRegistro()
        }
    }

    // ==================================================================
    // MÉTODOS DE INTERFAZ DE USUARIO
    // ==================================================================

    private fun mostrarExitoYRedirigir(email: String, esRegistroNuevo: Boolean) {
        runOnUiThread {
            try {
                try {
                    auth.signOut()
                    Log.d(TAG, "Sesion de Firebase cerrada")
                } catch (e: Exception) {
                    Log.e(TAG, "Error cerrando sesion de Firebase: ${e.message}")
                }

                SessionManager.logout(this@RegistroActivity)

                val mensaje = if (esRegistroNuevo) {
                    "Registro exitoso. Ahora puede iniciar sesion"
                } else {
                    "El usuario ya estaba registrado. Puede iniciar sesion"
                }

                Toast.makeText(this@RegistroActivity, mensaje, Toast.LENGTH_LONG).show()

                if (esRegistroNuevo) {
                    limpiarCampos()
                }

                resetBotonRegistro()

                val intent = Intent(this@RegistroActivity, LoginActivity::class.java).apply {
                    putExtra("email_registrado", email)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()

            } catch (e: Exception) {
                Log.e(TAG, "Error en mostrarExitoYRedirigir: ${e.message}", e)
                val intent = Intent(this@RegistroActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun resetBotonRegistro() {
        runOnUiThread {
            btnRegistrar.isEnabled = true
            btnRegistrar.text = "Registrar"
        }
    }

    private fun limpiarCampos() {
        try {
            etUsuario.text.clear()
            etPassword.text.clear()
            switchRol.isChecked = false
            Log.d(TAG, "Campos limpiados")
        } catch (e: Exception) {
            Log.e(TAG, "Error limpiando campos", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}