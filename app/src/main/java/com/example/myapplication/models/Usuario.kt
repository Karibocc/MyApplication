package com.example.myapplication.models

import android.content.Context
import android.database.Cursor
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.activities.AdminActivity
import com.example.myapplication.activities.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

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

        // ✅ Verificación de email
        fun esEmailValido(email: String): Boolean {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        // ✅ Hashear contraseñas con SHA-256
        private fun hashPassword(password: String): String {
            return try {
                val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
                bytes.joinToString("") { "%02x".format(it) }
            } catch (e: Exception) {
                Log.e("Usuario", "Error al generar hash: ${e.message}")
                password
            }
        }

        // ===================== REGISTRAR USUARIO =====================
        fun registrarUsuario(context: Context, nuevoUsuario: Usuario): Boolean {
            val dbHelper = DatabaseHelper(context)

            // Verificar si el usuario ya existe
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

        // ===================== VALIDAR LOGIN =====================
        fun validarLogin(context: Context, username: String, password: String): Boolean {
            val dbHelper = DatabaseHelper(context)
            return dbHelper.validarUsuario(username, password)
        }

        // ===================== VERIFICAR SI USUARIO EXISTE =====================
        fun usuarioExiste(context: Context, username: String): Boolean {
            val dbHelper = DatabaseHelper(context)
            return dbHelper.usuarioExiste(username)
        }

        // ===================== OBTENER USUARIO POR NOMBRE =====================
        fun obtenerUsuarioPorNombre(context: Context, username: String): Usuario? {
            val dbHelper = DatabaseHelper(context)
            var cursor: Cursor? = null
            return try {
                cursor = dbHelper.obtenerUsuarioPorNombre(username)
                if (cursor.moveToFirst()) {
                    val dbUsername = cursor.getString(cursor.getColumnIndexOrThrow("username"))
                    val password = cursor.getString(cursor.getColumnIndexOrThrow("password"))
                    val rol = cursor.getString(cursor.getColumnIndexOrThrow("rol"))
                    Usuario(dbUsername, password, rol, rol.equals("admin", ignoreCase = true))
                } else null
            } catch (e: Exception) {
                Log.e("Usuario", "Error obteniendo usuario: ${e.message}")
                null
            } finally {
                cursor?.close()
            }
        }

        suspend fun obtenerUsuarioPorNombreSuspend(context: Context, username: String): Usuario? {
            return withContext(Dispatchers.IO) {
                obtenerUsuarioPorNombre(context, username)
            }
        }

        // ===================== OBTENER ROL =====================
        fun obtenerRol(context: Context, username: String): String {
            val usuario = obtenerUsuarioPorNombre(context, username)
            return usuario?.rol ?: "Desconocido"
        }

        // ===================== OBTENER ACTIVIDAD SEGÚN ROL =====================
        fun obtenerActividadSegunRol(context: Context, username: String): Class<*> {
            val usuario = obtenerUsuarioPorNombre(context, username)
            return when (usuario?.rol?.lowercase()) {
                "admin" -> AdminActivity::class.java
                "cliente" -> MainActivity::class.java
                else -> MainActivity::class.java
            }
        }

        // ===================== OBTENER TODOS LOS USUARIOS =====================
        fun obtenerTodosLosUsuarios(context: Context): List<Usuario> {
            val dbHelper = DatabaseHelper(context)
            val usuarios = mutableListOf<Usuario>()
            var cursor: Cursor? = null
            try {
                cursor = dbHelper.obtenerTodosLosUsuarios()
                while (cursor.moveToNext()) {
                    val username = cursor.getString(cursor.getColumnIndexOrThrow("username"))
                    val password = cursor.getString(cursor.getColumnIndexOrThrow("password"))
                    val rol = cursor.getString(cursor.getColumnIndexOrThrow("rol"))
                    usuarios.add(Usuario(username, password, rol, rol.equals("admin", true)))
                }
            } catch (e: Exception) {
                Log.e("Usuario", "Error obteniendo usuarios: ${e.message}")
            } finally {
                cursor?.close()
            }
            return usuarios
        }
    }
}
