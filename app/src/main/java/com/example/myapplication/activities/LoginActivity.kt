package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    // Views
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogle: Button
    private lateinit var btnRegister: Button

    // Firebase Authentication
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // Google Sign-In Launcher
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            handleGoogleSignInResult(result.data)
        } else {
            showToast(getString(R.string.login_cancelled))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Solución agregada: evitar pantalla en blanco asegurando layout visible
        try {
            setContentView(R.layout.activity_login)
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar layout activity_login", e)
            showToast("Error cargando interfaz de inicio de sesión")
            return
        }

        // Inicializar Firebase Auth
        auth = Firebase.auth

        // Mostrar en Logcat si hay un usuario autenticado
        Log.d(TAG, "Usuario actual: ${auth.currentUser?.email ?: "No hay sesión activa"}")

        // ✅ Solución agregada: verificar si MainActivity puede mostrarse antes de redirigir
        if (auth.currentUser != null) {
            try {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                return
            } catch (e: Exception) {
                Log.e(TAG, "Error al redirigir a MainActivity", e)
                showToast("Error cargando pantalla principal, mostrando login.")
            }
        }

        initializeViews()
        setupGoogleSignIn()
        setupClickListeners()
    }

    private fun initializeViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogle = findViewById(R.id.btnGoogle)
        btnRegister = findViewById(R.id.btnRegister)
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (validateCredentials(email, password)) {
                loginWithEmail(email, password)
            }
        }

        btnGoogle.setOnClickListener {
            signInWithGoogle()
        }

        btnRegister.setOnClickListener {
            navigateToRegister()
        }
    }

    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToMain()
                } else {
                    showToast(task.exception?.message ?: getString(R.string.auth_failed))
                    Log.e(TAG, "Error en login con email", task.exception)
                }
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleGoogleSignInResult(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { token ->
                authenticateWithGoogle(token)
            } ?: showToast(getString(R.string.google_auth_failed))
        } catch (e: ApiException) {
            Log.w(TAG, "Google sign in failed", e)
            showToast("${getString(R.string.google_auth_failed)}: ${e.message}")
        }
    }

    private fun authenticateWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToMain()
                } else {
                    showToast(task.exception?.message ?: getString(R.string.auth_failed))
                    Log.e(TAG, "Error en autenticación Google", task.exception)
                }
            }
    }

    private fun validateCredentials(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            etEmail.error = getString(R.string.email_required)
            return false
        }

        if (password.isEmpty() || password.length < 6) {
            etPassword.error = getString(R.string.password_length_error)
            return false
        }

        return true
    }

    private fun navigateToMain() {
        try {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error al abrir MainActivity", e)
            showToast("No se pudo abrir la pantalla principal")
        }
    }

    private fun navigateToRegister() {
        startActivity(Intent(this, RegistroActivity::class.java))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}

