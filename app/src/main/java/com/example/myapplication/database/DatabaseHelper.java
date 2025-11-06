package com.example.myapplication.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.util.Log;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    // ===================== CONFIGURACIÓN DE BASE DE DATOS =====================
    private static final String DATABASE_NAME = "miapplication.db";
    private static final int DATABASE_VERSION = 7;

    // ===================== TABLA PRODUCTOS =====================
    public static final String TABLE_PRODUCTOS = "productos";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_DESCRIPCION = "descripcion";
    public static final String COLUMN_PRECIO = "precio";
    public static final String COLUMN_IMAGEN_PATH = "imagen_path";
    public static final String COLUMN_STOCK = "stock";
    public static final String COLUMN_CANTIDAD = "cantidad";

    // ===================== TABLA USUARIOS =====================
    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COLUMN_USUARIO_ID = "id_usuario";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_SALT = "salt";
    public static final String COLUMN_ROL = "rol";

    // ===================== TABLA CARRITO =====================
    public static final String TABLE_CARRITO = "carrito";
    public static final String COLUMN_CARRITO_ID = "id_carrito";

    // ===================== CREACIÓN DE TABLAS =====================
    private static final String TABLE_CREATE_PRODUCTOS =
            "CREATE TABLE " + TABLE_PRODUCTOS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NOMBRE + " TEXT NOT NULL, " +
                    COLUMN_DESCRIPCION + " TEXT, " +
                    COLUMN_PRECIO + " REAL NOT NULL, " +
                    COLUMN_IMAGEN_PATH + " TEXT, " +
                    COLUMN_STOCK + " INTEGER DEFAULT 0, " +
                    COLUMN_CANTIDAD + " INTEGER DEFAULT 1);";

    private static final String TABLE_CREATE_USUARIOS =
            "CREATE TABLE " + TABLE_USUARIOS + " (" +
                    COLUMN_USUARIO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_SALT + " TEXT, " +
                    COLUMN_ROL + " TEXT NOT NULL);";

    private static final String TABLE_CREATE_CARRITO =
            "CREATE TABLE " + TABLE_CARRITO + " (" +
                    COLUMN_CARRITO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_ID + " INTEGER NOT NULL, " +
                    COLUMN_CANTIDAD + " INTEGER DEFAULT 1, " +
                    "FOREIGN KEY(" + COLUMN_ID + ") REFERENCES " +
                    TABLE_PRODUCTOS + "(" + COLUMN_ID + "));";

    // ===================== CONSTRUCTOR =====================
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // ===================== CREACIÓN Y ACTUALIZACIÓN =====================
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_PRODUCTOS);
        db.execSQL(TABLE_CREATE_USUARIOS);
        db.execSQL(TABLE_CREATE_CARRITO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2)
            db.execSQL("ALTER TABLE " + TABLE_PRODUCTOS + " ADD COLUMN " + COLUMN_IMAGEN_PATH + " TEXT;");
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_PRODUCTOS + " ADD COLUMN " + COLUMN_STOCK + " INTEGER DEFAULT 0;");
            db.execSQL(TABLE_CREATE_CARRITO);
        }
        if (oldVersion < 4)
            db.execSQL("ALTER TABLE " + TABLE_PRODUCTOS + " ADD COLUMN " + COLUMN_CANTIDAD + " INTEGER DEFAULT 1;");
        if (oldVersion < 5)
            db.execSQL("ALTER TABLE " + TABLE_USUARIOS + " ADD COLUMN " + COLUMN_SALT + " TEXT;");
    }

    // ===================== MÉTODOS AUXILIARES DE SEGURIDAD =====================
    private String generarSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        StringBuilder sb = new StringBuilder();
        for (byte b : salt) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes());
            byte[] bytes = digest.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error generando hash: " + e.getMessage());
            return password;
        }
    }

    // ===================== MÉTODOS DE PRODUCTOS =====================

    // 🔹 MÉTODO ORIGINAL (para compatibilidad)
    public long insertarProducto(String nombre, String descripcion, double precio, String imagenPath, int stock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_DESCRIPCION, descripcion);
        values.put(COLUMN_PRECIO, precio);
        values.put(COLUMN_IMAGEN_PATH, imagenPath);
        values.put(COLUMN_STOCK, stock);
        values.put(COLUMN_CANTIDAD, 1);
        return db.insert(TABLE_PRODUCTOS, null, values);
    }

    // 🔹 NUEVO MÉTODO: INSERTAR PRODUCTO CON OBJETO PRODUCTO (SOLUCIÓN AL PROBLEMA)
    public long insertarProducto(com.example.myapplication.models.Producto producto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE, producto.getNombre());
        values.put(COLUMN_DESCRIPCION, producto.getDescripcion());
        values.put(COLUMN_PRECIO, producto.getPrecio());
        values.put(COLUMN_IMAGEN_PATH, producto.getImagen_path());
        values.put(COLUMN_STOCK, producto.getStock());
        values.put(COLUMN_CANTIDAD, producto.getCantidad());
        return db.insert(TABLE_PRODUCTOS, null, values);
    }

    // ====== MÉTODO PARA ACTUALIZAR PRODUCTO ======
    public int actualizarProducto(int productoId, String nombre, String descripcion, double precio, String imagenPath, int stock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_DESCRIPCION, descripcion);
        values.put(COLUMN_PRECIO, precio);
        values.put(COLUMN_IMAGEN_PATH, imagenPath);
        values.put(COLUMN_STOCK, stock);
        return db.update(TABLE_PRODUCTOS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(productoId)});
    }

    // ====== ELIMINAR PRODUCTO ======
    public int eliminarProducto(int productoId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Antes de eliminar, eliminamos del carrito si existe
        db.delete(TABLE_CARRITO, COLUMN_ID + " = ?", new String[]{String.valueOf(productoId)});
        return db.delete(TABLE_PRODUCTOS, COLUMN_ID + " = ?", new String[]{String.valueOf(productoId)});
    }

    public Cursor obtenerTodosLosProductos() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PRODUCTOS, null, null, null, null, null, COLUMN_NOMBRE + " ASC");
    }

    public int obtenerStockProducto(int productoId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_PRODUCTOS, new String[]{COLUMN_STOCK},
                    COLUMN_ID + " = ?", new String[]{String.valueOf(productoId)},
                    null, null, null);
            int stock = 0;
            if (cursor != null && cursor.moveToFirst()) {
                stock = cursor.getInt(0);
            }
            return stock;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void actualizarStockProducto(int productoId, int nuevoStock) {
        if (nuevoStock < 0) {
            Log.w("DatabaseHelper", "Intento de establecer stock negativo: " + nuevoStock);
            nuevoStock = 0;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STOCK, nuevoStock);
        db.update(TABLE_PRODUCTOS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(productoId)});
    }

    // ===================== MÉTODOS DE USUARIOS =====================
    public boolean usuarioExiste(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USUARIOS, new String[]{COLUMN_USERNAME},
                    COLUMN_USERNAME + " = ?", new String[]{username.toLowerCase(Locale.ROOT).trim()},
                    null, null, null);
            boolean existe = cursor != null && cursor.moveToFirst();
            return existe;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public boolean validarUsuario(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USUARIOS,
                    new String[]{COLUMN_PASSWORD, COLUMN_SALT},
                    COLUMN_USERNAME + " = ?",
                    new String[]{username.toLowerCase(Locale.ROOT).trim()},
                    null, null, null);

            boolean valido = false;
            if (cursor != null && cursor.moveToFirst()) {
                String hashAlmacenado = cursor.getString(0);
                String salt = cursor.getString(1);
                String hashIngresado = hashPassword(password, salt);
                valido = hashAlmacenado.equals(hashIngresado);
            }
            return valido;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public String obtenerRol(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USUARIOS,
                    new String[]{COLUMN_ROL},
                    COLUMN_USERNAME + " = ?",
                    new String[]{username.toLowerCase(Locale.ROOT).trim()},
                    null, null, null);

            String rol = null;
            if (cursor != null && cursor.moveToFirst()) {
                rol = cursor.getString(0);
            }
            return rol;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // ===================== MÉTODO: INSERTAR USUARIO =====================
    public long insertarUsuario(String username, String password, String rol) {
        SQLiteDatabase db = this.getWritableDatabase();
        String salt = generarSalt(); // Genera un salt único para cada usuario
        String hash = hashPassword(password, salt); // Hashea la contraseña con el salt

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username.toLowerCase(Locale.ROOT).trim());
        values.put(COLUMN_PASSWORD, hash);
        values.put(COLUMN_SALT, salt);
        values.put(COLUMN_ROL, rol);

        return db.insert(TABLE_USUARIOS, null, values);
    }

    // ===================== MÉTODO: OBTENER USUARIO POR NOMBRE =====================
    public Usuario obtenerUsuarioPorNombre(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USUARIOS,
                    new String[]{COLUMN_USUARIO_ID, COLUMN_USERNAME, COLUMN_PASSWORD, COLUMN_SALT, COLUMN_ROL},
                    COLUMN_USERNAME + " = ?",
                    new String[]{username.toLowerCase(Locale.ROOT).trim()},
                    null, null, null);

            Usuario usuario = null;
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USUARIO_ID));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
                String password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
                String salt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SALT));
                String rol = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROL));
                usuario = new Usuario(id, nombre, password, salt, rol);
            }
            return usuario;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // ===================== MÉTODO: OBTENER TODOS LOS USUARIOS =====================
    public Cursor obtenerTodosLosUsuarios() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USUARIOS,
                new String[]{COLUMN_USUARIO_ID, COLUMN_USERNAME, COLUMN_ROL},
                null, null, null, null,
                COLUMN_USERNAME + " ASC");
    }

    // ===================== MÉTODO PARA ACTUALIZAR USUARIO =====================
    public int actualizarUsuario(String username, String nuevoRol) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ROL, nuevoRol);

        return db.update(TABLE_USUARIOS, values,
                COLUMN_USERNAME + " = ?",
                new String[]{username.toLowerCase(Locale.ROOT).trim()});
    }

    // ===================== MÉTODO PARA ELIMINAR USUARIO =====================
    public int eliminarUsuario(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_USUARIOS,
                COLUMN_USERNAME + " = ?",
                new String[]{username.toLowerCase(Locale.ROOT).trim()});
    }

    // ===================== MÉTODO: OBTENER USUARIO POR ID =====================
    public Usuario obtenerUsuarioPorId(int usuarioId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USUARIOS,
                    new String[]{COLUMN_USUARIO_ID, COLUMN_USERNAME, COLUMN_PASSWORD, COLUMN_SALT, COLUMN_ROL},
                    COLUMN_USUARIO_ID + " = ?",
                    new String[]{String.valueOf(usuarioId)},
                    null, null, null);

            Usuario usuario = null;
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USUARIO_ID));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
                String password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
                String salt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SALT));
                String rol = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROL));
                usuario = new Usuario(id, nombre, password, salt, rol);
            }
            return usuario;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // ===================== MÉTODO: CAMBIAR CONTRASEÑA =====================
    public int cambiarPassword(String username, String nuevaPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        String salt = generarSalt();
        String hash = hashPassword(nuevaPassword, salt);

        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, hash);
        values.put(COLUMN_SALT, salt);

        return db.update(TABLE_USUARIOS, values,
                COLUMN_USERNAME + " = ?",
                new String[]{username.toLowerCase(Locale.ROOT).trim()});
    }

    // ===================== MÉTODOS DE CARRITO =====================
    public long agregarAlCarrito(int productoId, int cantidad) {
        SQLiteDatabase db = this.getWritableDatabase();
        int stockDisponible = obtenerStockProducto(productoId);
        if (stockDisponible < cantidad) return -1;

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_CARRITO,
                    new String[]{COLUMN_CARRITO_ID, COLUMN_CANTIDAD},
                    COLUMN_ID + " = ?", new String[]{String.valueOf(productoId)},
                    null, null, null);

            long result;
            if (cursor != null && cursor.moveToFirst()) {
                int cantidadActual = cursor.getInt(1);
                ContentValues values = new ContentValues();
                values.put(COLUMN_CANTIDAD, cantidadActual + cantidad);
                result = db.update(TABLE_CARRITO, values, COLUMN_ID + " = ?", new String[]{String.valueOf(productoId)});
            } else {
                ContentValues values = new ContentValues();
                values.put(COLUMN_ID, productoId);
                values.put(COLUMN_CANTIDAD, cantidad);
                result = db.insert(TABLE_CARRITO, null, values);
            }

            if (result > 0) actualizarStockProducto(productoId, stockDisponible - cantidad);
            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int actualizarCantidadEnCarrito(int productoId, int nuevaCantidad) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CANTIDAD, nuevaCantidad);
        return db.update(TABLE_CARRITO, values, COLUMN_ID + " = ?", new String[]{String.valueOf(productoId)});
    }

    public int eliminarDelCarrito(int productoId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int cantidad = obtenerCantidadEnCarrito(productoId);
        int stockActual = obtenerStockProducto(productoId);
        int result = db.delete(TABLE_CARRITO, COLUMN_ID + " = ?", new String[]{String.valueOf(productoId)});
        if (result > 0) actualizarStockProducto(productoId, stockActual + cantidad);
        return result;
    }

    private int obtenerCantidadEnCarrito(int productoId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_CARRITO, new String[]{COLUMN_CANTIDAD},
                    COLUMN_ID + " = ?", new String[]{String.valueOf(productoId)}, null, null, null);
            int cantidad = 0;
            if (cursor != null && cursor.moveToFirst()) {
                cantidad = cursor.getInt(0);
            }
            return cantidad;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public Cursor obtenerCarrito() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT p." + COLUMN_ID + ", p." + COLUMN_NOMBRE + ", p." + COLUMN_DESCRIPCION + ", " +
                "p." + COLUMN_PRECIO + ", p." + COLUMN_IMAGEN_PATH + ", p." + COLUMN_STOCK + ", c." + COLUMN_CANTIDAD +
                " FROM " + TABLE_CARRITO + " c " +
                "INNER JOIN " + TABLE_PRODUCTOS + " p ON c." + COLUMN_ID + " = p." + COLUMN_ID + ";";
        return db.rawQuery(query, null);
    }

    public double calcularTotalCarrito() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT SUM(p." + COLUMN_PRECIO + " * c." + COLUMN_CANTIDAD + ") AS total " +
                    "FROM " + TABLE_CARRITO + " c " +
                    "INNER JOIN " + TABLE_PRODUCTOS + " p ON c." + COLUMN_ID + " = p." + COLUMN_ID + ";";
            cursor = db.rawQuery(query, null);
            double total = 0;
            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
            }
            return total;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // ===================== MÉTODOS DE REPORTES =====================
    public int obtenerCantidadUsuarios() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USUARIOS, null);
            int cantidad = 0;
            if (cursor != null && cursor.moveToFirst()) {
                cantidad = cursor.getInt(0);
            }
            return cantidad;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int obtenerCantidadProductos() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PRODUCTOS, null);
            int cantidad = 0;
            if (cursor != null && cursor.moveToFirst()) {
                cantidad = cursor.getInt(0);
            }
            return cantidad;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int obtenerCantidadProductosEnCarrito() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT SUM(" + COLUMN_CANTIDAD + ") FROM " + TABLE_CARRITO, null);
            int cantidad = 0;
            if (cursor != null && cursor.moveToFirst()) {
                cantidad = cursor.getInt(0);
            }
            return cantidad;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // ===================== CLASE INTERNA DE USUARIO =====================
    public static class Usuario {
        public int id;
        public String username;
        public String password;
        public String salt;
        public String rol;

        public Usuario(int id, String username, String password, String salt, String rol) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.salt = salt;
            this.rol = rol;
        }

        // Constructor alternativo sin ID
        public Usuario(String username, String password, String rol) {
            this.username = username;
            this.password = password;
            this.rol = rol;
        }
    }
}

