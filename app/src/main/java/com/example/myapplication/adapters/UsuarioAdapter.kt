package com.example.myapplication.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.models.Usuario

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

        tvUsername.text = "Usuario: ${usuario.username}"
        tvRol.text = "Rol: ${usuario.rol?.replaceFirstChar { it.uppercase() } ?: "Usuario"}"
        tvEmail.text = "Email: ${usuario.email ?: "No especificado"}"

        when (usuario.rol?.lowercase()) {
            "administrador" -> {
                tvRol.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            }
            "usuario" -> {
                tvRol.setTextColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
            }
            else -> {
                tvRol.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            }
        }

        btnEditar.setOnClickListener {
            Log.d("USUARIO_ADAPTER", "Editando: ${usuario.username}, Posición: $position")
            onEditarClickListener(usuario, position)
        }

        btnEliminar.setOnClickListener {
            Log.d("USUARIO_ADAPTER", "Eliminando: ${usuario.username}, Posición: $position")
            onEliminarClickListener(usuario, position)
        }

        btnCambiarRol.setOnClickListener {
            Log.d("USUARIO_ADAPTER", "Cambiando rol: ${usuario.username}, Posición: $position")
            onCambiarRolClickListener(usuario, position)
        }

        return itemView
    }

    override fun getCount(): Int {
        return usuarios.size
    }

    override fun getItem(position: Int): Usuario {
        return usuarios[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun actualizarLista(nuevaLista: List<Usuario>) {
        usuarios.clear()
        usuarios.addAll(nuevaLista)
        notifyDataSetChanged()
        Log.d("USUARIO_ADAPTER", "Lista actualizada - ${usuarios.size} usuarios")
    }

    fun obtenerUsuarioEnPosicion(position: Int): Usuario? {
        return if (position in 0 until usuarios.size) {
            usuarios[position]
        } else {
            null
        }
    }

    fun eliminarUsuario(position: Int): Usuario? {
        return if (position in 0 until usuarios.size) {
            val usuarioEliminado = usuarios.removeAt(position)
            notifyDataSetChanged()
            Log.d("USUARIO_ADAPTER", "Usuario eliminado: ${usuarioEliminado.username}")
            usuarioEliminado
        } else {
            null
        }
    }

    fun eliminarUsuarioPorUsername(username: String): Boolean {
        val iterator = usuarios.iterator()
        while (iterator.hasNext()) {
            val usuario = iterator.next()
            if (usuario.username == username) {
                iterator.remove()
                notifyDataSetChanged()
                Log.d("USUARIO_ADAPTER", "Usuario eliminado por username: $username")
                return true
            }
        }
        return false
    }

    fun actualizarUsuario(position: Int, usuarioActualizado: Usuario): Boolean {
        return if (position in 0 until usuarios.size) {
            usuarios[position] = usuarioActualizado
            notifyDataSetChanged()
            Log.d("USUARIO_ADAPTER", "Usuario actualizado: ${usuarioActualizado.username}")
            true
        } else {
            false
        }
    }

    fun filtrarPorRol(rol: String): List<Usuario> {
        return if (rol.isBlank()) {
            usuarios
        } else {
            usuarios.filter { it.rol.equals(rol, ignoreCase = true) }
        }
    }

    fun contarUsuariosPorRol(rol: String): Int {
        return usuarios.count { it.rol.equals(rol, ignoreCase = true) }
    }

    fun estaVacio(): Boolean {
        return usuarios.isEmpty()
    }

    fun obtenerRolesUnicos(): List<String> {
        return usuarios.map { it.rol ?: "Usuario" }.distinct()
    }

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

    fun obtenerListaActual(): List<Usuario> {
        return usuarios.toList()
    }
}