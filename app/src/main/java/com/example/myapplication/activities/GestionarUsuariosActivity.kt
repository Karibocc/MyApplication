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

        Log.d("NAVEGACION", "✅ GestionarUsuariosActivity creada exitosamente")

        try {
            lvUsuarios = findViewById(R.id.lvUsuarios)
            btnAgregarUsuario = findViewById(R.id.btnAgregarUsuario)
            btnActualizarLista = findViewById(R.id.btnActualizarLista)

            // Configurar botón actualizar lista
            btnActualizarLista.setOnClickListener {
                Log.d("GESTION_USUARIOS", "🔄 Actualizando lista manualmente")
                actualizarListaUsuarios()
                Toast.makeText(this, "Lista actualizada", Toast.LENGTH_SHORT).show()
            }

            // Configurar adapter con listeners
            configurarAdapter()

            // Botón para redirigir al módulo de registro
            btnAgregarUsuario.setOnClickListener {
                Log.d("GESTION_USUARIOS", "🔄 Botón Agregar Usuario clickeado - Redirigiendo a registro")
                redirigirARegistro()
            }

            Log.d("GESTION_USUARIOS", "✅ GestionarUsuariosActivity configurada correctamente")

        } catch (e: Exception) {
            Log.e("GESTION_USUARIOS", "❌ ERROR en onCreate: ${e.message}", e)
            Toast.makeText(this, "Error configurando la pantalla: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    /**
     * Redirige a la actividad de registro de usuarios
     */
    private fun redirigirARegistro() {
        try {
            Log.d("NAVEGACION", "🚀 Redirigiendo a actividad de registro")

            // Intent para abrir la actividad de registro
            // Reemplaza "RegistroActivity::class.java" con el nombre real de tu actividad de registro
            val intent = Intent(this, RegistroActivity::class.java)

            // Opcional: agregar flags si es necesario
            // intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

            startActivity(intent)

            // Opcional: agregar animación
            // overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

            Log.d("NAVEGACION", "✅ Actividad de registro iniciada exitosamente")

        } catch (e: Exception) {
            Log.e("NAVEGACION", "❌ ERROR al redirigir a registro: ${e.message}", e)
            Toast.makeText(this, "Error al abrir el módulo de registro", Toast.LENGTH_SHORT).show()

            // Fallback: mostrar el diálogo antiguo si hay error
            mostrarDialogAgregarUsuarioFallback()
        }
    }

    /**
     * Fallback: mostrar diálogo de agregar usuario si no existe la actividad de registro
     */
    private fun mostrarDialogAgregarUsuarioFallback() {
        Log.d("GESTION_USUARIOS", "🔄 Usando fallback: diálogo de agregar usuario")
        Toast.makeText(this, "Usando método alternativo para agregar usuario", Toast.LENGTH_SHORT).show()
        mostrarDialogAgregarUsuario()
    }

    /**
     * Configurar el adapter con todos los listeners
     */
    private fun configurarAdapter() {
        usuariosAdapter = UsuarioAdapter(
            context = this,
            usuarios = listaUsuarios,
            onEditarClickListener = { usuario, position ->
                Log.d("GESTION_USUARIOS", "✏️ Editando usuario: ${usuario.username}")
                mostrarDialogEditarUsuario(usuario, position)
            },
            onEliminarClickListener = { usuario, position ->
                Log.d("GESTION_USUARIOS", "🗑️ Eliminando usuario: ${usuario.username}")
                mostrarDialogConfirmarEliminacion(usuario, position)
            },
            onCambiarRolClickListener = { usuario, position ->
                Log.d("GESTION_USUARIOS", "🔄 Cambiando rol de usuario: ${usuario.username}")
                mostrarDialogCambiarRol(usuario, position)
            }
        )
        lvUsuarios.adapter = usuariosAdapter
    }

    /**
     * Actualizar lista de usuarios
     */
    private fun actualizarListaUsuarios() {
        try {
            Log.d("GESTION_USUARIOS", "🔄 Intentando obtener usuarios...")
            val nuevaLista = Usuario.obtenerTodosLosUsuarios(this).toMutableList()
            Log.d("GESTION_USUARIOS", "✅ Usuarios obtenidos: ${nuevaLista.size}")

            // Actualizar la lista interna y el adapter
            listaUsuarios.clear()
            listaUsuarios.addAll(nuevaLista)
            usuariosAdapter.actualizarLista(nuevaLista)

            // Mostrar mensaje si no hay usuarios
            if (nuevaLista.isEmpty()) {
                Toast.makeText(this, "No hay usuarios registrados", Toast.LENGTH_SHORT).show()
            }

            Log.d("GESTION_USUARIOS", "✅ Lista actualizada exitosamente")

        } catch (e: Exception) {
            Log.e("GESTION_USUARIOS", "❌ ERROR actualizando lista: ${e.message}", e)
            Toast.makeText(this, "Error cargando usuarios: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Diálogo para cambiar rol de usuario
     */
    private fun mostrarDialogCambiarRol(usuario: Usuario, position: Int) {
        val roles = arrayOf("Administrador", "Usuario", "Invitado")
        var rolSeleccionado = usuario.rol

        MaterialAlertDialogBuilder(this)
            .setTitle("Cambiar Rol de Usuario")
            .setMessage("Usuario: ${usuario.username}\nRol actual: ${usuario.rol}")
            .setSingleChoiceItems(roles, roles.indexOfFirst { it.equals(usuario.rol, ignoreCase = true) }) { dialog, which ->
                rolSeleccionado = roles[which]
            }
            .setPositiveButton("Cambiar Rol") { dialog, _ ->
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

    /**
     * Cambiar rol de usuario
     */
    private fun cambiarRolUsuario(usuario: Usuario, nuevoRol: String, position: Int) {
        try {
            Log.d("GESTION_USUARIOS", "🔄 Cambiando rol de ${usuario.username} a $nuevoRol")

            // Usar el método correcto para actualizar rol
            val exito = Usuario.actualizarRolUsuario(this, usuario.username, nuevoRol)

            if (exito) {
                Toast.makeText(this, "✅ Rol actualizado exitosamente", Toast.LENGTH_SHORT).show()
                // Actualizar la lista local
                val usuarioActualizado = usuario.copy(rol = nuevoRol)
                usuariosAdapter.actualizarUsuario(position, usuarioActualizado)
                actualizarListaUsuarios()
            } else {
                Toast.makeText(this, "❌ Error al cambiar el rol", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e("GESTION_USUARIOS", "❌ ERROR cambiando rol: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Diálogo para confirmar eliminación
     */
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

    /**
     * Eliminar usuario
     */
    private fun eliminarUsuario(usuario: Usuario, position: Int) {
        try {
            Log.d("GESTION_USUARIOS", "🗑️ Eliminando usuario: ${usuario.username}")

            // Usar el método correcto para eliminar
            val exito = Usuario.eliminarUsuario(this, usuario.username)

            if (exito) {
                Toast.makeText(this, "✅ Usuario eliminado exitosamente", Toast.LENGTH_SHORT).show()
                // Eliminar de la lista local
                usuariosAdapter.eliminarUsuario(position)
                actualizarListaUsuarios()
            } else {
                Toast.makeText(this, "❌ Error al eliminar usuario", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e("GESTION_USUARIOS", "❌ ERROR eliminando usuario: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Diálogo para editar usuario - VERSIÓN COMPATIBLE
     */
    private fun mostrarDialogEditarUsuario(usuario: Usuario, position: Int) {
        try {
            val dialogView = layoutInflater.inflate(R.layout.dialog_agregar_usuario, null)

            // Buscar elementos del layout
            val etUsername = dialogView.findViewById<EditText>(R.id.etUsername)
            val spRol = dialogView.findViewById<Spinner>(R.id.spRol)

            // Intentar encontrar el campo email (puede no existir)
            var etEmail: EditText? = null
            try {
                etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
            } catch (e: Exception) {
                Log.d("GESTION_USUARIOS", "⚠️ Campo etEmail no encontrado en el layout")
            }

            // Ocultar campo de contraseña si existe
            try {
                val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
                etPassword.visibility = View.GONE
            } catch (e: Exception) {
                Log.d("GESTION_USUARIOS", "⚠️ Campo etPassword no encontrado en el layout")
            }

            // Configurar valores actuales
            etUsername.setText(usuario.username)

            // Configurar email si el campo existe
            etEmail?.setText(usuario.email ?: "")

            // Configurar Spinner con roles
            val roles = listOf("Administrador", "Usuario", "Invitado")
            val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spRol.adapter = spinnerAdapter

            // Seleccionar rol actual
            val posicionRol = roles.indexOfFirst { it.equals(usuario.rol, ignoreCase = true) }
            if (posicionRol >= 0) {
                spRol.setSelection(posicionRol)
            }

            MaterialAlertDialogBuilder(this)
                .setTitle("Editar Usuario: ${usuario.username}")
                .setView(dialogView)
                .setPositiveButton("Guardar Cambios") { dialog, _ ->
                    val nuevoUsername = etUsername.text.toString().trim()
                    val nuevoEmail = etEmail?.text?.toString()?.trim() ?: usuario.email
                    val nuevoRol = spRol.selectedItem.toString()

                    if (nuevoUsername.isEmpty()) {
                        Toast.makeText(this, "El nombre de usuario no puede estar vacío", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    // Lógica para guardar cambios
                    guardarCambiosUsuario(usuario, nuevoUsername, nuevoEmail, nuevoRol, position)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()

        } catch (e: Exception) {
            Log.e("GESTION_USUARIOS", "❌ Error en diálogo de edición: ${e.message}", e)
            // Fallback: mostrar diálogo simple de cambio de rol
            mostrarDialogCambiarRol(usuario, position)
        }
    }

    /**
     * Guardar cambios del usuario editado
     */
    private fun guardarCambiosUsuario(usuarioOriginal: Usuario, nuevoUsername: String, nuevoEmail: String?, nuevoRol: String, position: Int) {
        try {
            Log.d("GESTION_USUARIOS", "💾 Guardando cambios para usuario: ${usuarioOriginal.username}")

            // Verificar si hubo cambios
            val usernameCambio = nuevoUsername != usuarioOriginal.username
            val emailCambio = nuevoEmail != usuarioOriginal.email
            val rolCambio = nuevoRol != usuarioOriginal.rol

            if (!usernameCambio && !emailCambio && !rolCambio) {
                Toast.makeText(this, "No se realizaron cambios", Toast.LENGTH_SHORT).show()
                return
            }

            // Actualizar rol si cambió
            if (rolCambio) {
                val exitoRol = Usuario.actualizarRolUsuario(this, usuarioOriginal.username, nuevoRol)
                if (!exitoRol) {
                    Toast.makeText(this, "❌ Error al actualizar el rol", Toast.LENGTH_SHORT).show()
                    return
                }
            }

            // Aquí podrías agregar lógica para actualizar username y email en la base de datos
            // Por ahora solo actualizamos localmente
            val usuarioActualizado = usuarioOriginal.copy(
                username = nuevoUsername,
                email = nuevoEmail,
                rol = nuevoRol
            )

            usuariosAdapter.actualizarUsuario(position, usuarioActualizado)

            // Mostrar mensaje según los cambios realizados
            val cambios = mutableListOf<String>()
            if (usernameCambio) cambios.add("usuario")
            if (emailCambio) cambios.add("email")
            if (rolCambio) cambios.add("rol")

            if (cambios.isNotEmpty()) {
                val mensajeCambios = cambios.joinToString(" y ")
                Toast.makeText(this, "✅ ${mensajeCambios.replaceFirstChar { it.uppercase() }} actualizado(s)", Toast.LENGTH_SHORT).show()
            }

            // Actualizar la lista completa para reflejar cambios
            actualizarListaUsuarios()

        } catch (e: Exception) {
            Log.e("GESTION_USUARIOS", "❌ Error guardando cambios: ${e.message}", e)
            Toast.makeText(this, "Error guardando cambios: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Muestra un diálogo emergente para agregar un nuevo usuario (método antiguo - ahora es fallback)
     */
    private fun mostrarDialogAgregarUsuario() {
        try {
            val dialogView = layoutInflater.inflate(R.layout.dialog_agregar_usuario, null)

            // Buscar los elementos dentro de la vista del diálogo
            val etUsername = dialogView.findViewById<EditText>(R.id.etUsername)
            val spRol = dialogView.findViewById<Spinner>(R.id.spRol)

            // Intentar encontrar el campo password
            var etPassword: EditText? = null
            try {
                etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
            } catch (e: Exception) {
                Log.e("GESTION_USUARIOS", "❌ Campo etPassword no encontrado en el layout")
                Toast.makeText(this, "Error: Layout de diálogo incompleto", Toast.LENGTH_SHORT).show()
                return
            }

            // Configurar Spinner con roles
            val roles = listOf("Administrador", "Usuario", "Invitado")
            val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spRol.adapter = spinnerAdapter

            MaterialAlertDialogBuilder(this)
                .setTitle("Agregar Usuario")
                .setView(dialogView)
                .setPositiveButton("Agregar") { dialog, _ ->
                    val username = etUsername.text.toString().trim()
                    val password = etPassword.text.toString().trim()
                    val rol = spRol.selectedItem.toString()

                    Log.d("REGISTRO_USUARIO", "🔍 INICIANDO PROCESO DE REGISTRO")
                    Log.d("REGISTRO_USUARIO", "📝 Datos - Usuario: '$username', Rol: '$rol', Password: '${"*".repeat(password.length)}'")

                    if (username.isEmpty() || password.isEmpty()) {
                        Log.e("REGISTRO_USUARIO", "❌ ERROR: Campos vacíos detectados")
                        Toast.makeText(this, "Ingresa usuario y contraseña", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        return@setPositiveButton
                    }

                    // Validar email (si usas email como username)
                    if (!Usuario.esEmailValido(username)) {
                        Log.e("REGISTRO_USUARIO", "❌ ERROR: Email inválido - '$username'")
                        Toast.makeText(this, "Por favor ingrese un email válido", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        return@setPositiveButton
                    }

                    // Validar longitud de contraseña
                    if (password.length < 6) {
                        Log.e("REGISTRO_USUARIO", "❌ ERROR: Contraseña muy corta - ${password.length} caracteres")
                        Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        return@setPositiveButton
                    }

                    Log.d("REGISTRO_USUARIO", "✅ Validaciones pasadas, llamando a registrarUsuarioDesdeStrings")

                    // Mostrar mensaje de procesamiento
                    Toast.makeText(this, "Registrando usuario...", Toast.LENGTH_SHORT).show()

                    // Ejecutar en un hilo para evitar bloqueos
                    Thread {
                        try {
                            Log.d("REGISTRO_USUARIO", "🔄 Ejecutando registro en segundo plano")
                            val exito = Usuario.registrarUsuarioDesdeStrings(this@GestionarUsuariosActivity, username, password, rol)

                            runOnUiThread {
                                Log.d("REGISTRO_USUARIO", "📊 Resultado del registro: $exito")

                                if (exito) {
                                    Log.i("REGISTRO_USUARIO", "🎉 USUARIO REGISTRADO EXITOSAMENTE: $username")
                                    Toast.makeText(this@GestionarUsuariosActivity, "✅ Usuario agregado exitosamente", Toast.LENGTH_LONG).show()

                                    // Actualizar la lista
                                    actualizarListaUsuarios()

                                    // Limpiar campos del diálogo
                                    etUsername.text.clear()
                                    etPassword.text.clear()

                                } else {
                                    Log.e("REGISTRO_USUARIO", "💥 ERROR: No se pudo registrar el usuario")
                                    Toast.makeText(this@GestionarUsuariosActivity, "❌ Error: No se pudo registrar el usuario", Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("REGISTRO_USUARIO", "💥 EXCEPCIÓN CRÍTICA: ${e.message}", e)
                            runOnUiThread {
                                Toast.makeText(this@GestionarUsuariosActivity, "❌ Error crítico: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }.start()

                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    Log.d("REGISTRO_USUARIO", "❌ Registro cancelado por usuario")
                    dialog.dismiss()
                }
                .show()

        } catch (e: Exception) {
            Log.e("GESTION_USUARIOS", "❌ Error en diálogo de agregar: ${e.message}", e)
            Toast.makeText(this, "Error al mostrar diálogo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar usuarios y actualizar el adapter
        Log.d("GESTION_USUARIOS", "🔄 onResume - Actualizando lista")
        actualizarListaUsuarios()
    }
}