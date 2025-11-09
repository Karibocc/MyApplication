package com.example.myapplication.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.myapplication.R
import com.example.myapplication.models.Usuario

/**
 * Adapter personalizado para mostrar la lista de usuarios.
 */
class UsuarioAdapter(
    context: Context,
    private val usuarios: MutableList<Usuario>,
    private val onEditarClickListener: (Usuario, Int) -> Unit = { _, _ -> },
    private val onEliminarClickListener: (Usuario, Int) -> Unit = { _, _ -> },
    private val onCambiarRolClickListener: (Usuario, Int) -> Unit = { _, _ -> }
) : ArrayAdapter<Usuario>(context, 0, usuarios) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_usuario, parent, false)

        val usuario = usuarios[position]

        val tvUsername = itemView.findViewById<TextView>(R.id.tvUsername)
        val tvRol = itemView.findViewById<TextView>(R.id.tvRol)
        val tvEmail = itemView.findViewById<TextView>(R.id.tvEmail)
        val btnEditar = itemView.findViewById<Button>(R.id.btnEditar)
        val btnEliminar = itemView.findViewById<Button>(R.id.btnEliminar)
        val btnCambiarRol = itemView.findViewById<Button>(R.id.btnCambiarRol)

        // Configurar los textos
        tvUsername.text = "Usuario: ${usuario.username}"
        tvRol.text = "Rol: ${usuario.rol.replaceFirstChar { it.uppercase() }}"

        // Mostrar email si está disponible
        tvEmail.text = "Email: ${usuario.email ?: usuario.username}"

        // Configurar listeners de los botones
        btnEditar.setOnClickListener {
            onEditarClickListener(usuario, position)
        }

        btnEliminar.setOnClickListener {
            onEliminarClickListener(usuario, position)
        }

        btnCambiarRol.setOnClickListener {
            onCambiarRolClickListener(usuario, position)
        }

        return itemView
    }

    /**
     * Actualiza la lista de usuarios y notifica el cambio al ListView.
     */
    fun actualizarLista(nuevaLista: List<Usuario>) {
        usuarios.clear()
        usuarios.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    /**
     * 🔹 NUEVO: Método para obtener un usuario por su posición
     */
    fun obtenerUsuarioEnPosicion(position: Int): Usuario? {
        return if (position in 0 until usuarios.size) {
            usuarios[position]
        } else {
            null
        }
    }

    /**
     * 🔹 NUEVO: Método para eliminar un usuario por su posición
     */
    fun eliminarUsuario(position: Int): Usuario? {
        return if (position in 0 until usuarios.size) {
            val usuarioEliminado = usuarios.removeAt(position)
            notifyDataSetChanged()
            usuarioEliminado
        } else {
            null
        }
    }

    /**
     * 🔹 NUEVO: Método para eliminar un usuario por su username
     */
    fun eliminarUsuarioPorUsername(username: String): Boolean {
        val iterator = usuarios.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().username == username) {
                iterator.remove()
                notifyDataSetChanged()
                return true
            }
        }
        return false
    }

    /**
     * 🔹 NUEVO: Método para actualizar un usuario específico
     */
    fun actualizarUsuario(position: Int, usuarioActualizado: Usuario): Boolean {
        return if (position in 0 until usuarios.size) {
            usuarios[position] = usuarioActualizado
            notifyDataSetChanged()
            true
        } else {
            false
        }
    }

    /**
     * 🔹 NUEVO: Método para filtrar usuarios por rol
     */
    fun filtrarPorRol(rol: String): List<Usuario> {
        return if (rol.isBlank()) {
            usuarios
        } else {
            usuarios.filter { it.rol.equals(rol, ignoreCase = true) }
        }
    }

    /**
     * 🔹 NUEVO: Método para obtener la cantidad de usuarios por rol
     */
    fun contarUsuariosPorRol(rol: String): Int {
        return usuarios.count { it.rol.equals(rol, ignoreCase = true) }
    }

    /**
     * 🔹 NUEVO: Método para verificar si el adapter está vacío
     */
    fun estaVacio(): Boolean {
        return usuarios.isEmpty()
    }

    /**
     * 🔹 NUEVO: Método para obtener todos los roles únicos
     */
    fun obtenerRolesUnicos(): List<String> {
        return usuarios.map { it.rol }.distinct()
    }

    /**
     * 🔹 NUEVO: Método para buscar usuarios por nombre
     */
    fun buscarUsuarios(query: String): List<Usuario> {
        return if (query.isBlank()) {
            usuarios
        } else {
            usuarios.filter {
                it.username.contains(query, ignoreCase = true) ||
                        (it.email?.contains(query, ignoreCase = true) == true)
            }
        }
    }

    /**
     * 🔹 NUEVO: Método para obtener la lista actual de usuarios
     */
    fun obtenerListaActual(): List<Usuario> {
        return usuarios.toList()
    }
}