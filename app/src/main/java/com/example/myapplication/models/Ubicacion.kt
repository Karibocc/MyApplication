package com.example.myapplication.models

data class Ubicacion(
    val latitud: Double,
    val longitud: Double,
    val direccion: String = ""
)