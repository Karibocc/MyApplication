package com.example.myapplication.managers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object SessionManager {

    private const val PREFS_NAME = "user_session"
    private const val KEY_USERNAME = "username"
    private const val KEY_ROL = "rol"

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

    fun isLoggedIn(context: Context): Boolean {
        return getCurrentUserEmail(context) != null
    }

    fun getUsername(context: Context): String? {
        return getCurrentUserEmail(context)
    }

    fun getUserRole(context: Context): String? {
        return getCurrentUserRole(context)
    }

    fun isUserLoggedIn(context: Context): Boolean {
        return isLoggedIn(context)
    }

    fun clearSession(context: Context) {
        logout(context)
    }
}