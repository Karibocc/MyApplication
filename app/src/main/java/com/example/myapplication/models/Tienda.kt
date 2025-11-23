package com.example.myapplication.models

data class Tienda(
    val id: String = "",
    val nombre: String = "",
    val direccion: String = "",
    val horario: String = "",
    val telefono: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0
)