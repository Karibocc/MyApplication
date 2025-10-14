package com.example.myapplication.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.myapplication.R
import com.example.myapplication.database.DatabaseHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AddProductActivity : AppCompatActivity() {

    private lateinit var editTextNombre: EditText
    private lateinit var editTextDescripcion: EditText
    private lateinit var editTextPrecio: EditText
    private lateinit var editTextStock: EditText // Nuevo campo para el stock
    private lateinit var imageViewProducto: ImageView
    private lateinit var buttonSeleccionarImagen: Button
    private lateinit var buttonCapturarImagen: Button
    private lateinit var buttonGuardar: Button

    private var imagenPath: String? = null
    private var imageUri: Uri? = null

    private val PICK_IMAGE_REQUEST = 1
    private val CAPTURE_IMAGE_REQUEST = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        initViews()
        setupButtons()
    }

    private fun initViews() {
        editTextNombre = findViewById(R.id.editTextNombre)
        editTextDescripcion = findViewById(R.id.editTextDescripcion)
        editTextPrecio = findViewById(R.id.editTextPrecio)
        editTextStock = findViewById(R.id.etStockProducto) // Asegúrate de tener este ID en tu layout
        imageViewProducto = findViewById(R.id.imageViewProducto)
        buttonSeleccionarImagen = findViewById(R.id.buttonSeleccionarImagen)
        buttonCapturarImagen = findViewById(R.id.buttonCapturarImagen)
        buttonGuardar = findViewById(R.id.buttonGuardar)
    }

    private fun setupButtons() {
        buttonSeleccionarImagen.setOnClickListener { seleccionarImagen() }
        buttonCapturarImagen.setOnClickListener { capturarImagen() }
        buttonGuardar.setOnClickListener { guardarProducto() }
    }

    private fun seleccionarImagen() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun capturarImagen() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val imageFile = File.createTempFile(
            "img_${System.currentTimeMillis()}",
            ".jpg",
            externalCacheDir
        )
        imageUri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            imageFile
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, CAPTURE_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when {
            requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null -> {
                data.data?.let { uri ->
                    imageViewProducto.setImageURI(uri)
                    imagenPath = guardarImagenInternamente(uri)
                }
            }
            requestCode == CAPTURE_IMAGE_REQUEST && resultCode == Activity.RESULT_OK -> {
                imageUri?.let { uri ->
                    imageViewProducto.setImageURI(uri)
                    imagenPath = guardarImagenInternamente(uri)
                }
            }
        }
    }

    private fun guardarImagenInternamente(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val fileDir = File(filesDir, "product_images").apply {
                if (!exists()) mkdir()
            }
            val imageFile = File(fileDir, "img_${System.currentTimeMillis()}.jpg")

            inputStream?.use { input ->
                FileOutputStream(imageFile).use { output ->
                    input.copyTo(output)
                }
            }
            imageFile.absolutePath
        } catch (e: IOException) {
            Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun guardarProducto() {
        if (!validarCampos()) return

        val nombre = editTextNombre.text.toString().trim()
        val descripcion = editTextDescripcion.text.toString().trim()
        val precio = editTextPrecio.text.toString().trim().toDouble()
        val stock = editTextStock.text.toString().trim().toIntOrNull() ?: 0

        val dbHelper = DatabaseHelper(this)
        val id = dbHelper.insertarProducto(nombre, descripcion, precio, imagenPath ?: "", stock)

        if (id != -1L) {
            Toast.makeText(this, "Producto guardado con éxito", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "Error al guardar el producto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validarCampos(): Boolean {
        var isValid = true

        if (editTextNombre.text.toString().trim().isEmpty()) {
            editTextNombre.error = "El nombre es requerido"
            editTextNombre.requestFocus()
            isValid = false
        }

        if (editTextPrecio.text.toString().trim().isEmpty()) {
            editTextPrecio.error = "El precio es requerido"
            if (isValid) editTextPrecio.requestFocus()
            isValid = false
        } else {
            try {
                editTextPrecio.text.toString().trim().toDouble()
            } catch (e: NumberFormatException) {
                editTextPrecio.error = "Precio inválido"
                if (isValid) editTextPrecio.requestFocus()
                isValid = false
            }
        }

        if (editTextStock.text.toString().trim().isEmpty()) {
            editTextStock.error = "El stock es requerido"
            if (isValid) editTextStock.requestFocus()
            isValid = false
        } else {
            try {
                editTextStock.text.toString().trim().toInt()
            } catch (e: NumberFormatException) {
                editTextStock.error = "Stock inválido"
                if (isValid) editTextStock.requestFocus()
                isValid = false
            }
        }

        return isValid
    }

    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, AddProductActivity::class.java)
            activity.startActivity(intent)
        }
    }
}