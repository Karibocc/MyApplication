package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.activities.MainActivity
import com.example.myapplication.models.usuario
import com.example.myapplication.R


class RegistroActivity : AppCompatActivity() {

    private lateinit var btnRegistrar: Button
    private lateinit var etUsuario: EditText
    private lateinit var etPassword: EditText
    private lateinit var switchRol: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // Inicialización de vistas
        btnRegistrar = findViewById(R.id.btnRegistrar)
        etUsuario = findViewById(R.id.etUsuario)
        etPassword = findViewById(R.id.etPassword)
        switchRol = findViewById(R.id.switchRol)

        btnRegistrar.setOnClickListener {
            // Validar que los campos no estén vacíos
            if (etUsuario.text.toString().isEmpty() || etPassword.text.toString().isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rol = if (switchRol.isChecked) "Admin" else "Cliente"
            val usuario = usuario(
                username = etUsuario.text.toString(),
                password = etPassword.text.toString(),
                rol = rol
            )

            // Aquí podrías agregar lógica para guardar el usuario en SharedPreferences o base de datos

            Toast.makeText(this, "Registrado como $rol", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}