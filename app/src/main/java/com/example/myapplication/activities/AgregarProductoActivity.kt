package com.example.myapplication.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
    private lateinit var ivFoto: ImageView

    private lateinit var dbHelper: DatabaseHelper
    private var currentPhotoPath: String? = null
    private var photoUri: Uri? = null

    companion object {
        private const val TAG = "AgregarProductoActivity"
    }

    // Contract para la cámara
    private val takePictureResult = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            try {
                photoUri?.let { uri ->
                    ivFoto.setImageURI(uri)
                    Log.d(TAG, "✅ Foto capturada y mostrada: $uri")
                    Toast.makeText(this, "Foto capturada exitosamente", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error mostrando la foto: ${e.message}", e)
                Toast.makeText(this, "Error mostrando la foto", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w(TAG, "⚠️ El usuario canceló la captura de foto")
            Toast.makeText(this, "Captura de foto cancelada", Toast.LENGTH_SHORT).show()
        }
    }

    // Contract para seleccionar imagen de la galería
    private val pickImageResult = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                ivFoto.setImageURI(it)
                photoUri = it
                currentPhotoPath = getPathFromUri(it)
                Log.d(TAG, "✅ Imagen seleccionada de galería: $it")
                Toast.makeText(this, "Imagen seleccionada", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error cargando imagen: ${e.message}", e)
                Toast.makeText(this, "Error cargando imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Contract para permisos de cámara
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "✅ Permiso de cámara concedido")
            tomarFotoConCamara()
        } else {
            Log.w(TAG, "❌ Permiso de cámara denegado")
            Toast.makeText(
                this,
                "Se necesita permiso de cámara para tomar fotos. Puede activarlo en Configuración > Aplicaciones.",
                Toast.LENGTH_LONG
            ).show()
            mostrarDialogoPermisos()
        }
    }

    // Contract para permisos de medios (Android 13+)
    private val mediaPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "✅ Permiso de medios concedido")
            seleccionarDeGaleria()
        } else {
            Log.w(TAG, "❌ Permiso de medios denegado")
            Toast.makeText(
                this,
                "Se necesita permiso para acceder a la galería.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "🚀 AgregarProductoActivity iniciada")

        try {
            setContentView(R.layout.activity_agregar_producto)
            Log.d(TAG, "✅ Layout cargado exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR cargando layout: ${e.message}", e)
            crearLayoutMinimo()
            return
        }

        try {
            dbHelper = DatabaseHelper(this)
            initializeViews()
            setupClickListeners()
            Log.d(TAG, "✅ AgregarProductoActivity configurada")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR configurando activity: ${e.message}", e)
            Toast.makeText(this, "Error configurando la actividad", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initializeViews() {
        try {
            etNombre = findViewById(R.id.etNombre)
            etDescripcion = findViewById(R.id.etDescripcion)
            etPrecio = findViewById(R.id.etPrecio)
            etStock = findViewById(R.id.etStock)
            btnTomarFoto = findViewById(R.id.btnTomarFoto)
            btnGuardar = findViewById(R.id.btnGuardar)
            ivFoto = findViewById(R.id.ivFoto)

            Log.d(TAG, "✅ Vistas inicializadas correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR inicializando vistas: ${e.message}", e)
            throw e
        }
    }

    private fun setupClickListeners() {
        // Botón Tomar Foto - ahora con menú de opciones
        btnTomarFoto.setOnClickListener {
            try {
                Log.d(TAG, "🖱️ Clic en Tomar Foto")
                mostrarOpcionesFoto()
            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR en botón tomar foto: ${e.message}", e)
                Toast.makeText(this, "Error accediendo a la cámara", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón Guardar Producto
        btnGuardar.setOnClickListener {
            try {
                Log.d(TAG, "🖱️ Clic en Guardar Producto")
                guardarProducto()
            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR en botón guardar: ${e.message}", e)
                Toast.makeText(this, "Error guardando producto", Toast.LENGTH_SHORT).show()
            }
        }

        // Click en la imagen para cambiar foto
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
        try {
            Log.d(TAG, "📸 Iniciando cámara...")

            // Verificar permisos de cámara
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
                // Permiso ya concedido, proceder con la cámara
                iniciarCamara()
            } else {
                // Solicitar permiso
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR iniciando cámara: ${e.message}", e)
            Toast.makeText(this, "Error iniciando cámara: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun iniciarCamara() {
        try {
            // Crear archivo para la foto
            val photoFile = try {
                createImageFile()
            } catch (ex: IOException) {
                Log.e(TAG, "❌ Error creando archivo: ${ex.message}", ex)
                Toast.makeText(this, "Error creando archivo para foto", Toast.LENGTH_SHORT).show()
                return
            }

            // Crear URI usando FileProvider
            photoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )

            // Lanzar la cámara
            takePictureResult.launch(photoUri)

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR en iniciarCamara: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun seleccionarDeGaleria() {
        try {
            Log.d(TAG, "🖼️ Abriendo galería...")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ - Solo necesita permiso READ_MEDIA_IMAGES
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                    abrirGaleria()
                } else {
                    mediaPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                // Android 12 o inferior
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                    abrirGaleria()
                } else {
                    mediaPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR abriendo galería: ${e.message}", e)
            Toast.makeText(this, "Error abriendo galería", Toast.LENGTH_SHORT).show()
        }
    }

    private fun abrirGaleria() {
        pickImageResult.launch("image/*")
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Crear un nombre de archivo único y descriptivo
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "PRODUCTO_${timeStamp}_"

        // ✅ MEJORA: Usar el directorio de imágenes externo definido en file_paths.xml como "my_images"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        // ✅ MEJORA: Verificar que el directorio existe, si no, crearlo
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs()
        }

        return File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Guardar la ruta del archivo para uso posterior
            currentPhotoPath = absolutePath
            Log.d(TAG, "📁 Archivo de imagen creado: $currentPhotoPath")
            Log.d(TAG, "📁 Directorio utilizado: ${storageDir?.absolutePath}")
            Log.d(TAG, "📁 Tamaño del directorio: ${storageDir?.list()?.size ?: 0} archivos")
        }
    }

    private fun getPathFromUri(uri: Uri): String {
        // Para Android 10+, es mejor guardar el URI directamente
        // ya que el acceso a rutas reales está restringido
        return uri.toString()
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
            Log.e(TAG, "❌ Error abriendo configuración: ${e.message}", e)
            Toast.makeText(this, "Error abriendo configuración", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarProducto() {
        try {
            Log.d(TAG, "💾 Intentando guardar producto...")

            // Obtener valores de los campos
            val nombre = etNombre.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val precioTexto = etPrecio.text.toString().trim()
            val stockTexto = etStock.text.toString().trim()

            // Validaciones
            if (nombre.isEmpty()) {
                etNombre.error = "El nombre es requerido"
                etNombre.requestFocus()
                Log.w(TAG, "⚠️ Validación fallida: nombre vacío")
                return
            }

            if (descripcion.isEmpty()) {
                etDescripcion.error = "La descripción es requerida"
                etDescripcion.requestFocus()
                Log.w(TAG, "⚠️ Validación fallida: descripción vacía")
                return
            }

            if (precioTexto.isEmpty()) {
                etPrecio.error = "El precio es requerido"
                etPrecio.requestFocus()
                Log.w(TAG, "⚠️ Validación fallida: precio vacío")
                return
            }

            if (stockTexto.isEmpty()) {
                etStock.error = "El stock es requerido"
                etStock.requestFocus()
                Log.w(TAG, "⚠️ Validación fallida: stock vacío")
                return
            }

            // Convertir y validar precio
            val precio = try {
                precioTexto.toDouble()
            } catch (e: NumberFormatException) {
                etPrecio.error = "Precio inválido"
                etPrecio.requestFocus()
                Log.w(TAG, "⚠️ Validación fallida: precio inválido")
                return
            }

            if (precio <= 0) {
                etPrecio.error = "El precio debe ser mayor a 0"
                etPrecio.requestFocus()
                Log.w(TAG, "⚠️ Validación fallida: precio <= 0")
                return
            }

            // Convertir y validar stock
            val stock = try {
                stockTexto.toInt()
            } catch (e: NumberFormatException) {
                etStock.error = "Stock inválido"
                etStock.requestFocus()
                Log.w(TAG, "⚠️ Validación fallida: stock inválido")
                return
            }

            if (stock < 0) {
                etStock.error = "El stock no puede ser negativo"
                etStock.requestFocus()
                Log.w(TAG, "⚠️ Validación fallida: stock negativo")
                return
            }

            Log.d(TAG, "📊 Datos validados - Nombre: $nombre, Precio: $precio, Stock: $stock")

            // Crear objeto Producto con la ruta de la imagen
            val producto = Producto(
                id = 0,
                nombre = nombre,
                descripcion = descripcion,
                precio = precio,
                imagen_path = currentPhotoPath ?: "", // Guardar la ruta de la imagen
                stock = stock
            )

            // Guardar en la base de datos
            val resultado = dbHelper.insertarProducto(producto)

            if (resultado != -1L) {
                Log.d(TAG, "✅ Producto guardado exitosamente - ID: $resultado")
                Toast.makeText(this, "✅ Producto '$nombre' guardado exitosamente", Toast.LENGTH_LONG).show()
                limpiarCampos()

                // Opcional: regresar después de guardar
                // finish()
            } else {
                Log.e(TAG, "❌ ERROR guardando producto en la base de datos")
                Toast.makeText(this, "❌ Error guardando el producto", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e(TAG, "💥 ERROR CRÍTICO guardando producto: ${e.message}", e)
            Toast.makeText(this, "Error crítico: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun limpiarCampos() {
        try {
            etNombre.text.clear()
            etDescripcion.text.clear()
            etPrecio.text.clear()
            etStock.text.clear()

            // Limpiar imagen
            ivFoto.setImageResource(android.R.color.transparent)
            currentPhotoPath = null
            photoUri = null

            Log.d(TAG, "🧹 Campos limpiados")
            etNombre.requestFocus()

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR limpiando campos: ${e.message}", e)
        }
    }

    private fun crearLayoutMinimo() {
        try {
            Log.d(TAG, "🔄 Creando layout mínimo de emergencia...")

            val textView = android.widget.TextView(this).apply {
                text = "Agregar Producto\n(Modo emergencia - Layout no encontrado)"
                textSize = 16f
                setPadding(50, 50, 50, 50)
                gravity = android.view.Gravity.CENTER
            }

            val button = Button(this).apply {
                text = "Volver al Menú"
                setOnClickListener {
                    Toast.makeText(this@AgregarProductoActivity, "Regresando...", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            val layout = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                setBackgroundColor(android.graphics.Color.WHITE)
                addView(textView)
                addView(button)
            }

            setContentView(layout)
            Log.d(TAG, "✅ Layout mínimo creado exitosamente")

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR CRÍTICO en layout mínimo", e)
            Toast.makeText(this, "Error crítico en la aplicación", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (::dbHelper.isInitialized) {
                dbHelper.close()
                Log.d(TAG, "🔒 DatabaseHelper cerrado")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR cerrando database: ${e.message}")
        }
    }
}