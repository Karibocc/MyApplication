package com.example.myapplication.fragments

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.models.Producto


open class ProductosEnCarrito {
    private val _productos = mutableListOf<Producto>()
    private val productos: List<Producto> get() = _productos.toList()

    // LiveData para observar cambios
    private val _productosLiveData = MutableLiveData<List<Producto>>()
    val productosLiveData: LiveData<List<Producto>> = _productosLiveData

    private val _totalLiveData = MutableLiveData<Double>()
    val totalLiveData: LiveData<Double> = _totalLiveData

    init {
        // Inicializar con datos persistentes si es necesario
        // cargarCarritoGuardado(context)
    }

    // Agregar producto al carrito
    fun agregarProducto(producto: Producto) {
        val existingIndex = _productos.indexOfFirst { it.id == producto.id }
        if (existingIndex == -1) {
            _productos.add(producto)
        } else {
            // Actualizar producto existente (por ejemplo, cantidad)
            _productos[existingIndex] = producto
        }
        actualizarEstados()
    }

    // Eliminar producto del carrito
    fun eliminarProducto(productoId: Int) {
        _productos.removeAll { it.id == productoId }
        actualizarEstados()
    }

    // Actualizar cantidad de un producto
    fun actualizarCantidad(productoId: Int, nuevaCantidad: Int) {
        _productos.find { it.id == productoId }?.let { producto ->
            // Asumiendo que Producto tiene propiedad cantidad
            val productoActualizado = producto.copy(/* cantidad = nuevaCantidad */)
            _productos[_productos.indexOf(producto)] = productoActualizado
            actualizarEstados()
        }
    }

    // Calcular total del carrito
    fun calcularTotal(): Double {
        return _productos.sumOf { it.precio /* * it.cantidad */ }
    }

    // Obtener todos los productos
    fun obtenerProductos(): List<Producto> {
        return productos
    }

    // Limpiar carrito
    fun limpiarCarrito() {
        _productos.clear()
        actualizarEstados()
    }

    // Método privado para actualizar LiveData
    private fun actualizarEstados() {
        _productosLiveData.value = productos
        _totalLiveData.value = calcularTotal()
        // guardarCarrito() // Si necesitas persistencia
    }

    // Métodos para persistencia (opcional)
    private fun guardarCarrito(context: Context) {
        // Implementar lógica para guardar en SharedPreferences o Room
    }

    private fun cargarCarritoGuardado(context: Context) {
        // Implementar lógica para cargar desde SharedPreferences o Room
    }
}
