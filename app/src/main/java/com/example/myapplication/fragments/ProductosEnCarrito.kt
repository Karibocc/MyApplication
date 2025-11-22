package com.example.myapplication.fragments

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.models.Producto

class ProductosEnCarrito {
    private val _productos = mutableListOf<Producto>()
    private val productos: List<Producto> get() = _productos.toList()

    private val _productosLiveData = MutableLiveData<List<Producto>>()
    val productosLiveData: LiveData<List<Producto>> = _productosLiveData

    private val _totalLiveData = MutableLiveData<Double>()
    val totalLiveData: LiveData<Double> = _totalLiveData

    fun agregarProducto(producto: Producto) {
        val existingIndex = _productos.indexOfFirst { it.id == producto.id }
        if (existingIndex == -1) {
            _productos.add(producto)
        } else {
            _productos[existingIndex] = producto
        }
        actualizarEstados()
    }

    fun eliminarProducto(productoId: Int) {
        _productos.removeAll { it.id == productoId }
        actualizarEstados()
    }

    fun actualizarCantidad(productoId: Int, nuevaCantidad: Int) {
        _productos.find { it.id == productoId }?.let { producto ->
            val productoActualizado = producto.copy(cantidad = nuevaCantidad)
            _productos[_productos.indexOf(producto)] = productoActualizado
            actualizarEstados()
        }
    }

    fun calcularTotal(): Double {
        return _productos.sumOf { it.precio * it.cantidad }
    }

    fun obtenerProductos(): List<Producto> {
        return productos
    }

    fun limpiarCarrito() {
        _productos.clear()
        actualizarEstados()
    }

    fun obtenerProductoPorId(productoId: Int): Producto? {
        return _productos.find { it.id == productoId }
    }

    fun contieneProducto(productoId: Int): Boolean {
        return _productos.any { it.id == productoId }
    }

    fun obtenerCantidadTotalProductos(): Int {
        return _productos.sumOf { it.cantidad }
    }

    fun obtenerCantidadProducto(productoId: Int): Int {
        return _productos.find { it.id == productoId }?.cantidad ?: 0
    }

    fun incrementarCantidad(productoId: Int): Boolean {
        return _productos.find { it.id == productoId }?.let { producto ->
            val nuevaCantidad = producto.cantidad + 1
            actualizarCantidad(productoId, nuevaCantidad)
            true
        } ?: false
    }

    fun decrementarCantidad(productoId: Int): Boolean {
        return _productos.find { it.id == productoId }?.let { producto ->
            if (producto.cantidad > 1) {
                val nuevaCantidad = producto.cantidad - 1
                actualizarCantidad(productoId, nuevaCantidad)
                true
            } else {
                eliminarProducto(productoId)
                false
            }
        } ?: false
    }

    fun estaVacio(): Boolean {
        return _productos.isEmpty()
    }

    fun obtenerNumeroItemsUnicos(): Int {
        return _productos.size
    }

    fun calcularSubtotalProducto(productoId: Int): Double {
        return _productos.find { it.id == productoId }?.let { producto ->
            producto.precio * producto.cantidad
        } ?: 0.0
    }

    private fun actualizarEstados() {
        _productosLiveData.value = productos
        _totalLiveData.value = calcularTotal()
    }

    private fun guardarCarrito(context: Context) {
    }

    private fun cargarCarritoGuardado(context: Context) {
    }
}