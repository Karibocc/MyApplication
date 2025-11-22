package com.example.myapplication.activities

import android.content.Context
import android.content.Intent
import com.example.myapplication.activities.LoginActivity
import com.example.myapplication.managers.SessionManager

object SessionValidator {

    /**
     * Verifica si el usuario tiene sesión activa.
     * Si no, redirige al LoginActivity y cierra la actividad actual.
     */
    fun validarSesion(context: Context) {
        // Usamos el método actualizado isUserLoggedIn para compatibilidad
        if (!SessionManager.isUserLoggedIn(context)) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }


    fun tieneSesionActiva(context: Context): Boolean {
        return SessionManager.isUserLoggedIn(context)
    }


    fun obtenerUsuarioActual(context: Context): String? {
        return SessionManager.getUsername(context)
    }


    fun obtenerRolActual(context: Context): String? {
        return SessionManager.getUserRole(context)
    }


    fun esUsuarioAdmin(context: Context): Boolean {
        val rol = obtenerRolActual(context)
        return rol?.equals("admin", ignoreCase = true) ?: false
    }


    fun esUsuarioCliente(context: Context): Boolean {
        val rol = obtenerRolActual(context)
        return rol?.equals("cliente", ignoreCase = true) ?: false
    }

    /**
     * Redirige forzadamente al login cerrando la sesión actual.
     * Útil para casos de sesión inválida o corrupta.
     */
    fun forzarCierreSesion(context: Context) {
        SessionManager.clearSession(context)
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}