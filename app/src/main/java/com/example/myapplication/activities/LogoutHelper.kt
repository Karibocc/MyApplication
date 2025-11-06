package com.example.myapplication.activities

import android.app.Activity
import android.content.Intent
import com.example.myapplication.activities.LoginActivity
import com.example.myapplication.managers.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth

object LogoutHelper {

    /**
     * Cierra la sesión del usuario, limpia las preferencias
     * y redirige al LoginActivity.
     */
    fun cerrarSesion(activity: Activity) {
        // Limpiar sesión local usando SessionManager
        SessionManager.logout(activity)

        // También limpiar usando clearSession para compatibilidad
        SessionManager.clearSession(activity)

        // Cerrar sesión de Firebase si está activa
        cerrarSesionFirebase()

        // Redirigir al LoginActivity
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
        activity.finish()
    }

    /**
     * Cierra solo la sesión de Firebase Auth
     */
    private fun cerrarSesionFirebase() {
        try {
            val auth = FirebaseAuth.getInstance()
            auth.signOut()
        } catch (e: Exception) {
            // Silenciar error si Firebase no está configurado o no hay sesión
        }
    }

    /**
     * Cierra sesión y muestra un mensaje de despedida
     */
    fun cerrarSesionConMensaje(activity: Activity, mensaje: String? = null) {
        // Limpiar sesiones
        SessionManager.logout(activity)
        SessionManager.clearSession(activity)
        cerrarSesionFirebase()

        // Redirigir al LoginActivity
        val intent = Intent(activity, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Opcional: pasar mensaje como extra
            mensaje?.let { putExtra("mensaje_logout", it) }
        }
        activity.startActivity(intent)
        activity.finish()
    }

    /**
     * Cierra sesión sin redirigir (útil para servicios o background)
     */
    fun cerrarSesionSilenciosa(context: android.content.Context) {
        SessionManager.logout(context)
        SessionManager.clearSession(context)
        cerrarSesionFirebase()
    }

    /**
     * Verifica si hay alguna sesión activa (local o Firebase)
     */
    fun haySesionActiva(context: android.content.Context): Boolean {
        return SessionManager.isUserLoggedIn(context) ||
                FirebaseAuth.getInstance().currentUser != null
    }
}