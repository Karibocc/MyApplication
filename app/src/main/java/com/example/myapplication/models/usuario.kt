package com.example.myapplication.models

data class usuario(
    val username: String,
    val password: String,
    val rol: String,
    val isAdmin: Boolean = false
) {
    // Puedes agregar funciones adicionales si es necesario
    // Por ejemplo:
    fun isValid(): Boolean {
        return username.isNotBlank() && password.isNotBlank()
    }
}