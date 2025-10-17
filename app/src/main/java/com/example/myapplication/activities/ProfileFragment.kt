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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private lateinit var btnLogout: Button
    private lateinit var tvUserEmail: TextView
    private val auth = Firebase.auth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        btnLogout = view.findViewById(R.id.btnLogout)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)

        // Mostrar el correo del usuario autenticado
        val user = auth.currentUser
        if (user != null) {
            tvUserEmail.text = "Usuario: ${user.email}"
        } else {
            tvUserEmail.text = "Sin sesión activa"
        }

        // Cerrar sesión y volver al login
        btnLogout.setOnClickListener {
            auth.signOut()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }
}
