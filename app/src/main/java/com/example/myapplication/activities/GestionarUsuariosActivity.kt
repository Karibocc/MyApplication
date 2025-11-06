package com.example.myapplication.activities

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.adapters.UsuarioAdapter
import com.example.myapplication.models.Usuario
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class GestionarUsuariosActivity : AppCompatActivity() {

    private lateinit var lvUsuarios: ListView
    private lateinit var btnAgregarUsuario: Button
    private lateinit var usuariosAdapter: UsuarioAdapter
    private var listaUsuarios: MutableList<Usuario> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestionar_usuarios)

        // Inicializar vistas
        lvUsuarios = findViewById(R.id.lvUsuarios)
        btnAgregarUsuario = findViewById(R.id.btnAgregarUsuario)

        // Configurar adapter
        actualizarListaUsuarios()

        // Botón para mostrar diálogo de agregar usuario
        btnAgregarUsuario.setOnClickListener { mostrarDialogAgregarUsuario() }

        // Click en cada usuario de la lista
        lvUsuarios.setOnItemClickListener { _, _, position, _ ->
            val usuario = listaUsuarios[position]
            Toast.makeText(
                this,
                "Usuario: ${usuario.username}\nRol: ${usuario.rol}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Muestra un diálogo emergente para agregar un nuevo usuario
     */
    private fun mostrarDialogAgregarUsuario() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_agregar_usuario, null)

        // Buscar los elementos dentro de la vista del diálogo
        val etUsername = dialogView.findViewById<EditText>(R.id.etUsername)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val spRol = dialogView.findViewById<Spinner>(R.id.spRol)

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

                                // 🔥 CORRECCIÓN: Actualizar la lista correctamente
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
    }

    /**
     * 🔥 NUEVO MÉTODO: Actualizar lista de usuarios de forma centralizada
     */
    private fun actualizarListaUsuarios() {
        val nuevaLista = Usuario.obtenerTodosLosUsuarios(this).toMutableList()
        Log.d("REGISTRO_USUARIO", "📋 Actualizando lista - Total usuarios: ${nuevaLista.size}")

        // Actualizar la lista interna
        listaUsuarios.clear()
        listaUsuarios.addAll(nuevaLista)

        // Notificar al adapter
        if (::usuariosAdapter.isInitialized) {
            usuariosAdapter.actualizarLista(nuevaLista)
        } else {
            // Si el adapter no está inicializado, crearlo
            usuariosAdapter = UsuarioAdapter(this, listaUsuarios)
            lvUsuarios.adapter = usuariosAdapter
        }

        // Verificar contenido de la lista
        nuevaLista.forEachIndexed { index, usuario ->
            Log.d("REGISTRO_USUARIO", "👤 Usuario $index: ${usuario.username} - ${usuario.rol}")
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar usuarios y actualizar el adapter
        Log.d("REGISTRO_USUARIO", "🔄 onResume - Actualizando lista")
        actualizarListaUsuarios()
    }
}
