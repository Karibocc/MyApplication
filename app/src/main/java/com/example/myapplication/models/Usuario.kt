package com.example.myapplication.models

import android.content.Context
import android.database.Cursor
import android.util.Patterns
import android.widget.Toast
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.activities.AdminActivity
import com.example.myapplication.activities.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Usuario(
    val username: String,
    val password: String,
    val rol: String,
    val isAdmin: Boolean = false
) {
    fun isValid(): Boolean {
        return username.isNotBlank() && password.isNotBlank()
    }

    companion object {

        // ✅ Función mejorada para verificar email
        fun esEmailValido(email: String): Boolean {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        fun registrarUsuario(context: Context, nuevoUsuario: Usuario): Boolean {
            val dbHelper = DatabaseHelper(context)

            // Verificar si ya existe
            if (usuarioExiste(context, nuevoUsuario.username)) {
                Toast.makeText(context, "El usuario ya existe", Toast.LENGTH_SHORT).show()
                return false
            }

            val resultado = dbHelper.insertarUsuario(
                nuevoUsuario.username,
                nuevoUsuario.password,
                nuevoUsuario.rol
            )

            return if (resultado != -1L) {
                Toast.makeText(context, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show()
                true
            } else {
                Toast.makeText(context, "Error al registrar usuario", Toast.LENGTH_SHORT).show()
                false
            }
        }

        fun validarLogin(context: Context, username: String, password: String): Boolean {
            val dbHelper = DatabaseHelper(context)
            return dbHelper.validarUsuario(username, password)
        }

        // ✅ Versión mejorada con manejo seguro de recursos
        fun obtenerUsuarioPorNombre(context: Context, username: String): Usuario? {
            val dbHelper = DatabaseHelper(context)
            var cursor: Cursor? = null

            return try {
                cursor = dbHelper.obtenerUsuarioPorNombre(username)

                if (cursor.moveToFirst()) {
                    val password = cursor.getString(cursor.getColumnIndexOrThrow("password"))
                    val rol = cursor.getString(cursor.getColumnIndexOrThrow("rol"))
                    Usuario(username, password, rol, rol.equals("admin", ignoreCase = true))
                } else {
                    null
                }
            } catch (e: Exception) {
                android.util.Log.e("Usuario", "Error obteniendo usuario: ${e.message}")
                null
            } finally {
                cursor?.close()
                // No cerramos dbHelper aquí para no interferir con la lógica existente
            }
        }

        // ✅ Versión suspend para corrutinas (nueva función)
        suspend fun obtenerUsuarioPorNombreSuspend(context: Context, username: String): Usuario? {
            return withContext(Dispatchers.IO) {
                obtenerUsuarioPorNombre(context, username)
            }
        }

        fun usuarioExiste(context: Context, username: String): Boolean {
            return obtenerUsuarioPorNombre(context, username) != null
        }

        fun obtenerRol(context: Context, username: String): String {
            val usuario = obtenerUsuarioPorNombre(context, username)
            return usuario?.rol ?: "Desconocido"
        }

        // ✅ Nueva función agregada: obtiene el nombre de la actividad según el rol
        fun obtenerActividadSegunRol(context: Context, username: String): Class<*> {
            val usuario = obtenerUsuarioPorNombre(context, username)
            return when (usuario?.rol?.lowercase()) {
                "admin" -> AdminActivity::class.java
                "cliente" -> MainActivity::class.java
                else -> MainActivity::class.java // fallback seguro
            }
        }

        // ✅ Nueva función: obtener todos los usuarios (útil para admin)
        fun obtenerTodosLosUsuarios(context: Context): List<Usuario> {
            val dbHelper = DatabaseHelper(context)
            val usuarios = mutableListOf<Usuario>()
            var cursor: Cursor? = null

            try {
                // Asumiendo que tienes este método en DatabaseHelper
                cursor = dbHelper.obtenerTodosLosUsuarios()
                while (cursor != null && cursor.moveToNext()) {
                    val username = cursor.getString(cursor.getColumnIndexOrThrow("username"))
                    val password = cursor.getString(cursor.getColumnIndexOrThrow("password"))
                    val rol = cursor.getString(cursor.getColumnIndexOrThrow("rol"))
                    usuarios.add(Usuario(username, password, rol))
                }
            } catch (e: Exception) {
                android.util.Log.e("Usuario", "Error obteniendo usuarios: ${e.message}")
            } finally {
                cursor?.close()
                // No cerramos dbHelper para mantener compatibilidad
            }

            return usuarios
        }
    }
}
