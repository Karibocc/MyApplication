package com.example.myapplication.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.models.Usuario

/**
 * Adapter personalizado para mostrar la lista de usuarios.
 */
class UsuarioAdapter(
    context: Context,
    private val usuarios: MutableList<Usuario>
) : ArrayAdapter<Usuario>(context, 0, usuarios) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_usuario, parent, false)

        val usuario = usuarios[position]

        val tvUsername = itemView.findViewById<TextView>(R.id.tvUsername)
        val tvRol = itemView.findViewById<TextView>(R.id.tvRol)

        tvUsername.text = usuario.username
        // Usamos replaceFirstChar para compatibilidad con versiones modernas
        tvRol.text = usuario.rol.replaceFirstChar { it.uppercase() }

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
}
