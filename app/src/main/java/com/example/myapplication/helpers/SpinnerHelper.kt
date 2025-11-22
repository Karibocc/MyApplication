package com.example.myapplication.helpers

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat

object SpinnerHelper {

    fun configurarSpinnerRoles(context: Context, spinner: Spinner, onRolSeleccionado: (String) -> Unit = {}) {
        val roles = arrayOf("Administrador", "Usuario", "Invitado")

        val adapter = object : ArrayAdapter<String>(
            context,
            android.R.layout.simple_spinner_item,
            roles
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(ContextCompat.getColor(context, android.R.color.black)) // Texto negro
                textView.textSize = 16f
                view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white)) // Fondo blanco
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(ContextCompat.getColor(context, android.R.color.black)) // Texto negro en dropdown
                textView.textSize = 16f
                textView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white)) // Fondo blanco en dropdown
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Configurar el fondo del spinner
        spinner.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val rolSeleccionado = roles[position]
                Log.d("SPINNER_HELPER", "Rol seleccionado: $rolSeleccionado")
                onRolSeleccionado(rolSeleccionado)

                // Actualizar el texto mostrado en el spinner
                if (view is TextView) {
                    view.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("SPINNER_HELPER", "No se seleccionó ningún rol")
            }
        }
    }

    fun seleccionarRol(spinner: Spinner, rol: String) {
        val adapter = spinner.adapter as? ArrayAdapter<*>
        if (adapter != null) {
            for (i in 0 until adapter.count) {
                if (adapter.getItem(i) == rol) {
                    spinner.setSelection(i)
                    Log.d("SPINNER_HELPER", "Rol seleccionado programáticamente: $rol en posición $i")
                    break
                }
            }
        } else {
            Log.e("SPINNER_HELPER", "Adapter es nulo, no se puede seleccionar rol")
        }
    }

    fun obtenerRoles(): Array<String> {
        return arrayOf("Administrador", "Usuario", "Invitado")
    }

    fun obtenerPosicionRol(rol: String): Int {
        val roles = obtenerRoles()
        return roles.indexOfFirst { it.equals(rol, ignoreCase = true) }.takeIf { it >= 0 } ?: 0
    }
}
