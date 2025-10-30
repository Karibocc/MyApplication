package com.example.myapplication.activities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.managers.SessionManager
import com.example.myapplication.models.Usuario
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogle: Button
    private lateinit var btnRegister: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            handleGoogleSignInResult(result.data)
        } else {
            showToast("Login cancelado")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "🚀 === INICIANDO LOGINACTIVITY ===")

        try {
            Log.d(TAG, "1. Antes de setContentView")
            setContentView(R.layout.activity_login)
            Log.d(TAG, "✅ 1. setContentView completado")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR CRÍTICO en setContentView: ${e.message}", e)
            Log.e(TAG, "❌ Tipo de error: ${e.javaClass.simpleName}")
            showToast("Error cargando la interfaz")
            // No podemos continuar sin el layout básico
            finish()
            return
        }

        try {
            Log.d(TAG, "2. Inicializando Firebase Auth")
            auth = Firebase.auth
            Log.d(TAG, "✅ 2. Firebase Auth inicializado")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR inicializando Firebase: ${e.message}", e)
            showToast("Error de configuración")
            // Continuamos sin Firebase para diagnóstico
        }

        // ✅ CORREGIDO: VERIFICACIÓN DE SESIÓN MÁS ESTRICTA
        try {
            Log.d(TAG, "3. Verificando sesión existente")
            val currentUser = auth.currentUser
            val sessionEmail = SessionManager.getCurrentUserEmail(this)

            Log.d(TAG, "📊 Firebase user: ${currentUser?.email ?: "null"}")
            Log.d(TAG, "📊 SessionManager: ${sessionEmail ?: "null"}")

            // ✅ MODIFICADO: Solo redirigir si AMBAS sesiones están activas y coinciden
            if (currentUser != null && !sessionEmail.isNullOrEmpty()) {
                val firebaseEmail = currentUser.email ?: ""
                val sessionEmailNormalized = sessionEmail.lowercase().trim()

                // ✅ NUEVO: Verificar que los emails coincidan
                if (firebaseEmail.lowercase().trim() == sessionEmailNormalized) {
                    Log.d(TAG, "✅ Sesiones consistentes, redirigiendo a: $firebaseEmail")
                    navigateAccordingToRole(firebaseEmail)
                    return
                } else {
                    Log.w(TAG, "⚠️ Sesiones inconsistentes - Firebase: $firebaseEmail, SessionManager: $sessionEmail")
                    // NO redirigir, mostrar pantalla de login normal
                    showToast("Sesión inconsistente, por favor inicie sesión nuevamente")

                    // ✅ NUEVO: Limpiar sesiones inconsistentes
                    try {
                        auth.signOut()
                        SessionManager.logout(this)
                        Log.d(TAG, "✅ Sesiones inconsistentes limpiadas")
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error limpiando sesiones inconsistentes", e)
                    }
                }
            } else if (currentUser != null && sessionEmail.isNullOrEmpty()) {
                Log.w(TAG, "⚠️ Solo Firebase tiene sesión (posible registro reciente)")
                // ✅ NUEVO: No redirigir automáticamente después del registro
                // Mostrar pantalla de login normal para inicio de sesión manual
                showToast("Por favor inicie sesión manualmente")

                // Opcional: Pre-llenar el email
                try {
                    etEmail.setText(currentUser.email ?: "")
                } catch (e: Exception) {
                    Log.d(TAG, "ℹ️ No se pudo pre-llenar email, vistas no inicializadas aún")
                }
            } else if (currentUser == null && !sessionEmail.isNullOrEmpty()) {
                Log.w(TAG, "⚠️ Solo SessionManager tiene sesión (sesión huérfana)")
                // Limpiar sesión huérfana
                SessionManager.logout(this)
                showToast("Sesión expirada")
            } else {
                Log.d(TAG, "ℹ️ No hay sesión activa, mostrando login")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR verificando sesión: ${e.message}", e)
            // Continuamos con el flujo normal
        }

        try {
            Log.d(TAG, "4. Inicializando vistas")
            initializeViews()
            Log.d(TAG, "✅ 4. Vistas inicializadas")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR inicializando vistas: ${e.message}", e)
            showToast("Error configurando interfaz")
            // Intentamos continuar
        }

        try {
            Log.d(TAG, "5. Configurando Google Sign-In")
            setupGoogleSignIn()
            Log.d(TAG, "✅ 5. Google Sign-In configurado")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR configurando Google Sign-In: ${e.message}", e)
            // Ocultamos el botón de Google si falla
            try {
                btnGoogle.visibility = android.view.View.GONE
            } catch (e2: Exception) {
                Log.e(TAG, "❌ No se pudo ocultar botón Google", e2)
            }
        }

        try {
            Log.d(TAG, "6. Configurando click listeners")
            setupClickListeners()
            Log.d(TAG, "✅ 6. Click listeners configurados")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR configurando listeners: ${e.message}", e)
            showToast("Error en botones")
        }

        Log.d(TAG, "✅ === LOGINACTIVITY CONFIGURADO EXITOSAMENTE ===")
    }

    private fun initializeViews() {
        try {
            etEmail = findViewById(R.id.etEmail)
            etPassword = findViewById(R.id.etPassword)
            btnLogin = findViewById(R.id.btnLogin)
            btnGoogle = findViewById(R.id.btnGoogle)
            btnRegister = findViewById(R.id.btnRegister)

            Log.d(TAG, "✅ Todas las vistas encontradas")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR: No se pudo encontrar una vista", e)
            // Verificamos qué vista específica falla
            try { etEmail = findViewById(R.id.etEmail) } catch (e1: Exception) { Log.e(TAG, "❌ etEmail no encontrado", e1) }
            try { etPassword = findViewById(R.id.etPassword) } catch (e2: Exception) { Log.e(TAG, "❌ etPassword no encontrado", e2) }
            try { btnLogin = findViewById(R.id.btnLogin) } catch (e3: Exception) { Log.e(TAG, "❌ btnLogin no encontrado", e3) }
            try { btnGoogle = findViewById(R.id.btnGoogle) } catch (e4: Exception) { Log.e(TAG, "❌ btnGoogle no encontrado", e4) }
            try { btnRegister = findViewById(R.id.btnRegister) } catch (e5: Exception) { Log.e(TAG, "❌ btnRegister no encontrado", e5) }

            throw e // Relanzamos para manejo superior
        }
    }

    private fun setupGoogleSignIn() {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR en setupGoogleSignIn: ${e.message}", e)
            throw e
        }
    }

    private fun setupClickListeners() {
        try {
            btnLogin.setOnClickListener {
                try {
                    val email = etEmail.text.toString().trim()
                    val password = etPassword.text.toString().trim()
                    Log.d(TAG, "🖱️ Click en Login - Email: $email")

                    if (validateCredentials(email, password)) {
                        loginWithEmail(email, password)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ ERROR en click listener de login: ${e.message}", e)
                    showToast("Error procesando login")
                }
            }

            btnGoogle.setOnClickListener {
                try {
                    signInWithGoogle()
                } catch (e: Exception) {
                    Log.e(TAG, "❌ ERROR en click listener de Google: ${e.message}", e)
                    showToast("Error con Google Sign-In")
                }
            }

            btnRegister.setOnClickListener {
                try {
                    navigateToRegister()
                } catch (e: Exception) {
                    Log.e(TAG, "❌ ERROR en click listener de registro: ${e.message}", e)
                    showToast("Error abriendo registro")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR en setupClickListeners: ${e.message}", e)
            throw e
        }
    }

    private fun validateCredentials(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            etEmail.error = "Email requerido"
            return false
        }

        if (!Usuario.esEmailValido(email)) {
            etEmail.error = "Formato de email inválido"
            return false
        }

        if (password.isEmpty() || password.length < 6) {
            etPassword.error = "La contraseña debe tener al menos 6 caracteres"
            return false
        }

        if (!checkNetworkConnection()) {
            showToast("Sin conexión a internet")
            return false
        }

        return true
    }

    private fun loginWithEmail(email: String, password: String) {
        val emailNormalizado = email.lowercase().trim()
        val passwordLimpia = password.trim()

        Log.d(TAG, "🔐 Intentando login con: $emailNormalizado")

        try {
            btnLogin.isEnabled = false
            btnLogin.text = "Iniciando sesión..."
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error actualizando botón", e)
        }

        auth.signInWithEmailAndPassword(emailNormalizado, passwordLimpia)
            .addOnCompleteListener(this) { task ->
                try {
                    if (task.isSuccessful) {
                        Log.d(TAG, "✅ Login exitoso con Firebase")
                        navigateAccordingToRole(emailNormalizado)
                    } else {
                        Log.e(TAG, "❌ Login falló: ${task.exception?.message}")
                        showToast("Error: ${task.exception?.message ?: "Credenciales inválidas"}")
                        resetLoginButton()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ ERROR en listener de login", e)
                    resetLoginButton()
                    showToast("Error inesperado")
                }
            }
    }

    private fun resetLoginButton() {
        try {
            btnLogin.isEnabled = true
            btnLogin.text = "Iniciar Sesión"
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error reseteando botón", e)
        }
    }

    private fun signInWithGoogle() {
        if (!checkNetworkConnection()) {
            showToast("Sin conexión a internet")
            return
        }
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleGoogleSignInResult(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { token ->
                authenticateWithGoogle(token)
            } ?: showToast("Error en Google Sign-In")
        } catch (e: ApiException) {
            Log.w(TAG, "Google sign in failed", e)
            showToast("Error: ${e.message}")
        }
    }

    private fun authenticateWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val email = auth.currentUser?.email ?: ""
                    navigateAccordingToRole(email)
                } else {
                    showToast("Error en autenticación Google")
                }
            }
    }

    private fun navigateAccordingToRole(email: String) {
        Log.d(TAG, "🎯 Navegando según rol para: $email")

        coroutineScope.launch {
            try {
                val usuario = Usuario.obtenerUsuarioPorNombre(this@LoginActivity, email)
                val rol = usuario?.rol ?: "cliente"
                Log.d(TAG, "📊 Rol determinado: $rol")

                // ✅ CORREGIDO: Usar el método correcto saveUserSession
                try {
                    SessionManager.saveUserSession(this@LoginActivity, email, rol)
                    Log.d(TAG, "✅ Sesión guardada en SessionManager: $email - $rol")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error guardando sesión en SessionManager", e)
                }

                val destino = when (rol.lowercase()) {
                    "admin" -> {
                        showToast("Bienvenido administrador")
                        Intent(this@LoginActivity, AdminActivity::class.java)
                    }
                    else -> {
                        showToast("Bienvenido cliente")
                        Intent(this@LoginActivity, MainActivity::class.java)
                    }
                }

                startActivity(destino)
                finish()
            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR en navigateAccordingToRole: ${e.message}", e)
                showToast("Error determinando rol")

                // Fallback a MainActivity
                try {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } catch (e2: Exception) {
                    Log.e(TAG, "❌ ERROR incluso en fallback", e2)
                }
            }
        }
    }

    private fun checkNetworkConnection(): Boolean {
        return try {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected == true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error verificando conexión", e)
            false
        }
    }

    private fun navigateToRegister() {
        try {
            startActivity(Intent(this, RegistroActivity::class.java))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error navegando a registro", e)
            showToast("Error abriendo registro")
        }
    }

    private fun showToast(message: String) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error mostrando toast: $message", e)
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}



