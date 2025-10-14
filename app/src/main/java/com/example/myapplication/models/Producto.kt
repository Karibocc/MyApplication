package com.example.myapplication.models

data class Producto(
    val id: Int,
    val nombre: String,
    val descripcion: String = "",
    val precio: Double,
    val imagen_path: String = "",
    val stock: Int
) {
    /**
     * Implementación personalizada de equals().
     * Dos productos se consideran iguales si tienen el mismo ID.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Producto

        if (id != other.id) return false
        return true
    }

    /**
     * Implementación de hashCode() consistente con equals().
     * Solo usa el ID para el cálculo del hash.
     */
    override fun hashCode(): Int {
        return id
    }

    /**
     * Validaciones básicas al crear un producto
     */
    init {
        require(id >= 0) { "El ID debe ser positivo" }
        require(nombre.isNotBlank()) { "El nombre no puede estar vacío" }
        require(precio >= 0) { "El precio no puede ser negativo" }
    }
}

