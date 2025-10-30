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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegistroActivity : AppCompatActivity() {

    private lateinit var btnRegistrar: Button
    private lateinit var etUsuario: EditText
    private lateinit var etPassword: EditText
    private lateinit var switchRol: SwitchMaterial
    private lateinit var auth: FirebaseAuth

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

        Log.d("RegistroActivity", "🔐 Registrando usuario: $usernameNormalizado")

        // Deshabilitar botón durante el registro
        btnRegistrar.isEnabled = false
        btnRegistrar.text = "Registrando..."

        // ✅ PRIMERO: Crear usuario en Firebase Auth
        auth.createUserWithEmailAndPassword(usernameNormalizado, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("RegistroActivity", "✅ Usuario creado en Firebase")

                    // ✅ SEGUNDO: Guardar en SQLite
                    val nuevoUsuario = Usuario(usernameNormalizado, password, rol)
                    val registroExitoso = Usuario.registrarUsuario(this, nuevoUsuario)

                    if (registroExitoso) {
                        Log.d("RegistroActivity", "✅ Usuario guardado en SQLite")

                        // Verificar que realmente se guardó
                        val usuarioGuardado = Usuario.obtenerUsuarioPorNombre(this, usernameNormalizado)
                        if (usuarioGuardado != null) {
                            Log.d("RegistroActivity", "✅ Verificación exitosa: ${usuarioGuardado.username}")
                        }

                        // ✅ CORREGIDO: CERRAR SESIÓN DE FIREBASE DESPUÉS DEL REGISTRO
                        try {
                            auth.signOut()
                            Log.d("RegistroActivity", "✅ Sesión de Firebase cerrada después del registro")
                        } catch (e: Exception) {
                            Log.e("RegistroActivity", "⚠️ Error cerrando sesión de Firebase: ${e.message}")
                        }

                        // ✅ CORREGIDO: LIMPIAR CUALQUIER SESIÓN PREVIA
                        SessionManager.logout(this)

                        Toast.makeText(this, "✅ Registro exitoso. Ahora puede iniciar sesión", Toast.LENGTH_LONG).show()

                        // Limpiar campos después del registro exitoso
                        limpiarCampos()

                        // ✅ CORREGIDO: REDIRIGIR AL LOGIN SIN SESIÓN ACTIVA
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.putExtra("email_registrado", usernameNormalizado)

                        // ✅ NUEVO: Flags para limpiar el stack de actividades
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                        startActivity(intent)
                        finish()

                    } else {
                        Log.e("RegistroActivity", "❌ Error al guardar en SQLite")

                        // ✅ NUEVO: Si falla SQLite, también cerrar sesión de Firebase
                        try {
                            auth.signOut()
                        } catch (e: Exception) {
                            Log.e("RegistroActivity", "Error cerrando sesión", e)
                        }

                        Toast.makeText(this, "Error al guardar en base de datos local", Toast.LENGTH_SHORT).show()
                        btnRegistrar.isEnabled = true
                        btnRegistrar.text = "Registrar"
                    }
                } else {
                    Log.e("RegistroActivity", "❌ Error en Firebase: ${task.exception?.message}")
                    val errorMessage = when {
                        task.exception?.message?.contains("email address is already") == true ->
                            "El email ya está registrado en Firebase"
                        task.exception?.message?.contains("badly formatted") == true ->
                            "Formato de email inválido"
                        else -> task.exception?.message ?: "Error al registrar en Firebase"
                    }
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
            Log.d("RegistroActivity", "✅ Campos limpiados después del registro exitoso")
        } catch (e: Exception) {
            Log.e("RegistroActivity", "Error limpiando campos", e)
        }
    }

    companion object {
        private const val TAG = "RegistroActivity"
    }
}


