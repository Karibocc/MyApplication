package com.example.myapplication.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.activities.LoginActivity
import com.example.myapplication.managers.SessionManager
import com.example.myapplication.models.Usuario
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private lateinit var btnLogout: Button
    private lateinit var tvUserEmail: TextView
    private val auth = Firebase.auth
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        btnLogout = view.findViewById(R.id.btnLogout)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)

        cargarInformacionUsuario()

        btnLogout.setOnClickListener {
            cerrarSesion()
        }

        return view
    }

    private fun cargarInformacionUsuario() {
        coroutineScope.launch {
            try {
                val user = auth.currentUser
                if (user != null) {
                    val usuarioLocal = withContext(Dispatchers.IO) {
                        Usuario.obtenerUsuarioPorNombre(requireContext(), user.email ?: "")
                    }

                    if (usuarioLocal != null) {
                        tvUserEmail.text = "Usuario: ${usuarioLocal.username}\nRol: ${usuarioLocal.rol}"
                    } else {
                        val sessionEmail = SessionManager.getCurrentUserEmail(requireContext())
                        val sessionRole = SessionManager.getCurrentUserRole(requireContext())

                        if (!sessionEmail.isNullOrEmpty()) {
                            tvUserEmail.text = "Usuario: $sessionEmail\nRol: ${sessionRole ?: "cliente"}"
                        } else {
                            tvUserEmail.text = "Usuario Firebase: ${user.email}"
                        }
                    }
                } else {
                    val sessionEmail = SessionManager.getCurrentUserEmail(requireContext())
                    val sessionRole = SessionManager.getCurrentUserRole(requireContext())

                    if (!sessionEmail.isNullOrEmpty()) {
                        tvUserEmail.text = "Usuario: $sessionEmail\nRol: ${sessionRole ?: "cliente"}"
                    } else {
                        tvUserEmail.text = "Sin sesión activa"
                    }
                }
            } catch (e: Exception) {
                try {
                    val sessionEmail = SessionManager.getCurrentUserEmail(requireContext())
                    if (!sessionEmail.isNullOrEmpty()) {
                        tvUserEmail.text = "Usuario: $sessionEmail"
                    } else {
                        tvUserEmail.text = "Error cargando información"
                    }
                } catch (e2: Exception) {
                    tvUserEmail.text = "Sin sesión activa"
                }
            }
        }
    }

    private fun cerrarSesion() {
        try {
            auth.signOut()

            SessionManager.logout(requireContext())
            SessionManager.clearSession(requireContext())

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            activity?.finish()

        } catch (e: Exception) {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}