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
        if (!SessionManager.isLoggedIn(context)) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }
}