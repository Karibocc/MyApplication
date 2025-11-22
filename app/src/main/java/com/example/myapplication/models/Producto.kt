package com.example.myapplication.models

data class Producto(
    val id: Int,
    val nombre: String,
    val descripcion: String = "",
    val precio: Double,
    val imagen_path: String = "",
    val stock: Int,
    var cantidad: Int = 1,
    val categoria: String = "General"

) {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Producto

        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return id
    }


    init {
        require(id >= 0) { "El ID debe ser positivo" }
        require(nombre.isNotBlank()) { "El nombre no puede estar vacío" }
        require(precio >= 0) { "El precio no puede ser negativo" }
        require(cantidad >= 0) { "La cantidad no puede ser negativa" }
    }


    fun subtotal(): Double {
        return precio * cantidad
    }


    fun precioFormateado(): String {
        return "$${"%.2f".format(precio)}"
    }
}