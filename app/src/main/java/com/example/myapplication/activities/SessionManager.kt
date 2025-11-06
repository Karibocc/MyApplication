package com.example.myapplication.managers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object SessionManager {

    private const val PREFS_NAME = "user_session"
    private const val KEY_USERNAME = "username"
    private const val KEY_ROL = "rol"

    // Guardar sesión de usuario
    fun saveUserSession(context: Context, username: String, rol: String) {
        try {
            val prefs: SharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString(KEY_USERNAME, username)
            editor.putString(KEY_ROL, rol)
            editor.apply()
            Log.d("SessionManager", "Sesión guardada: $username - $rol")
        } catch (e: Exception) {
            Log.e("SessionManager", "Error guardando sesión", e)
        }
    }

    // Obtener email/username del usuario actual
    fun getCurrentUserEmail(context: Context): String? {
        return try {
            val prefs: SharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.getString(KEY_USERNAME, null)
        } catch (e: Exception) {
            Log.e("SessionManager", "Error obteniendo sesión", e)
            null
        }
    }

    // Obtener rol del usuario actual
    fun getCurrentUserRole(context: Context): String? {
        return try {
            val prefs: SharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.getString(KEY_ROL, null)
        } catch (e: Exception) {
            Log.e("SessionManager", "Error obteniendo rol", e)
            null
        }
    }

    // Cerrar sesión
    fun logout(context: Context) {
        try {
            val prefs: SharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.clear()
            editor.apply()
            Log.d("SessionManager", "Sesión cerrada")
        } catch (e: Exception) {
            Log.e("SessionManager", "Error cerrando sesión", e)
        }
    }

    // Verifica si hay sesión activa
    fun isLoggedIn(context: Context): Boolean {
        return getCurrentUserEmail(context) != null
    }

    // ===================== MÉTODOS NUEVOS PARA COMPATIBILIDAD =====================

    // Alias para getCurrentUserEmail - mantiene compatibilidad
    fun getUsername(context: Context): String? {
        return getCurrentUserEmail(context)
    }

    // Alias para getCurrentUserRole - mantiene compatibilidad
    fun getUserRole(context: Context): String? {
        return getCurrentUserRole(context)
    }

    // Alias para isLoggedIn - mantiene compatibilidad
    fun isUserLoggedIn(context: Context): Boolean {
        return isLoggedIn(context)
    }

    // Método para limpiar sesión (alias de logout)
    fun clearSession(context: Context) {
        logout(context)
    }
}