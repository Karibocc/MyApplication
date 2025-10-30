package com.example.myapplication.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.util.Log;

import java.security.MessageDigest;

public class DatabaseHelper extends SQLiteOpenHelper {

    // ===================== CONSTANTES DE BASE DE DATOS =====================
    private static final String DATABASE_NAME = "miapplication.db";
    private static final int DATABASE_VERSION = 4;

    // TABLA PRODUCTOS
    public static final String TABLE_PRODUCTOS = "productos";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_DESCRIPCION = "descripcion";
    public static final String COLUMN_PRECIO = "precio";
    public static final String COLUMN_IMAGEN_PATH = "imagen_path";
    public static final String COLUMN_STOCK = "stock";
    public static final String COLUMN_CANTIDAD = "cantidad";

    // TABLA USUARIOS
    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COLUMN_USUARIO_ID = "id_usuario";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_ROL = "rol";

    // TABLA CARRITO
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
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_PRODUCTOS + " ADD COLUMN " + COLUMN_IMAGEN_PATH + " TEXT;");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_PRODUCTOS + " ADD COLUMN " + COLUMN_STOCK + " INTEGER DEFAULT 0;");
            db.execSQL(TABLE_CREATE_CARRITO);
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_PRODUCTOS + " ADD COLUMN " + COLUMN_CANTIDAD + " INTEGER DEFAULT 1;");
        }
    }

    // ===================== MÉTODO DE HASH =====================
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error generando hash: " + e.getMessage());
            return password;
        }
    }

    // ===================== PRODUCTOS =====================
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

    public Cursor obtenerTodosLosProductos() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PRODUCTOS, null, null, null, null, null, COLUMN_NOMBRE + " ASC");
    }

    public Cursor obtenerProductoPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PRODUCTOS, null, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
    }

    public int actualizarProducto(int id, String nombre, String descripcion, double precio, String imagenPath, int stock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_DESCRIPCION, descripcion);
        values.put(COLUMN_PRECIO, precio);
        values.put(COLUMN_IMAGEN_PATH, imagenPath);
        values.put(COLUMN_STOCK, stock);
        return db.update(TABLE_PRODUCTOS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int eliminarProducto(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_PRODUCTOS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int obtenerStockProducto(int productoId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTOS, new String[]{COLUMN_STOCK},
                COLUMN_ID + " = ?", new String[]{String.valueOf(productoId)},
                null, null, null);
        int stock = 0;
        if (cursor.moveToFirst()) stock = cursor.getInt(0);
        cursor.close();
        return stock;
    }

    private void actualizarStockProducto(int productoId, int nuevoStock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STOCK, nuevoStock);
        db.update(TABLE_PRODUCTOS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(productoId)});
    }

    // ===================== USUARIOS =====================
    public long insertarUsuario(String username, String password, String rol) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        String usernameNormalizado = username.toLowerCase().trim();
        String passwordHasheada = hashPassword(password.trim());
        values.put(COLUMN_USERNAME, usernameNormalizado);
        values.put(COLUMN_PASSWORD, passwordHasheada);
        values.put(COLUMN_ROL, rol);
        return db.insert(TABLE_USUARIOS, null, values);
    }

    public boolean validarUsuario(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String usernameNormalizado = username.toLowerCase().trim();
        String passwordHasheada = hashPassword(password.trim());
        Cursor cursor = db.query(TABLE_USUARIOS, new String[]{COLUMN_PASSWORD},
                COLUMN_USERNAME + " = ?", new String[]{usernameNormalizado},
                null, null, null);
        boolean valido = false;
        if (cursor.moveToFirst()) {
            String storedPassword = cursor.getString(0);
            valido = storedPassword.equals(passwordHasheada);
        }
        cursor.close();
        return valido;
    }

    public String obtenerRol(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USUARIOS, new String[]{COLUMN_ROL},
                COLUMN_USERNAME + " = ?", new String[]{username.toLowerCase().trim()},
                null, null, null);
        String rol = "";
        if (cursor.moveToFirst()) rol = cursor.getString(0);
        cursor.close();
        return rol;
    }

    // ===================== MÉTODOS NUEVOS PARA USUARIO =====================

    /**
     * Verifica si un usuario ya existe
     */
    public boolean usuarioExiste(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USUARIOS,
                new String[]{COLUMN_USERNAME},
                COLUMN_USERNAME + " = ?",
                new String[]{username.toLowerCase().trim()},
                null, null, null
        );
        boolean existe = cursor.moveToFirst();
        cursor.close();
        return existe;
    }

    /**
     * Obtiene un usuario por su username
     */
    public Cursor obtenerUsuarioPorNombre(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_USUARIOS,
                new String[]{COLUMN_USERNAME, COLUMN_PASSWORD, COLUMN_ROL},
                COLUMN_USERNAME + " = ?",
                new String[]{username.toLowerCase().trim()},
                null, null, null
        );
    }

    /**
     * Obtiene todos los usuarios
     */
    public Cursor obtenerTodosLosUsuarios() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_USUARIOS,
                new String[]{COLUMN_USERNAME, COLUMN_PASSWORD, COLUMN_ROL},
                null, null, null, null,
                COLUMN_USERNAME + " ASC"
        );
    }

    // ===================== CARRITO =====================
    public long agregarAlCarrito(int productoId, int cantidad) {
        SQLiteDatabase db = this.getWritableDatabase();
        int stockDisponible = obtenerStockProducto(productoId);
        if (stockDisponible < cantidad) return -1;

        Cursor cursor = db.query(TABLE_CARRITO,
                new String[]{COLUMN_CARRITO_ID, COLUMN_CANTIDAD},
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(productoId)},
                null, null, null);

        long result;
        if (cursor.moveToFirst()) {
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
        cursor.close();

        if (result > 0) {
            actualizarStockProducto(productoId, stockDisponible - cantidad);
        }
        return result;
    }

    public int eliminarDelCarrito(int productoId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int cantidad = obtenerCantidadEnCarrito(productoId);
        int stockActual = obtenerStockProducto(productoId);
        int result = db.delete(TABLE_CARRITO, COLUMN_ID + " = ?", new String[]{String.valueOf(productoId)});
        if (result > 0) {
            actualizarStockProducto(productoId, stockActual + cantidad);
        }
        return result;
    }

    private int obtenerCantidadEnCarrito(int productoId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CARRITO, new String[]{COLUMN_CANTIDAD},
                COLUMN_ID + " = ?", new String[]{String.valueOf(productoId)},
                null, null, null);
        int cantidad = 0;
        if (cursor.moveToFirst()) cantidad = cursor.getInt(0);
        cursor.close();
        return cantidad;
    }

    public Cursor obtenerCarrito() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT p." + COLUMN_ID + ", p." + COLUMN_NOMBRE + ", p." + COLUMN_DESCRIPCION + ", " +
                "p." + COLUMN_PRECIO + ", p." + COLUMN_IMAGEN_PATH + ", p." + COLUMN_STOCK + ", c." + COLUMN_CANTIDAD +
                " FROM " + TABLE_CARRITO + " c " +
                "INNER JOIN " + TABLE_PRODUCTOS + " p ON c." + COLUMN_ID + " = p." + COLUMN_ID + ";";
        return db.rawQuery(query, null);
    }

    public int actualizarCantidadEnCarrito(int productoId, int nuevaCantidad) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CANTIDAD, nuevaCantidad);
        return db.update(TABLE_CARRITO, values, COLUMN_ID + " = ?", new String[]{String.valueOf(productoId)});
    }

    public double calcularTotalCarrito() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(p." + COLUMN_PRECIO + " * c." + COLUMN_CANTIDAD + ") AS total " +
                "FROM " + TABLE_CARRITO + " c " +
                "INNER JOIN " + TABLE_PRODUCTOS + " p ON c." + COLUMN_ID + " = p." + COLUMN_ID + ";";
        Cursor cursor = db.rawQuery(query, null);
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
        }
        cursor.close();
        return total;
    }

    // Clase interna Usuario (compatibilidad)
    public static class Usuario {
        public String username;
        public String password;
        public String rol;

        public Usuario(String username, String password, String rol) {
            this.username = username;
            this.password = password;
            this.rol = rol;
        }
    }
}





