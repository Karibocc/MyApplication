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

    /**
     * Verifica si el usuario tiene sesión activa y retorna un booleano.
     * No redirige automáticamente.
     */
    fun tieneSesionActiva(context: Context): Boolean {
        return SessionManager.isUserLoggedIn(context)
    }

    /**
     * Obtiene el nombre de usuario de la sesión activa.
     * Retorna null si no hay sesión activa.
     */
    fun obtenerUsuarioActual(context: Context): String? {
        return SessionManager.getUsername(context)
    }

    /**
     * Obtiene el rol del usuario de la sesión activa.
     * Retorna null si no hay sesión activa.
     */
    fun obtenerRolActual(context: Context): String? {
        return SessionManager.getUserRole(context)
    }

    /**
     * Verifica si el usuario actual es administrador.
     * Retorna false si no hay sesión activa o no es admin.
     */
    fun esUsuarioAdmin(context: Context): Boolean {
        val rol = obtenerRolActual(context)
        return rol?.equals("admin", ignoreCase = true) ?: false
    }

    /**
     * Verifica si el usuario actual es cliente.
     * Retorna false si no hay sesión activa o no es cliente.
     */
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