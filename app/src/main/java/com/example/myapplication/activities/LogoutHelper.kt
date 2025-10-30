package com.example.myapplication.activities

import android.app.Activity
import android.content.Intent
import com.example.myapplication.activities.LoginActivity
import com.example.myapplication.managers.SessionManager

object LogoutHelper {

    /**
     * Cierra la sesión del usuario, limpia las preferencias
     * y redirige al LoginActivity.
     */
    fun cerrarSesion(activity: Activity) {
        SessionManager.logout(activity)
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
        activity.finish()
    }
}