package com.example.myapplication.models

import android.content.Context
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.myapplication.activities.AdminActivity
import com.example.myapplication.activities.MainActivity
import com.example.myapplication.database.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Usuario(
    val id: Int = 0,
    val username: String,
    val password: String,
    val salt: String = "",
    val rol: String,
    val email: String? = null,
    val activo: Boolean = true,
    val isAdmin: Boolean = false
) {
    fun isValid(): Boolean {
        return username.isNotBlank() && password.isNotBlank()
    }

    // ===================== GUARDAR USUARIO =====================
    fun guardar(context: Context): Boolean {
        return registrarUsuario(context, this)
    }

    companion object {

        // ===================== VALIDAR EMAIL =====================
        fun esEmailValido(email: String): Boolean {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        // ===================== REGISTRAR USUARIO =====================
        fun registrarUsuario(context: Context, nuevoUsuario: Usuario): Boolean {
            val dbHelper = DatabaseHelper(context)

            // 🔥 AGREGAR LOGS PARA DEBUG
            Log.d("REGISTRO_USUARIO", "=== INICIANDO REGISTRO EN BD ===")
            Log.d("REGISTRO_USUARIO", "Usuario: ${nuevoUsuario.username}")
            Log.d("REGISTRO_USUARIO", "Rol: ${nuevoUsuario.rol}")

            if (usuarioExiste(context, nuevoUsuario.username)) {
                Log.e("REGISTRO_USUARIO", "Usuario ya existe en BD")
                Toast.makeText(context, "El usuario ya existe", Toast.LENGTH_SHORT).show()
                return false
            }

            Log.d("REGISTRO_USUARIO", "Usuario no existe, procediendo a insertar")
            val resultado = dbHelper.insertarUsuario(
                nuevoUsuario.username,
                nuevoUsuario.password,
                nuevoUsuario.rol
            )

            Log.d("REGISTRO_USUARIO", "Resultado de inserción en BD: $resultado")

            return if (resultado != -1L) {
                Log.i("REGISTRO_USUARIO", "✅ Inserción exitosa en BD, ID: $resultado")
                Toast.makeText(context, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show()
                true
            } else {
                Log.e("REGISTRO_USUARIO", "❌ Error en inserción BD, resultado: $resultado")
                Toast.makeText(context, "Error al registrar usuario", Toast.LENGTH_SHORT).show()
                false
            }
        }

        // ===================== REGISTRAR USUARIO DESDE STRINGS (NUEVA FUNCIÓN) =====================
        fun registrarUsuarioDesdeStrings(context: Context, username: String, password: String, rol: String): Boolean {
            val nuevoUsuario = Usuario(
                username = username,
                password = password,
                rol = rol
            )
            return registrarUsuario(context, nuevoUsuario)
        }

        // ===================== REGISTRAR USUARIO SIN TOAST (PARA CONTROL MANUAL) =====================
        fun registrarUsuarioSilencioso(context: Context, nuevoUsuario: Usuario): Boolean {
            val dbHelper = DatabaseHelper(context)

            if (usuarioExiste(context, nuevoUsuario.username)) {
                return false
            }

            val resultado = dbHelper.insertarUsuario(
                nuevoUsuario.username,
                nuevoUsuario.password,
                nuevoUsuario.rol
            )

            return resultado != -1L
        }

        // ===================== REGISTRAR USUARIO DESDE STRINGS SIN TOAST =====================
        fun registrarUsuarioDesdeStringsSilencioso(context: Context, username: String, password: String, rol: String): Boolean {
            val nuevoUsuario = Usuario(
                username = username,
                password = password,
                rol = rol
            )
            return registrarUsuarioSilencioso(context, nuevoUsuario)
        }

        // ===================== VALIDAR LOGIN =====================
        fun validarLogin(context: Context, username: String, password: String): Boolean {
            val dbHelper = DatabaseHelper(context)
            return dbHelper.validarUsuario(username, password)
        }

        // ===================== VERIFICAR EXISTENCIA DE USUARIO =====================
        fun usuarioExiste(context: Context, username: String): Boolean {
            val dbHelper = DatabaseHelper(context)
            return dbHelper.usuarioExiste(username)
        }

        // ===================== OBTENER USUARIO POR NOMBRE =====================
        fun obtenerUsuarioPorNombre(context: Context, username: String): Usuario? {
            val dbHelper = DatabaseHelper(context)
            return try {
                val dbUsuario = dbHelper.obtenerUsuarioPorNombre(username)
                dbUsuario?.let {
                    Usuario(
                        id = it.id,
                        username = it.username,
                        password = it.password,
                        salt = it.salt,
                        rol = it.rol,
                        isAdmin = it.rol.equals("admin", ignoreCase = true)
                    )
                }
            } catch (e: Exception) {
                Log.e("Usuario", "Error obteniendo usuario: ${e.message}")
                null
            }
        }

        suspend fun obtenerUsuarioPorNombreSuspend(context: Context, username: String): Usuario? {
            return withContext(Dispatchers.IO) {
                obtenerUsuarioPorNombre(context, username)
            }
        }

        // ===================== OBTENER USUARIO POR ID =====================
        fun obtenerUsuarioPorId(context: Context, usuarioId: Int): Usuario? {
            val dbHelper = DatabaseHelper(context)
            return try {
                val dbUsuario = dbHelper.obtenerUsuarioPorId(usuarioId)
                dbUsuario?.let {
                    Usuario(
                        id = it.id,
                        username = it.username,
                        password = it.password,
                        salt = it.salt,
                        rol = it.rol,
                        isAdmin = it.rol.equals("admin", ignoreCase = true)
                    )
                }
            } catch (e: Exception) {
                Log.e("Usuario", "Error obteniendo usuario por ID: ${e.message}")
                null
            }
        }

        // ===================== OBTENER ROL =====================
        fun obtenerRol(context: Context, username: String): String {
            val dbHelper = DatabaseHelper(context)
            return dbHelper.obtenerRol(username) ?: "Desconocido"
        }

        // ===================== OBTENER ACTIVIDAD SEGÚN ROL =====================
        fun obtenerActividadSegunRol(context: Context, username: String): Class<*> {
            val rol = obtenerRol(context, username)
            return when (rol.lowercase()) {
                "admin" -> AdminActivity::class.java
                "cliente" -> MainActivity::class.java
                else -> MainActivity::class.java
            }
        }

        // ===================== OBTENER TODOS LOS USUARIOS =====================
        fun obtenerTodosLosUsuarios(context: Context): List<Usuario> {
            val dbHelper = DatabaseHelper(context)
            val usuarios = mutableListOf<Usuario>()
            try {
                val cursor = dbHelper.obtenerTodosLosUsuarios()
                cursor?.use { c ->
                    val idIndex = c.getColumnIndex(DatabaseHelper.COLUMN_USUARIO_ID)
                    val usernameIndex = c.getColumnIndex(DatabaseHelper.COLUMN_USERNAME)
                    val rolIndex = c.getColumnIndex(DatabaseHelper.COLUMN_ROL)

                    while (c.moveToNext()) {
                        val id = if (idIndex >= 0) c.getInt(idIndex) else 0
                        val username = if (usernameIndex >= 0) c.getString(usernameIndex) else ""
                        val rol = if (rolIndex >= 0) c.getString(rolIndex) else "cliente"

                        if (username.isNotBlank()) {
                            usuarios.add(
                                Usuario(
                                    id = id,
                                    username = username,
                                    password = "", // No obtenemos la contraseña por seguridad
                                    rol = rol,
                                    isAdmin = rol.equals("admin", ignoreCase = true)
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Usuario", "Error obteniendo usuarios: ${e.message}")
            }
            return usuarios
        }

        // ===================== ACTUALIZAR USUARIO =====================
        fun actualizarUsuario(context: Context, username: String, nuevoRol: String): Boolean {
            val dbHelper = DatabaseHelper(context)
            return try {
                val resultado = dbHelper.actualizarUsuario(username, nuevoRol)
                resultado > 0
            } catch (e: Exception) {
                Log.e("Usuario", "Error actualizando usuario: ${e.message}")
                false
            }
        }

        // ===================== ELIMINAR USUARIO =====================
        fun eliminarUsuario(context: Context, username: String): Boolean {
            val dbHelper = DatabaseHelper(context)
            return try {
                val resultado = dbHelper.eliminarUsuario(username)
                resultado > 0
            } catch (e: Exception) {
                Log.e("Usuario", "Error eliminando usuario: ${e.message}")
                false
            }
        }

        // ===================== CAMBIAR CONTRASEÑA =====================
        fun cambiarPassword(context: Context, username: String, nuevaPassword: String): Boolean {
            val dbHelper = DatabaseHelper(context)
            return try {
                val resultado = dbHelper.cambiarPassword(username, nuevaPassword)
                resultado > 0
            } catch (e: Exception) {
                Log.e("Usuario", "Error cambiando contraseña: ${e.message}")
                false
            }
        }

        // ===================== OBTENER CANTIDAD DE USUARIOS =====================
        fun obtenerCantidadUsuarios(context: Context): Int {
            val dbHelper = DatabaseHelper(context)
            return try {
                dbHelper.obtenerCantidadUsuarios()
            } catch (e: Exception) {
                Log.e("Usuario", "Error obteniendo cantidad de usuarios: ${e.message}")
                0
            }
        }

        // ===================== NUEVO: ACTUALIZAR ROL DE USUARIO =====================
        fun actualizarRolUsuario(context: Context, username: String, nuevoRol: String): Boolean {
            val dbHelper = DatabaseHelper(context)
            return try {
                Log.d("ACTUALIZAR_ROL", "🔄 Actualizando rol de usuario: $username a $nuevoRol")
                val resultado = dbHelper.actualizarRolUsuario(username, nuevoRol)

                if (resultado > 0) {
                    Log.i("ACTUALIZAR_ROL", "✅ Rol actualizado exitosamente para: $username")
                    true
                } else {
                    Log.e("ACTUALIZAR_ROL", "❌ No se pudo actualizar el rol para: $username")
                    false
                }
            } catch (e: Exception) {
                Log.e("ACTUALIZAR_ROL", "💥 Error actualizando rol: ${e.message}", e)
                false
            }
        }

        // ===================== NUEVO: ELIMINAR USUARIO CON LOGS =====================
        fun eliminarUsuarioConLogs(context: Context, username: String): Boolean {
            val dbHelper = DatabaseHelper(context)
            return try {
                Log.d("ELIMINAR_USUARIO", "🗑️ Intentando eliminar usuario: $username")

                // Verificar que el usuario existe antes de eliminar
                if (!usuarioExiste(context, username)) {
                    Log.e("ELIMINAR_USUARIO", "❌ Usuario no existe: $username")
                    return false
                }

                val resultado = dbHelper.eliminarUsuario(username)

                if (resultado > 0) {
                    Log.i("ELIMINAR_USUARIO", "✅ Usuario eliminado exitosamente: $username")
                    true
                } else {
                    Log.e("ELIMINAR_USUARIO", "❌ No se pudo eliminar usuario: $username")
                    false
                }
            } catch (e: Exception) {
                Log.e("ELIMINAR_USUARIO", "💥 Error eliminando usuario: ${e.message}", e)
                false
            }
        }

        // ===================== NUEVO: OBTENER USUARIOS POR ROL =====================
        fun obtenerUsuariosPorRol(context: Context, rol: String): List<Usuario> {
            val dbHelper = DatabaseHelper(context)
            val usuarios = mutableListOf<Usuario>()
            try {
                val cursor = dbHelper.obtenerUsuariosPorRol(rol)
                cursor?.use { c ->
                    val idIndex = c.getColumnIndex(DatabaseHelper.COLUMN_USUARIO_ID)
                    val usernameIndex = c.getColumnIndex(DatabaseHelper.COLUMN_USERNAME)
                    val rolIndex = c.getColumnIndex(DatabaseHelper.COLUMN_ROL)

                    while (c.moveToNext()) {
                        val id = if (idIndex >= 0) c.getInt(idIndex) else 0
                        val username = if (usernameIndex >= 0) c.getString(usernameIndex) else ""
                        val usuarioRol = if (rolIndex >= 0) c.getString(rolIndex) else rol

                        if (username.isNotBlank()) {
                            usuarios.add(
                                Usuario(
                                    id = id,
                                    username = username,
                                    password = "",
                                    rol = usuarioRol,
                                    isAdmin = usuarioRol.equals("admin", ignoreCase = true)
                                )
                            )
                        }
                    }
                }
                Log.d("OBTENER_USUARIOS_ROL", "✅ Usuarios con rol '$rol': ${usuarios.size}")
            } catch (e: Exception) {
                Log.e("OBTENER_USUARIOS_ROL", "❌ Error obteniendo usuarios por rol: ${e.message}")
            }
            return usuarios
        }

        // ===================== NUEVO: VERIFICAR SI USUARIO ES ADMIN =====================
        fun esUsuarioAdmin(context: Context, username: String): Boolean {
            val rol = obtenerRol(context, username)
            return rol.equals("admin", ignoreCase = true)
        }

        // ===================== NUEVO: CONTAR USUARIOS POR ROL =====================
        fun contarUsuariosPorRol(context: Context, rol: String): Int {
            val dbHelper = DatabaseHelper(context)
            return try {
                val cantidad = dbHelper.contarUsuariosPorRol(rol)
                Log.d("CONTAR_USUARIOS", "👥 Usuarios con rol '$rol': $cantidad")
                cantidad
            } catch (e: Exception) {
                Log.e("CONTAR_USUARIOS", "❌ Error contando usuarios por rol: ${e.message}")
                0
            }
        }
    }
}