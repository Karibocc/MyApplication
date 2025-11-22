package com.example.myapplication.activities

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.myapplication.R
import com.example.myapplication.database.DatabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class GestionProductoActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var etNombre: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etPrecio: EditText
    private lateinit var etStock: EditText
    private lateinit var btnTomarFoto: Button
    private lateinit var btnGuardar: Button
    private lateinit var btnEliminar: Button
    private lateinit var ivFoto: ImageView
    private lateinit var progressBar: ProgressBar

    private var esEdicion: Boolean = false
    private var productoId: Int = -1
    private var imagenSeleccionada: String? = null
    private var currentPhotoPath: String? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tomarFotoConCamara()
        } else {
            Toast.makeText(this, "Se necesita permiso de cámara para tomar fotos", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoPath?.let { path ->
                imagenSeleccionada = path
                ivFoto.setImageURI(Uri.parse(path))
                Toast.makeText(this, "Foto guardada", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No se pudo tomar la foto", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imagenSeleccionada = it.toString()
            ivFoto.setImageURI(it)
            Toast.makeText(this, "Imagen seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_producto)

        db = DatabaseHelper(this)
        initViews()
        setupModo()
        setupListeners()
    }

    private fun initViews() {
        etNombre = findViewById(R.id.etNombre)
        etDescripcion = findViewById(R.id.etDescripcion)
        etPrecio = findViewById(R.id.etPrecio)
        etStock = findViewById(R.id.etStock)
        btnTomarFoto = findViewById(R.id.btnTomarFoto)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnEliminar = findViewById(R.id.btnEliminar)
        ivFoto = findViewById(R.id.ivFoto)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupModo() {
        esEdicion = intent.getBooleanExtra("MODO_EDICION", false)
        productoId = intent.getIntExtra("PRODUCTO_ID", -1)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (esEdicion) {
            supportActionBar?.title = "Editar Producto"
            btnEliminar.visibility = View.VISIBLE
            btnGuardar.text = "Actualizar Producto"
        } else {
            supportActionBar?.title = "Agregar Producto"
            btnEliminar.visibility = View.GONE
            btnGuardar.text = "Guardar Producto"
        }

        if (esEdicion && productoId != -1) {
            cargarProductoParaEditar()
        }
    }

    private fun setupListeners() {
        btnTomarFoto.setOnClickListener { mostrarOpcionesImagen() }
        btnGuardar.setOnClickListener { guardarProducto() }
        btnEliminar.setOnClickListener { mostrarDialogoEliminar() }
    }

    private fun mostrarOpcionesImagen() {
        val options = arrayOf("Tomar Foto", "Elegir de Galería", "Cancelar")

        AlertDialog.Builder(this)
            .setTitle("Seleccionar Imagen")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> verificarPermisosCamara()
                    1 -> seleccionarImagenGaleria()
                }
            }
            .show()
    }

    private fun verificarPermisosCamara() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                tomarFotoConCamara()
            }
            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    private fun tomarFotoConCamara() {
        try {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                Toast.makeText(this, "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show()
                null
            }

            photoFile?.let { file ->
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    file
                )
                currentPhotoPath = file.absolutePath
                takePictureLauncher.launch(photoURI)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al abrir cámara: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun seleccionarImagenGaleria() {
        pickImageLauncher.launch("image/*")
    }

    private fun cargarProductoParaEditar() {
        mostrarLoading(true)

        coroutineScope.launch {
            try {
                val producto = withContext(Dispatchers.IO) {
                    db.obtenerProductoPorId(productoId)
                }

                producto?.let {
                    etNombre.setText(it.nombre)
                    etDescripcion.setText(it.descripcion)
                    etPrecio.setText(it.precio.toString())
                    etStock.setText(it.stock.toString())

                    it.imagen_path?.let { imagenPath ->
                        if (imagenPath.isNotEmpty()) {
                            imagenSeleccionada = imagenPath
                            try {
                                ivFoto.setImageURI(Uri.parse(imagenPath))
                            } catch (e: Exception) {
                                ivFoto.setImageResource(R.drawable.ic_image_placeholder)
                            }
                        }
                    }
                } ?: run {
                    Toast.makeText(this@GestionProductoActivity, "Producto no encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@GestionProductoActivity, "Error cargando producto: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            } finally {
                mostrarLoading(false)
            }
        }
    }

    private fun guardarProducto() {
        val nombre = etNombre.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()
        val precio = etPrecio.text.toString().toDoubleOrNull()
        val stock = etStock.text.toString().toIntOrNull()

        if (validarFormulario(nombre, descripcion, precio, stock)) {
            mostrarLoading(true)

            coroutineScope.launch {
                try {
                    val exito = withContext(Dispatchers.IO) {
                        if (esEdicion) {
                            val filasAfectadas = db.actualizarProducto(
                                productoId,
                                nombre,
                                descripcion,
                                precio ?: 0.0,
                                imagenSeleccionada,
                                stock ?: 0
                            )
                            filasAfectadas > 0
                        } else {
                            val nuevoId = db.insertarProducto(
                                nombre,
                                descripcion,
                                precio ?: 0.0,
                                imagenSeleccionada,
                                stock ?: 0
                            )
                            nuevoId > 0
                        }
                    }

                    if (exito) {
                        val mensaje = if (esEdicion) "Producto actualizado correctamente" else "Producto agregado correctamente"
                        Toast.makeText(this@GestionProductoActivity, mensaje, Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        val mensaje = if (esEdicion) "Error al actualizar producto" else "Error al guardar producto"
                        Toast.makeText(this@GestionProductoActivity, mensaje, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@GestionProductoActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    mostrarLoading(false)
                }
            }
        }
    }

    private fun validarFormulario(nombre: String, descripcion: String, precio: Double?, stock: Int?): Boolean {
        var esValido = true

        if (nombre.isEmpty()) {
            etNombre.error = "Nombre requerido"
            esValido = false
        }

        if (descripcion.isEmpty()) {
            etDescripcion.error = "Descripción requerida"
            esValido = false
        }

        if (precio == null || precio <= 0) {
            etPrecio.error = "Precio válido requerido"
            esValido = false
        }

        if (stock == null || stock < 0) {
            etStock.error = "Stock válido requerido"
            esValido = false
        }

        return esValido
    }

    private fun mostrarDialogoEliminar() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Producto")
            .setMessage("¿Estás seguro de que quieres eliminar este producto? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarProducto()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarProducto() {
        mostrarLoading(true)

        coroutineScope.launch {
            try {
                val eliminado = withContext(Dispatchers.IO) {
                    db.eliminarProducto(productoId) > 0
                }

                if (eliminado) {
                    Toast.makeText(this@GestionProductoActivity, "Producto eliminado correctamente", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@GestionProductoActivity, "Error al eliminar producto", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@GestionProductoActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                mostrarLoading(false)
            }
        }
    }

    private fun mostrarLoading(mostrar: Boolean) {
        progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
        btnGuardar.isEnabled = !mostrar
        btnEliminar.isEnabled = !mostrar
        btnTomarFoto.isEnabled = !mostrar
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        db.close()
    }
}