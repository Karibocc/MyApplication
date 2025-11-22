package com.example.myapplication.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.models.Producto
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AgregarProductoActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etPrecio: EditText
    private lateinit var etStock: EditText
    private lateinit var btnTomarFoto: Button
    private lateinit var btnGuardar: Button
    private lateinit var btnEliminar: Button
    private lateinit var ivFoto: ImageView

    private lateinit var dbHelper: DatabaseHelper
    private var currentPhotoPath: String? = null
    private var photoUri: Uri? = null
    private var productoExistente: Producto? = null
    private var modoEdicion: Boolean = false

    companion object {
        const val EXTRA_PRODUCTO_ID = "producto_id"
    }

    private val takePictureResult = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.let { uri ->
                ivFoto.setImageURI(uri)
                Toast.makeText(this, "Foto capturada exitosamente", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Captura de foto cancelada", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageResult = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            ivFoto.setImageURI(it)
            photoUri = it
            currentPhotoPath = uri.toString()
            Toast.makeText(this, "Imagen seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tomarFotoConCamara()
        } else {
            Toast.makeText(
                this,
                "Se necesita permiso de cámara para tomar fotos",
                Toast.LENGTH_LONG
            ).show()
            mostrarDialogoPermisos()
        }
    }

    private val mediaPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            seleccionarDeGaleria()
        } else {
            Toast.makeText(
                this,
                "Se necesita permiso para acceder a la galería",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_producto)

        initializeViews()
        dbHelper = DatabaseHelper(this)
        verificarModoEdicion()
        setupClickListeners()
    }

    private fun initializeViews() {
        etNombre = findViewById(R.id.etNombre)
        etDescripcion = findViewById(R.id.etDescripcion)
        etPrecio = findViewById(R.id.etPrecio)
        etStock = findViewById(R.id.etStock)
        btnTomarFoto = findViewById(R.id.btnTomarFoto)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnEliminar = findViewById(R.id.btnEliminar)
        ivFoto = findViewById(R.id.ivFoto)
    }

    private fun verificarModoEdicion() {
        val productoId = intent.getIntExtra(EXTRA_PRODUCTO_ID, -1)

        if (productoId != -1) {
            modoEdicion = true
            cargarProductoExistente(productoId)
        } else {
            modoEdicion = false
            btnEliminar.visibility = android.view.View.GONE
        }
    }

    private fun cargarProductoExistente(productoId: Int) {
        productoExistente = dbHelper.obtenerProductoPorId(productoId)

        productoExistente?.let { producto ->
            etNombre.setText(producto.nombre)
            etDescripcion.setText(producto.descripcion)
            etPrecio.setText(producto.precio.toString())
            etStock.setText(producto.stock.toString())

            if (!producto.imagen_path.isNullOrEmpty()) {
                currentPhotoPath = producto.imagen_path
                try {
                    Glide.with(this)
                        .load(producto.imagen_path)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_broken_image)
                        .into(ivFoto)
                } catch (e: Exception) {
                }
            }

            btnGuardar.text = "Actualizar Producto"
            btnEliminar.visibility = android.view.View.VISIBLE

        } ?: run {
            Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupClickListeners() {
        btnTomarFoto.setOnClickListener {
            mostrarOpcionesFoto()
        }

        btnGuardar.setOnClickListener {
            if (modoEdicion) {
                actualizarProducto()
            } else {
                guardarProducto()
            }
        }

        btnEliminar.setOnClickListener {
            mostrarDialogoConfirmarEliminacion()
        }

        ivFoto.setOnClickListener {
            mostrarOpcionesFoto()
        }
    }

    private fun mostrarOpcionesFoto() {
        val options = arrayOf("Tomar Foto", "Elegir de Galería", "Cancelar")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Seleccionar Imagen")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> tomarFotoConCamara()
                    1 -> seleccionarDeGaleria()
                    2 -> dialog.dismiss()
                }
            }
            .setCancelable(true)
            .show()
    }

    private fun tomarFotoConCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            iniciarCamara()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun iniciarCamara() {
        try {
            val photoFile = try {
                createImageFile()
            } catch (ex: IOException) {
                Toast.makeText(this, "Error creando archivo para foto", Toast.LENGTH_SHORT).show()
                return
            }

            photoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )

            takePictureResult.launch(photoUri)

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun seleccionarDeGaleria() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED) {
                abrirGaleria()
            } else {
                mediaPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
                abrirGaleria()
            } else {
                mediaPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun abrirGaleria() {
        pickImageResult.launch("image/*")
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "PRODUCTO_${timeStamp}_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs()
        }

        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun mostrarDialogoPermisos() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Permiso de Cámara Requerido")
            .setMessage("Esta aplicación necesita acceso a la cámara para tomar fotos de productos. ¿Desea abrir la configuración para conceder el permiso?")
            .setPositiveButton("Abrir Configuración") { _, _ ->
                abrirConfiguracionApp()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun abrirConfiguracionApp() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error abriendo configuración", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarProducto() {
        try {
            if (!validarCampos()) return

            val nombre = etNombre.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val precio = etPrecio.text.toString().trim().toDouble()
            val stock = etStock.text.toString().trim().toInt()

            val producto = Producto(
                id = 0,
                nombre = nombre,
                descripcion = descripcion,
                precio = precio,
                imagen_path = currentPhotoPath ?: "",
                stock = stock
            )

            val resultado = dbHelper.insertarProducto(producto)

            if (resultado != -1L) {
                Toast.makeText(this, "Producto '$nombre' guardado exitosamente", Toast.LENGTH_LONG).show()
                limpiarCampos()
            } else {
                Toast.makeText(this, "Error guardando el producto", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error crítico: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun actualizarProducto() {
        try {
            if (!validarCampos()) return

            productoExistente?.let { producto ->
                val nombre = etNombre.text.toString().trim()
                val descripcion = etDescripcion.text.toString().trim()
                val precio = etPrecio.text.toString().trim().toDouble()
                val stock = etStock.text.toString().trim().toInt()

                val filasAfectadas = dbHelper.actualizarProducto(
                    producto.id,
                    nombre,
                    descripcion,
                    precio,
                    currentPhotoPath ?: producto.imagen_path,
                    stock
                )

                if (filasAfectadas > 0) {
                    Toast.makeText(this, "Producto '$nombre' actualizado exitosamente", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error actualizando el producto", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error crítico: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun validarCampos(): Boolean {
        val nombre = etNombre.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()
        val precioTexto = etPrecio.text.toString().trim()
        val stockTexto = etStock.text.toString().trim()

        if (nombre.isEmpty()) {
            etNombre.error = "El nombre es requerido"
            etNombre.requestFocus()
            return false
        }

        if (descripcion.isEmpty()) {
            etDescripcion.error = "La descripción es requerida"
            etDescripcion.requestFocus()
            return false
        }

        if (precioTexto.isEmpty()) {
            etPrecio.error = "El precio es requerido"
            etPrecio.requestFocus()
            return false
        }

        if (stockTexto.isEmpty()) {
            etStock.error = "El stock es requerido"
            etStock.requestFocus()
            return false
        }

        val precio = try {
            precioTexto.toDouble()
        } catch (e: NumberFormatException) {
            etPrecio.error = "Precio inválido"
            etPrecio.requestFocus()
            return false
        }

        if (precio <= 0) {
            etPrecio.error = "El precio debe ser mayor a 0"
            etPrecio.requestFocus()
            return false
        }

        val stock = try {
            stockTexto.toInt()
        } catch (e: NumberFormatException) {
            etStock.error = "Stock inválido"
            etStock.requestFocus()
            return false
        }

        if (stock < 0) {
            etStock.error = "El stock no puede ser negativo"
            etStock.requestFocus()
            return false
        }

        return true
    }

    private fun mostrarDialogoConfirmarEliminacion() {
        productoExistente?.let { producto ->
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Está seguro que desea eliminar el producto \"${producto.nombre}\"? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar") { dialog, _ ->
                    eliminarProducto()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    private fun eliminarProducto() {
        try {
            productoExistente?.let { producto ->
                val filasAfectadas = dbHelper.eliminarProducto(producto.id)

                if (filasAfectadas > 0) {
                    Toast.makeText(this, "Producto '${producto.nombre}' eliminado exitosamente", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error eliminando el producto", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error crítico: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun limpiarCampos() {
        etNombre.text.clear()
        etDescripcion.text.clear()
        etPrecio.text.clear()
        etStock.text.clear()

        ivFoto.setImageResource(android.R.color.transparent)
        currentPhotoPath = null
        photoUri = null

        etNombre.requestFocus()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (::dbHelper.isInitialized) {
                dbHelper.close()
            }
        } catch (e: Exception) {
        }
    }
}