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

class RegistroActivity : AppCompatActivity() {

    private lateinit var btnRegistrar: Button
    private lateinit var etUsuario: EditText
    private lateinit var etPassword: EditText
    private lateinit var switchRol: SwitchMaterial
    private lateinit var auth: FirebaseAuth

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
            etUsuario.error = "Formato de email inválido"
            return
        }

        val usernameNormalizado = username.lowercase()
        val rol = if (switchRol.isChecked) "admin" else "cliente"

        Log.d(TAG, "🔐 Registrando usuario: $usernameNormalizado")

        btnRegistrar.isEnabled = false
        btnRegistrar.text = "Registrando..."

        // ✅ 1. Crear usuario en Firebase Authentication
        auth.createUserWithEmailAndPassword(usernameNormalizado, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "✅ Usuario creado en Firebase Auth")

                    // ✅ 2. Guardar en SQLite (DatabaseHelper)
                    val nuevoUsuario = Usuario(usernameNormalizado, password, rol)
                    val registroExitoso = Usuario.registrarUsuario(this, nuevoUsuario)

                    if (registroExitoso) {
                        Log.d(TAG, "✅ Usuario guardado en SQLite")

                        // ✅ 3. Sincronizar con Firestore (opcional)
                        val firestore = Firebase.firestore
                        val datosUsuario = hashMapOf(
                            "email" to usernameNormalizado,
                            "rol" to rol,
                            "fechaRegistro" to System.currentTimeMillis()
                        )
                        firestore.collection("usuarios")
                            .document(usernameNormalizado)
                            .set(datosUsuario)
                            .addOnSuccessListener {
                                Log.d(TAG, "✅ Usuario sincronizado con Firestore")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "⚠️ Error al guardar en Firestore: ${e.message}", e)
                            }

                        // ✅ 4. Cerrar sesión de Firebase tras registro
                        try {
                            auth.signOut()
                            Log.d(TAG, "✅ Sesión de Firebase cerrada después del registro")
                        } catch (e: Exception) {
                            Log.e(TAG, "⚠️ Error cerrando sesión de Firebase: ${e.message}")
                        }

                        // ✅ 5. Limpiar sesión previa local
                        SessionManager.logout(this)

                        Toast.makeText(this, "✅ Registro exitoso. Ahora puede iniciar sesión", Toast.LENGTH_LONG).show()

                        limpiarCampos()

                        // ✅ 6. Redirigir al Login
                        val intent = Intent(this, LoginActivity::class.java).apply {
                            putExtra("email_registrado", usernameNormalizado)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()

                    } else {
                        Log.e(TAG, "❌ Error al guardar en SQLite")

                        try {
                            auth.signOut()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error cerrando sesión", e)
                        }

                        Toast.makeText(this, "Error al guardar en base de datos local", Toast.LENGTH_SHORT).show()
                        btnRegistrar.isEnabled = true
                        btnRegistrar.text = "Registrar"
                    }

                } else {
                    // ⚠️ Manejo avanzado de errores de Firebase
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthUserCollisionException -> "El email ya está registrado en Firebase"
                        is FirebaseAuthInvalidCredentialsException -> "Formato de email inválido"
                        else -> task.exception?.message ?: "Error al registrar en Firebase"
                    }

                    Log.e(TAG, "❌ Error en Firebase: $errorMessage", task.exception)
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()

                    btnRegistrar.isEnabled = true
                    btnRegistrar.text = "Registrar"
                }
            }
    }

    private fun limpiarCampos() {
        try {
            etUsuario.text.clear()
            etPassword.text.clear()
            Log.d(TAG, "✅ Campos limpiados después del registro exitoso")
        } catch (e: Exception) {
            Log.e(TAG, "Error limpiando campos", e)
        }
    }
}



