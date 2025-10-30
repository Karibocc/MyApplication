package com.example.myapplication.managers

import android.content.Context

object SessionManager {
    private const val PREF_NAME = "UserSession"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    private const val KEY_USER_EMAIL = "userEmail"
    private const val KEY_USER_ROLE = "userRole"

    fun saveUserSession(context: Context, email: String, role: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_ROLE, role)
            apply()
        }
    }

    fun isLoggedIn(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getCurrentUserEmail(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_USER_EMAIL, null)
    }

    fun getCurrentUserRole(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_USER_ROLE, null)
    }

    fun logout(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().apply {
            clear()
            apply()
        }
    }
}