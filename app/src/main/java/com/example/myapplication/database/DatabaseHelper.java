package com.example.myapplication.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class DatabaseHelper extends SQLiteOpenHelper {

    // CONSTANTES DE LA BASE DE DATOS
    private static final String DATABASE_NAME = "miapplication.db";
    private static final int DATABASE_VERSION = 3;

    // TABLA PRODUCTOS
    public static final String TABLE_PRODUCTOS = "productos";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_DESCRIPCION = "descripcion";
    public static final String COLUMN_PRECIO = "precio";
    public static final String COLUMN_IMAGEN_PATH = "imagen_path";
    public static final String COLUMN_STOCK = "stock";

    // TABLA USUARIOS
    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COLUMN_USUARIO_ID = "id_usuario";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_ROL = "rol";

    // TABLA CARRITO
    public static final String TABLE_CARRITO = "carrito";
    public static final String COLUMN_CARRITO_ID = "id_carrito";
    public static final String COLUMN_CANTIDAD = "cantidad";

    // SENTENCIAS SQL
    private static final String TABLE_CREATE_PRODUCTOS =
            "CREATE TABLE " + TABLE_PRODUCTOS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NOMBRE + " TEXT NOT NULL, " +
                    COLUMN_DESCRIPCION + " TEXT, " +
                    COLUMN_PRECIO + " REAL NOT NULL, " +
                    COLUMN_IMAGEN_PATH + " TEXT, " +
                    COLUMN_STOCK + " INTEGER DEFAULT 0);";

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

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

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
    }
    // =================== OPERACIONES DE PRODUCTOS ===================

    public long insertarProducto(String nombre, String descripcion, double precio, String imagenPath, int stock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_DESCRIPCION, descripcion);
        values.put(COLUMN_PRECIO, precio);
        values.put(COLUMN_IMAGEN_PATH, imagenPath);
        values.put(COLUMN_STOCK, stock);
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
        return db.update(TABLE_PRODUCTOS, values, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)});
    }

    public int eliminarProducto(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_PRODUCTOS, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)});
    }

    public Cursor buscarProductos(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PRODUCTOS,
                null,
                COLUMN_NOMBRE + " LIKE ? OR " + COLUMN_DESCRIPCION + " LIKE ?",
                new String[]{"%" + query + "%", "%" + query + "%"},
                null, null,
                COLUMN_NOMBRE + " ASC");
    }

    // =================== OPERACIONES DE USUARIOS ===================

    public long insertarUsuario(String username, String password, String rol) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_ROL, rol);
        return db.insert(TABLE_USUARIOS, null, values);
    }

    public boolean usuarioExiste(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USUARIOS,
                new String[]{COLUMN_USERNAME},
                COLUMN_USERNAME + " = ?",
                new String[]{username},
                null, null, null);
        boolean existe = cursor.moveToFirst();
        cursor.close();
        return existe;
    }

    public boolean validarUsuario(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USUARIOS,
                null,
                COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?",
                new String[]{username, password},
                null, null, null);
        boolean valido = cursor.moveToFirst();
        cursor.close();
        return valido;
    }

    public String obtenerRol(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USUARIOS,
                new String[]{COLUMN_ROL},
                COLUMN_USERNAME + " = ?",
                new String[]{username},
                null, null, null);
        String rol = "";
        if (cursor.moveToFirst()) {
            rol = cursor.getString(0);
        }
        cursor.close();
        return rol;
    }

    public Cursor obtenerUsuarioPorNombre(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_USUARIOS,
                new String[]{COLUMN_USERNAME, COLUMN_PASSWORD, COLUMN_ROL},
                COLUMN_USERNAME + " = ?",
                new String[]{username},
                null, null, null
        );
    }

    // OPERACIONES DEL CARRITO
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
            result = db.update(TABLE_CARRITO, values,
                    COLUMN_ID + " = ?", new String[]{String.valueOf(productoId)});
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

        int result = db.delete(TABLE_CARRITO,
                COLUMN_ID + " = ?", new String[]{String.valueOf(productoId)});

        if (result > 0) {
            actualizarStockProducto(productoId, stockActual + cantidad);
        }

        return result;
    }

    public int actualizarCantidadEnCarrito(int productoId, int nuevaCantidad) {
        SQLiteDatabase db = this.getWritableDatabase();

        int stockDisponible = obtenerStockProducto(productoId);
        int cantidadActual = obtenerCantidadEnCarrito(productoId);
        int diferencia = nuevaCantidad - cantidadActual;

        if (stockDisponible < diferencia) return -1;

        ContentValues values = new ContentValues();
        values.put(COLUMN_CANTIDAD, nuevaCantidad);

        int result = db.update(TABLE_CARRITO, values,
                COLUMN_ID + " = ?", new String[]{String.valueOf(productoId)});

        if (result > 0) {
            actualizarStockProducto(productoId, stockDisponible - diferencia);
        }

        return result;
    }

    public Cursor obtenerCarrito() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT p." + COLUMN_ID + ", p." + COLUMN_NOMBRE +
                ", p." + COLUMN_DESCRIPCION + ", p." + COLUMN_PRECIO +
                ", p." + COLUMN_IMAGEN_PATH + ", c." + COLUMN_CANTIDAD +
                " FROM " + TABLE_PRODUCTOS + " p INNER JOIN " +
                TABLE_CARRITO + " c ON p." + COLUMN_ID + " = c." + COLUMN_ID;
        return db.rawQuery(query, null);
    }

    public double calcularTotalCarrito() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(p." + COLUMN_PRECIO + " * c." + COLUMN_CANTIDAD + ") as total " +
                "FROM " + TABLE_PRODUCTOS + " p INNER JOIN " +
                TABLE_CARRITO + " c ON p." + COLUMN_ID + " = c." + COLUMN_ID;

        Cursor cursor = db.rawQuery(query, null);
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public int limpiarCarrito() {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + COLUMN_ID + ", " + COLUMN_CANTIDAD +
                " FROM " + TABLE_CARRITO, null);

        while (cursor.moveToNext()) {
            int productoId = cursor.getInt(0);
            int cantidad = cursor.getInt(1);
            int stockActual = obtenerStockProducto(productoId);
            actualizarStockProducto(productoId, stockActual + cantidad);
        }
        cursor.close();

        return db.delete(TABLE_CARRITO, null, null);
    }

    // MÉTODOS AUXILIARES
    public int obtenerStockProducto(int productoId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTOS,
                new String[]{COLUMN_STOCK},
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(productoId)},
                null, null, null);
        int stock = 0;
        if (cursor.moveToFirst()) {
            stock = cursor.getInt(0);
        }
        cursor.close();
        return stock;
    }

    private void actualizarStockProducto(int productoId, int nuevoStock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STOCK, nuevoStock);
        db.update(TABLE_PRODUCTOS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(productoId)});
    }

    private int obtenerCantidadEnCarrito(int productoId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CARRITO,
                new String[]{COLUMN_CANTIDAD},
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(productoId)},
                null, null, null);
        int cantidad = 0;
        if (cursor.moveToFirst()) {
            cantidad = cursor.getInt(0);
        }
        cursor.close();
        return cantidad;
    }
}
