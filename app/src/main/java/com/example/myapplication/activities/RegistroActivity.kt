package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.managers.SessionManager
import com.example.myapplication.models.Usuario
import com.google.android.material.textfield.TextInputEditText
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
    private lateinit var btnCancelar: Button
    private lateinit var etUsername: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var spRol: Spinner
    private lateinit var auth: FirebaseAuth
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    companion object {
        private const val TAG = "RegistroActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        auth = Firebase.auth
        enlazarVistas()
        configurarSpinner()
        configurarBotones()
    }

    private fun enlazarVistas() {
        try {
            btnRegistrar = findViewById(R.id.btnGuardar)
            btnCancelar = findViewById(R.id.btnCancelar)
            etUsername = findViewById(R.id.etUsername)
            etEmail = findViewById(R.id.etEmail)
            etPassword = findViewById(R.id.etPassword)
            spRol = findViewById(R.id.spRol)
        } catch (e: Exception) {
            Log.e(TAG, "Error enlazando vistas", e)
            Toast.makeText(this, "Error configurando la interfaz", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configurarSpinner() {
        try {
            val roles = arrayOf("Administrador", "Usuario", "Invitado")

            val adapter = object : ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                roles
            ) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    val textView = view.findViewById<TextView>(android.R.id.text1)
                    textView.setTextColor(getColor(android.R.color.black))
                    textView.textSize = 16f
                    return view
                }

                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getDropDownView(position, convertView, parent)
                    val textView = view.findViewById<TextView>(android.R.id.text1)
                    textView.setTextColor(getColor(android.R.color.black))
                    textView.textSize = 16f
                    textView.setBackgroundColor(getColor(android.R.color.white))
                    return view
                }
            }

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spRol.adapter = adapter
        } catch (e: Exception) {
            Log.e(TAG, "Error configurando spinner", e)
        }
    }

    private fun configurarBotones() {
        btnCancelar.setOnClickListener {
            finish()
        }

        btnRegistrar.setOnClickListener {
            registrarUsuario()
        }
    }

    private fun registrarUsuario() {
        try {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val rolSeleccionado = spRol.selectedItem as? String ?: "Usuario"

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                return
            }

            if (password.length < 6) {
                Toast.makeText(this, "Contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return
            }

            if (!Usuario.esEmailValido(email)) {
                etEmail.error = "Formato de email inválido"
                return
            }

            val emailNormalizado = email.lowercase()
            val rol = when (rolSeleccionado.toLowerCase()) {
                "administrador" -> "admin"
                "usuario" -> "cliente"
                "invitado" -> "invitado"
                else -> "cliente"
            }

            btnRegistrar.isEnabled = false
            btnRegistrar.text = "Registrando..."

            coroutineScope.launch {
                try {
                    Log.d(TAG, "Datos registro: Email=$emailNormalizado, Username=$username, Rol=$rol")

                    // Verificar si el correo ya existe
                    val correoExiste = withContext(Dispatchers.IO) {
                        verificarCorreoExistente(emailNormalizado)
                    }

                    if (correoExiste) {
                        Log.d(TAG, "Correo ya existe en BD, redirigiendo...")
                        mostrarExitoYRedirigir(emailNormalizado, false)
                        return@launch
                    }

                    // Registrar en Firebase
                    Log.d(TAG, "Registrando en Firebase...")
                    val authResult = withContext(Dispatchers.IO) {
                        auth.createUserWithEmailAndPassword(emailNormalizado, password).await()
                    }

                    if (authResult.user != null) {
                        val userId = authResult.user!!.uid
                        Log.d(TAG, "Usuario registrado en Firebase, ID: $userId")

                        // Registrar en SQLite - FORZAR GUARDADO
                        Log.d(TAG, "Registrando en SQLite...")
                        val resultadoSQLite = withContext(Dispatchers.IO) {
                            guardarUsuarioEnSQLite(username, password, rol, emailNormalizado)
                        }

                        if (resultadoSQLite) {
                            Log.d(TAG, "Usuario guardado correctamente en SQLite")
                            sincronizarConFirestore(userId, username, emailNormalizado, rol)
                            mostrarExitoYRedirigir(emailNormalizado, true)
                        } else {
                            Log.e(TAG, "Error: No se pudo guardar en SQLite")
                            limpiarUsuarioFirebase()
                            runOnUiThread {
                                Toast.makeText(this@RegistroActivity, "Error al guardar en base de datos local", Toast.LENGTH_LONG).show()
                                resetBotonRegistro()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error en proceso de registro: ${e.message}", e)
                    manejarErrorFirebase(e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en registrarUsuario", e)
            Toast.makeText(this, "Error inesperado", Toast.LENGTH_SHORT).show()
            resetBotonRegistro()
        }
    }

    private suspend fun guardarUsuarioEnSQLite(username: String, password: String, rol: String, email: String): Boolean {
        return try {
            val dbHelper = DatabaseHelper(this@RegistroActivity)

            // Intentar insertar el usuario
            val resultado = dbHelper.insertarUsuario(username, password, rol, email)
            Log.d(TAG, "Resultado insert SQLite: $resultado")

            if (resultado == -1L) {
                Log.e(TAG, "Error en insertarUsuario, intentando método alternativo...")
                // Intentar método alternativo si el primero falla
                return insertarUsuarioAlternativo(dbHelper, username, password, rol, email)
            }

            // Verificar que realmente se guardó
            val guardadoCorrectamente = verificarUsuarioGuardadoPorEmail(email)
            if (!guardadoCorrectamente) {
                Log.e(TAG, "Usuario no encontrado después de insertar, intentando método alternativo...")
                return insertarUsuarioAlternativo(dbHelper, username, password, rol, email)
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error guardando usuario en SQLite: ${e.message}", e)
            false
        }
    }

    private fun insertarUsuarioAlternativo(dbHelper: DatabaseHelper, username: String, password: String, rol: String, email: String): Boolean {
        return try {
            // Intentar con el método sin email primero
            val resultadoSinEmail = dbHelper.insertarUsuario(username, password, rol)
            Log.d(TAG, "Resultado insert sin email: $resultadoSinEmail")

            if (resultadoSinEmail != -1L) {
                // Si se guardó sin email, actualizar para agregar el email
                val cursor = dbHelper.obtenerTodosLosUsuarios()
                var usuarioId: Int? = null

                if (cursor != null && cursor.moveToLast()) {
                    do {
                        val user = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME))
                        if (user.equals(username, ignoreCase = true)) {
                            usuarioId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USUARIO_ID))
                            break
                        }
                    } while (cursor.moveToPrevious())
                    cursor.close()
                }

                if (usuarioId != null) {
                    val resultadoUpdate = dbHelper.actualizarUsuario(username, rol, email)
                    Log.d(TAG, "Resultado update con email: $resultadoUpdate")
                    resultadoUpdate > 0
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en método alternativo: ${e.message}", e)
            false
        }
    }

    private suspend fun verificarCorreoExistente(email: String): Boolean {
        return try {
            val dbHelper = DatabaseHelper(this@RegistroActivity)
            val cursor = dbHelper.obtenerTodosLosUsuarios()
            var existe = false

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        val emailDb = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL))

                        if (emailDb != null && emailDb.equals(email, ignoreCase = true)) {
                            existe = true
                            Log.d(TAG, "Correo ya existe en BD: $emailDb")
                            break
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error leyendo cursor", e)
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
            existe
        } catch (e: Exception) {
            Log.e(TAG, "Error verificando correo existente: ${e.message}", e)
            false
        }
    }

    private suspend fun verificarUsuarioGuardadoPorEmail(email: String): Boolean {
        return try {
            val dbHelper = DatabaseHelper(this@RegistroActivity)
            val cursor = dbHelper.obtenerTodosLosUsuarios()
            var encontrado = false

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        val emailDb = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL))
                        if (emailDb != null && emailDb.equals(email, ignoreCase = true)) {
                            encontrado = true
                            Log.d(TAG, "Usuario verificado en BD por correo: $emailDb")
                            break
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error verificando usuario guardado", e)
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
            encontrado
        } catch (e: Exception) {
            Log.e(TAG, "Error en verificarUsuarioGuardadoPorEmail: ${e.message}", e)
            false
        }
    }

    private fun sincronizarConFirestore(userId: String, username: String, email: String, rol: String) {
        try {
            val firestore = Firebase.firestore
            val datosUsuario = hashMapOf(
                "userId" to userId,
                "username" to username,
                "email" to email,
                "rol" to rol,
                "fechaRegistro" to System.currentTimeMillis()
            )

            firestore.collection("usuarios")
                .document(userId)
                .set(datosUsuario)
                .addOnSuccessListener {
                    Log.d(TAG, "Usuario guardado en Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error Firestore", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error Firestore", e)
        }
    }

    private suspend fun limpiarUsuarioFirebase() {
        try {
            auth.currentUser?.delete()?.await()
            auth.signOut()
            Log.d(TAG, "Usuario limpiado de Firebase")
        } catch (e: Exception) {
            Log.e(TAG, "Error limpiando Firebase", e)
        }
    }

    private fun manejarErrorFirebase(e: Exception) {
        val errorMessage = when (e) {
            is FirebaseAuthUserCollisionException -> "El email ya está registrado"
            is FirebaseAuthInvalidCredentialsException -> "Formato de email inválido"
            else -> "Error al registrar: ${e.message}"
        }

        if (e is FirebaseAuthUserCollisionException) {
            val email = etEmail.text.toString().trim().lowercase()
            mostrarExitoYRedirigir(email, false)
        } else {
            runOnUiThread {
                Toast.makeText(this@RegistroActivity, errorMessage, Toast.LENGTH_LONG).show()
                resetBotonRegistro()
            }
        }
    }

    private fun mostrarExitoYRedirigir(email: String, esRegistroNuevo: Boolean) {
        runOnUiThread {
            try {
                SessionManager.logout(this@RegistroActivity)

                val mensaje = if (esRegistroNuevo) {
                    "Registro exitoso"
                } else {
                    "Usuario ya registrado"
                }

                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()

                if (esRegistroNuevo) {
                    limpiarCampos()
                }

                resetBotonRegistro()

                val intent = Intent(this, LoginActivity::class.java).apply {
                    putExtra("email_registrado", email)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Log.e(TAG, "Error en redirección", e)
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun resetBotonRegistro() {
        runOnUiThread {
            btnRegistrar.isEnabled = true
            btnRegistrar.text = "Guardar"
        }
    }

    private fun limpiarCampos() {
        try {
            etUsername.text?.clear()
            etEmail.text?.clear()
            etPassword.text?.clear()
            spRol.setSelection(0)
        } catch (e: Exception) {
            Log.e(TAG, "Error limpiando campos", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}