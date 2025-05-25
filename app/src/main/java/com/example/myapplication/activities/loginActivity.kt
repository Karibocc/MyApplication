package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.example.myapplication.R

class loginActivity : AppCompatActivity() {
    private lateinit var btnLogin: Button
    private lateinit var etUsuario: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnGoogle: Button

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Aquí puedes manejar el inicio de sesión exitoso
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(Exception::class.java)
                // Usuario ha iniciado sesión, redirigir a MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } catch (e: Exception) {
                // Manejar error de inicio de sesión
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        btnLogin = findViewById(R.id.btnLogin)
        etUsuario = findViewById(R.id.etUsuario)
        etPassword = findViewById(R.id.etPassword)
        btnGoogle = findViewById(R.id.btnGoogle)

        // Local login
        btnLogin.setOnClickListener {
            val user = etUsuario.text.toString()
            val pass = etPassword.text.toString()
            if (user == "admin" && pass == "1234") {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        // Google login
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val signInClient: GoogleSignInClient = GoogleSignIn.getClient(this, signInOptions)

        btnGoogle.setOnClickListener {
            val intent = signInClient.signInIntent
            googleSignInLauncher.launch(intent)
        }
    }
}