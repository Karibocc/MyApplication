package com.example.myapplication.models

import android.content.Context
import android.database.Cursor
import android.widget.Toast
import com.example.myapplication.database.DatabaseHelper

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

        fun obtenerUsuarioPorNombre(context: Context, username: String): Usuario? {
            val dbHelper = DatabaseHelper(context)
            val cursor: Cursor = dbHelper.obtenerUsuarioPorNombre(username)

            return if (cursor.moveToFirst()) {
                val password = cursor.getString(cursor.getColumnIndexOrThrow("password"))
                val rol = cursor.getString(cursor.getColumnIndexOrThrow("rol"))
                cursor.close()
                Usuario(username, password, rol, rol == "admin")
            } else {
                cursor.close()
                null
            }
        }

        fun usuarioExiste(context: Context, username: String): Boolean {
            return obtenerUsuarioPorNombre(context, username) != null
        }

        fun obtenerRol(context: Context, username: String): String {
            val usuario = obtenerUsuarioPorNombre(context, username)
            return usuario?.rol ?: "Desconocido"
        }
    }
}
