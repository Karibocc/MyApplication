package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.adapters.UsuarioAdapter
import com.example.myapplication.models.Usuario
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class GestionarUsuariosActivity : AppCompatActivity() {

    private lateinit var lvUsuarios: ListView
    private lateinit var btnAgregarUsuario: Button
    private lateinit var btnActualizarLista: Button
    private lateinit var usuariosAdapter: UsuarioAdapter
    private var listaUsuarios: MutableList<Usuario> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestionar_usuarios)

        Log.d("GESTION_USUARIOS", "GestionarUsuariosActivity creada")

        try {
            inicializarVistas()
            configurarListeners()
            configurarAdapter()
            actualizarListaUsuarios()

        } catch (e: Exception) {
            Log.e("GESTION_USUARIOS", "ERROR en onCreate: ${e.message}", e)
            Toast.makeText(this, "Error configurando la pantalla", Toast.LENGTH_LONG).show()
        }
    }

    private fun inicializarVistas() {
        lvUsuarios = findViewById(R.id.lvUsuarios)
        btnAgregarUsuario = findViewById(R.id.btnAgregarUsuario)
        btnActualizarLista = findViewById(R.id.btnActualizarLista)
    }

    private fun configurarListeners() {
        btnActualizarLista.setOnClickListener {
            Log.d("GESTION_USUARIOS", "Actualizando lista manualmente")
            actualizarListaUsuarios()
            Toast.makeText(this, "Lista actualizada", Toast.LENGTH_SHORT).show()
        }

        btnAgregarUsuario.setOnClickListener {
            Log.d("GESTION_USUARIOS", "Botón Agregar Usuario clickeado")
            // Redirigir a RegistroActivity
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }

    private fun configurarAdapter() {
        usuariosAdapter = UsuarioAdapter(
            context = this,
            usuarios = listaUsuarios,
            onEditarClickListener = { usuario, position ->
                Log.d("GESTION_USUARIOS", "Editando usuario: ${usuario.username}")
                mostrarDialogEditarUsuario(usuario, position)
            },
            onEliminarClickListener = { usuario, position ->
                Log.d("GESTION_USUARIOS", "Eliminando usuario: ${usuario.username}")
                mostrarDialogConfirmarEliminacion(usuario, position)
            },
            onCambiarRolClickListener = { usuario, position ->
                Log.d("GESTION_USUARIOS", "Cambiando rol de usuario: ${usuario.username}")
                mostrarDialogCambiarRol(usuario, position)
            }
        )
        lvUsuarios.adapter = usuariosAdapter
    }

    private fun actualizarListaUsuarios() {
        try {
            Log.d("GESTION_USUARIOS", "Obteniendo usuarios...")
            val nuevaLista = Usuario.obtenerTodosLosUsuarios(this).toMutableList()
            Log.d("GESTION_USUARIOS", "Usuarios obtenidos: ${nuevaLista.size}")

            listaUsuarios.clear()
            listaUsuarios.addAll(nuevaLista)
            usuariosAdapter.actualizarLista(nuevaLista)

            if (nuevaLista.isEmpty()) {
                Toast.makeText(this, "No hay usuarios registrados", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e("GESTION_USUARIOS", "ERROR actualizando lista: ${e.message}", e)
            Toast.makeText(this, "Error cargando usuarios", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogCambiarRol(usuario: Usuario, position: Int) {
        val roles = arrayOf("Administrador", "Usuario", "Invitado")
        var rolSeleccionado = usuario.rol

        MaterialAlertDialogBuilder(this)
            .setTitle("Cambiar Rol")
            .setMessage("Usuario: ${usuario.username}\nRol actual: ${usuario.rol}")
            .setSingleChoiceItems(roles, roles.indexOfFirst { it.equals(usuario.rol, ignoreCase = true) }) { _, which ->
                rolSeleccionado = roles[which]
            }
            .setPositiveButton("Cambiar") { dialog, _ ->
                if (rolSeleccionado != usuario.rol) {
                    cambiarRolUsuario(usuario, rolSeleccionado, position)
                } else {
                    Toast.makeText(this, "El rol no ha cambiado", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun mostrarDialogConfirmarEliminacion(usuario: Usuario, position: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de eliminar al usuario:\n${usuario.username}?")
            .setPositiveButton("Eliminar") { dialog, _ ->
                eliminarUsuario(usuario, position)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun mostrarDialogEditarUsuario(usuario: Usuario, position: Int) {
        try {
            val dialogView = layoutInflater.inflate(R.layout.dialog_agregar_usuario, null)

            val etUsername = dialogView.findViewById<EditText>(R.id.etUsername)
            val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
            val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
            val spRol = dialogView.findViewById<Spinner>(R.id.spRol)
            val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)
            val btnGuardar = dialogView.findViewById<Button>(R.id.btnGuardar)

            etUsername.setText(usuario.username)
            etEmail.setText(usuario.email ?: "")
            etPassword.visibility = View.GONE

            val roles = listOf("Administrador", "Usuario", "Invitado")
            val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spRol.adapter = spinnerAdapter

            val posicionRol = roles.indexOfFirst { it.equals(usuario.rol, ignoreCase = true) }
            if (posicionRol >= 0) {
                spRol.setSelection(posicionRol)
            }

            val dialog = MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setTitle("Editar Usuario")
                .create()

            btnCancelar.setOnClickListener {
                dialog.dismiss()
            }

            btnGuardar.setOnClickListener {
                val nuevoUsername = etUsername.text.toString().trim()
                val nuevoEmail = etEmail.text.toString().trim()
                val nuevoRol = spRol.selectedItem.toString()

                if (nuevoUsername.isEmpty()) {
                    Toast.makeText(this, "El nombre de usuario no puede estar vacío", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (nuevoEmail.isEmpty()) {
                    Toast.makeText(this, "El email no puede estar vacío", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                guardarCambiosUsuario(usuario, nuevoUsername, nuevoEmail, nuevoRol, position)
                dialog.dismiss()
            }

            dialog.show()

        } catch (e: Exception) {
            Log.e("GESTION_USUARIOS", "Error en diálogo de edición: ${e.message}", e)
            Toast.makeText(this, "Error al editar usuario", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cambiarRolUsuario(usuario: Usuario, nuevoRol: String, position: Int) {
        try {
            Log.d("GESTION_USUARIOS", "Cambiando rol de ${usuario.username} a $nuevoRol")
            Log.d("GESTION_USUARIOS", "Usuario ID: ${usuario.id}, Email: ${usuario.email}")

            val exito = Usuario.actualizarRolUsuario(this, usuario.username, nuevoRol)
            Log.d("GESTION_USUARIOS", "Resultado de actualizarRolUsuario: $exito")

            if (exito) {
                Toast.makeText(this, "Rol actualizado exitosamente", Toast.LENGTH_SHORT).show()
                val usuarioActualizado = usuario.copy(rol = nuevoRol)
                usuariosAdapter.actualizarUsuario(position, usuarioActualizado)


                Log.d("GESTION_USUARIOS", "Usuario actualizado en adapter: ${usuarioActualizado.username} - ${usuarioActualizado.rol}")
            } else {
                Toast.makeText(this, "Error al cambiar el rol", Toast.LENGTH_SHORT).show()

                actualizarListaUsuarios()
            }

        } catch (e: Exception) {
            Log.e("GESTION_USUARIOS", "ERROR cambiando rol: ${e.message}", e)
            Toast.makeText(this, "Error al cambiar rol", Toast.LENGTH_SHORT).show()
        }
    }

    private fun eliminarUsuario(usuario: Usuario, position: Int) {
        try {
            Log.d("GESTION_USUARIOS", "Eliminando usuario: ${usuario.username}")

            val exito = Usuario.eliminarUsuario(this, usuario.username)

            if (exito) {
                Toast.makeText(this, "Usuario eliminado exitosamente", Toast.LENGTH_SHORT).show()
                usuariosAdapter.eliminarUsuario(position)
            } else {
                Toast.makeText(this, "Error al eliminar usuario", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e("GESTION_USUARIOS", "ERROR eliminando usuario: ${e.message}", e)
            Toast.makeText(this, "Error al eliminar usuario", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarCambiosUsuario(usuarioOriginal: Usuario, nuevoUsername: String, nuevoEmail: String, nuevoRol: String, position: Int) {
        try {
            Log.d("GESTION_USUARIOS", "Guardando cambios para usuario: ${usuarioOriginal.username}")

            val rolCambio = nuevoRol != usuarioOriginal.rol
            val usernameCambio = nuevoUsername != usuarioOriginal.username

            var cambiosRealizados = false

            if (rolCambio) {
                val exitoRol = Usuario.actualizarRolUsuario(this, usuarioOriginal.username, nuevoRol)
                if (exitoRol) {
                    cambiosRealizados = true
                    Log.d("GESTION_USUARIOS", "Rol cambiado exitosamente")
                }
            }

            if (usernameCambio) {
                Toast.makeText(this, "Solo se puede cambiar el rol por ahora", Toast.LENGTH_SHORT).show()
            }

            if (cambiosRealizados) {
                Toast.makeText(this, "Cambios guardados exitosamente", Toast.LENGTH_SHORT).show()
                actualizarListaUsuarios()
            } else {
                Toast.makeText(this, "No se realizaron cambios", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e("GESTION_USUARIOS", "Error guardando cambios: ${e.message}", e)
            Toast.makeText(this, "Error guardando cambios", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("GESTION_USUARIOS", "onResume - Actualizando lista")
        actualizarListaUsuarios()
    }
}