package com.example.myapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.myapplication.models.Producto;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Configuración de base de datos
    private static final String DATABASE_NAME = "miapplication.db";
    private static final int DATABASE_VERSION = 8;

    // Tabla productos
    public static final String TABLE_PRODUCTOS = "productos";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_DESCRIPCION = "descripcion";
    public static final String COLUMN_PRECIO = "precio";
    public static final String COLUMN_IMAGEN_PATH = "imagen_path";
    public static final String COLUMN_STOCK = "stock";
    public static final String COLUMN_CANTIDAD = "cantidad";
    public static final String COLUMN_CATEGORIA = "categoria";
    public static final String COLUMN_FECHA_CREACION = "fecha_creacion";

    // Tabla usuarios
    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COLUMN_USUARIO_ID = "id_usuario";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_SALT = "salt";
    public static final String COLUMN_ROL = "rol";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_FECHA_REGISTRO = "fecha_registro";

    // Tabla carrito
    public static final String TABLE_CARRITO = "carrito";
    public static final String COLUMN_CARRITO_ID = "id_carrito";
    public static final String COLUMN_FECHA_AGREGADO = "fecha_agregado";

    // Tabla órdenes/ventas
    public static final String TABLE_ORDENES = "ordenes";
    public static final String COLUMN_ORDEN_ID = "id_orden";
    public static final String COLUMN_FECHA_ORDEN = "fecha_orden";
    public static final String COLUMN_TOTAL = "total";
    public static final String COLUMN_ESTADO = "estado";
    public static final String COLUMN_USUARIO_ORDEN = "usuario";
    public static final String COLUMN_PRODUCTOS_ORDEN = "productos";

    // Creación de tablas
    private static final String TABLE_CREATE_PRODUCTOS =
            "CREATE TABLE " + TABLE_PRODUCTOS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NOMBRE + " TEXT NOT NULL, " +
                    COLUMN_DESCRIPCION + " TEXT, " +
                    COLUMN_PRECIO + " REAL NOT NULL, " +
                    COLUMN_IMAGEN_PATH + " TEXT, " +
                    COLUMN_STOCK + " INTEGER DEFAULT 0, " +
                    COLUMN_CANTIDAD + " INTEGER DEFAULT 1, " +
                    COLUMN_CATEGORIA + " TEXT DEFAULT 'General', " +
                    COLUMN_FECHA_CREACION + " DATETIME DEFAULT CURRENT_TIMESTAMP);";

    private static final String TABLE_CREATE_USUARIOS =
            "CREATE TABLE " + TABLE_USUARIOS + " (" +
                    COLUMN_USUARIO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_SALT + " TEXT, " +
                    COLUMN_ROL + " TEXT NOT NULL, " +
                    COLUMN_EMAIL + " TEXT, " +
                    COLUMN_FECHA_REGISTRO + " DATETIME DEFAULT CURRENT_TIMESTAMP);";

    private static final String TABLE_CREATE_CARRITO =
            "CREATE TABLE " + TABLE_CARRITO + " (" +
                    COLUMN_CARRITO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_ID + " INTEGER NOT NULL, " +
                    COLUMN_CANTIDAD + " INTEGER DEFAULT 1, " +
                    COLUMN_FECHA_AGREGADO + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(" + COLUMN_ID + ") REFERENCES " +
                    TABLE_PRODUCTOS + "(" + COLUMN_ID + ") ON DELETE CASCADE);";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // ==================================================================
    // MÉTODOS DE CREACIÓN Y ACTUALIZACIÓN DE BD
    // ==================================================================

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_PRODUCTOS);
        db.execSQL(TABLE_CREATE_USUARIOS);
        db.execSQL(TABLE_CREATE_CARRITO);
        insertarDatosIniciales(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DatabaseHelper", "Actualizando BD de versión " + oldVersion + " a " + newVersion);

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
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE " + TABLE_PRODUCTOS + " ADD COLUMN " + COLUMN_CATEGORIA + " TEXT DEFAULT 'General';");
            db.execSQL("ALTER TABLE " + TABLE_PRODUCTOS + " ADD COLUMN " + COLUMN_FECHA_CREACION + " DATETIME DEFAULT CURRENT_TIMESTAMP;");
            db.execSQL("ALTER TABLE " + TABLE_USUARIOS + " ADD COLUMN " + COLUMN_EMAIL + " TEXT;");
            db.execSQL("ALTER TABLE " + TABLE_USUARIOS + " ADD COLUMN " + COLUMN_FECHA_REGISTRO + " DATETIME DEFAULT CURRENT_TIMESTAMP;");
            db.execSQL("ALTER TABLE " + TABLE_CARRITO + " ADD COLUMN " + COLUMN_FECHA_AGREGADO + " DATETIME DEFAULT CURRENT_TIMESTAMP;");
        }
        if (oldVersion < 7) {
            db.execSQL("CREATE INDEX idx_productos_nombre ON " + TABLE_PRODUCTOS + "(" + COLUMN_NOMBRE + ");");
            db.execSQL("CREATE INDEX idx_productos_categoria ON " + TABLE_PRODUCTOS + "(" + COLUMN_CATEGORIA + ");");
            db.execSQL("CREATE INDEX idx_usuarios_username ON " + TABLE_USUARIOS + "(" + COLUMN_USERNAME + ");");
        }
        if (oldVersion < 8) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARRITO);
            db.execSQL(TABLE_CREATE_CARRITO);
        }
    }

    private void insertarDatosIniciales(SQLiteDatabase db) {
        try {
            String saltAdmin = generarSalt();
            String hashAdmin = hashPassword("admin123", saltAdmin);

            ContentValues adminValues = new ContentValues();
            adminValues.put(COLUMN_USERNAME, "admin");
            adminValues.put(COLUMN_PASSWORD, hashAdmin);
            adminValues.put(COLUMN_SALT, saltAdmin);
            adminValues.put(COLUMN_ROL, "admin");
            adminValues.put(COLUMN_EMAIL, "admin@miapplication.com");
            db.insert(TABLE_USUARIOS, null, adminValues);

            insertarProductoEjemplo(db, "Laptop Gaming", "Laptop para gaming de alta performance", 1299.99, 10);
            insertarProductoEjemplo(db, "Smartphone Android", "Teléfono inteligente con Android 13", 499.99, 25);
            insertarProductoEjemplo(db, "Tablet 10 pulgadas", "Tablet perfecta para trabajo y entretenimiento", 299.99, 15);

            Log.d("DatabaseHelper", "Datos iniciales insertados correctamente");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error insertando datos iniciales: " + e.getMessage());
        }
    }

    private void insertarProductoEjemplo(SQLiteDatabase db, String nombre, String descripcion, double precio, int stock) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_DESCRIPCION, descripcion);
        values.put(COLUMN_PRECIO, precio);
        values.put(COLUMN_STOCK, stock);
        values.put(COLUMN_CATEGORIA, "Electrónicos");
        db.insert(TABLE_PRODUCTOS, null, values);
    }

    // ==================================================================
    // MÉTODOS AUXILIARES DE SEGURIDAD
    // ==================================================================

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

    // ==================================================================
    // MÉTODOS DE PRODUCTOS
    // ==================================================================

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

    public long insertarProducto(Producto producto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE, producto.getNombre());
        values.put(COLUMN_DESCRIPCION, producto.getDescripcion());
        values.put(COLUMN_PRECIO, producto.getPrecio());
        values.put(COLUMN_IMAGEN_PATH, producto.getImagen_path());
        values.put(COLUMN_STOCK, producto.getStock());
        values.put(COLUMN_CANTIDAD, producto.getCantidad());
        values.put(COLUMN_CATEGORIA, "General");
        return db.insert(TABLE_PRODUCTOS, null, values);
    }

    public long insertarProducto(String nombre, String descripcion, double precio, String imagenPath, int stock, String categoria) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_DESCRIPCION, descripcion);
        values.put(COLUMN_PRECIO, precio);
        values.put(COLUMN_IMAGEN_PATH, imagenPath);
        values.put(COLUMN_STOCK, stock);
        values.put(COLUMN_CANTIDAD, 1);
        values.put(COLUMN_CATEGORIA, categoria);
        return db.insert(TABLE_PRODUCTOS, null, values);
    }

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

    public int actualizarProducto(int productoId, String nombre, String descripcion, double precio, String imagenPath, int stock, String categoria) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_DESCRIPCION, descripcion);
        values.put(COLUMN_PRECIO, precio);
        values.put(COLUMN_IMAGEN_PATH, imagenPath);
        values.put(COLUMN_STOCK, stock);
        values.put(COLUMN_CATEGORIA, categoria);
        return db.update(TABLE_PRODUCTOS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(productoId)});
    }

    public int eliminarProducto(int productoId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CARRITO, COLUMN_ID + " = ?", new String[]{String.valueOf(productoId)});
        return db.delete(TABLE_PRODUCTOS, COLUMN_ID + " = ?", new String[]{String.valueOf(productoId)});
    }

    public Cursor obtenerTodosLosProductos() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PRODUCTOS, null, null, null, null, null, COLUMN_NOMBRE + " ASC");
    }

    public Cursor obtenerProductosPorCategoria(String categoria) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PRODUCTOS, null, COLUMN_CATEGORIA + " = ?",
                new String[]{categoria}, null, null, COLUMN_NOMBRE + " ASC");
    }

    public Cursor buscarProductos(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PRODUCTOS, null,
                COLUMN_NOMBRE + " LIKE ? OR " + COLUMN_DESCRIPCION + " LIKE ?",
                new String[]{"%" + query + "%", "%" + query + "%"},
                null, null, COLUMN_NOMBRE + " ASC");
    }

    public Cursor obtenerCategorias() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(true, TABLE_PRODUCTOS,
                new String[]{COLUMN_CATEGORIA},
                null, null, null, null,
                COLUMN_CATEGORIA + " ASC", null);
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

    public boolean actualizarStockProducto(int productoId, int nuevoStock) {
        if (nuevoStock < 0) {
            Log.w("DatabaseHelper", "Intento de establecer stock negativo: " + nuevoStock);
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STOCK, nuevoStock);
        int rowsAffected = db.update(TABLE_PRODUCTOS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(productoId)});
        return rowsAffected > 0;
    }

    public Producto obtenerProductoPorId(int productoId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_PRODUCTOS,
                    null,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(productoId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE));
                String descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPCION));
                double precio = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRECIO));
                String imagenPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGEN_PATH));
                int stock = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STOCK));
                int cantidad = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CANTIDAD));
                String categoria = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORIA));

                return new Producto(id, nombre, descripcion, precio, imagenPath, stock, cantidad, categoria);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // ==================================================================
    // MÉTODOS DE USUARIOS
    // ==================================================================

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

    public long insertarUsuario(String username, String password, String rol) {
        SQLiteDatabase db = this.getWritableDatabase();
        String salt = generarSalt();
        String hash = hashPassword(password, salt);

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username.toLowerCase(Locale.ROOT).trim());
        values.put(COLUMN_PASSWORD, hash);
        values.put(COLUMN_SALT, salt);
        values.put(COLUMN_ROL, rol);

        return db.insert(TABLE_USUARIOS, null, values);
    }

    public long insertarUsuario(String username, String password, String rol, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        String salt = generarSalt();
        String hash = hashPassword(password, salt);

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username.toLowerCase(Locale.ROOT).trim());
        values.put(COLUMN_PASSWORD, hash);
        values.put(COLUMN_SALT, salt);
        values.put(COLUMN_ROL, rol);
        values.put(COLUMN_EMAIL, email);

        return db.insert(TABLE_USUARIOS, null, values);
    }

    public Usuario obtenerUsuarioPorNombre(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USUARIOS,
                    new String[]{COLUMN_USUARIO_ID, COLUMN_USERNAME, COLUMN_PASSWORD, COLUMN_SALT, COLUMN_ROL, COLUMN_EMAIL},
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
                String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
                usuario = new Usuario(id, nombre, password, salt, rol, email);
            }
            return usuario;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public Cursor obtenerTodosLosUsuarios() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USUARIOS,
                new String[]{COLUMN_USUARIO_ID, COLUMN_USERNAME, COLUMN_ROL, COLUMN_EMAIL, COLUMN_FECHA_REGISTRO},
                null, null, null, null,
                COLUMN_USERNAME + " ASC");
    }

    public int actualizarUsuario(String username, String nuevoRol) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ROL, nuevoRol);

        return db.update(TABLE_USUARIOS, values,
                COLUMN_USERNAME + " = ?",
                new String[]{username.toLowerCase(Locale.ROOT).trim()});
    }

    public int actualizarUsuario(String username, String nuevoRol, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ROL, nuevoRol);
        values.put(COLUMN_EMAIL, email);

        return db.update(TABLE_USUARIOS, values,
                COLUMN_USERNAME + " = ?",
                new String[]{username.toLowerCase(Locale.ROOT).trim()});
    }

    public int eliminarUsuario(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_USUARIOS,
                COLUMN_USERNAME + " = ?",
                new String[]{username.toLowerCase(Locale.ROOT).trim()});
    }

    public Usuario obtenerUsuarioPorId(int usuarioId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USUARIOS,
                    new String[]{COLUMN_USUARIO_ID, COLUMN_USERNAME, COLUMN_PASSWORD, COLUMN_SALT, COLUMN_ROL, COLUMN_EMAIL},
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
                String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
                usuario = new Usuario(id, nombre, password, salt, rol, email);
            }
            return usuario;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

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

    public int actualizarRolUsuario(String username, String nuevoRol) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ROL, nuevoRol);

            int resultado = db.update(
                    TABLE_USUARIOS,
                    values,
                    COLUMN_USERNAME + " = ?",
                    new String[]{username}
            );

            Log.d("DATABASE", "Actualizar rol - Usuario: " + username +
                    ", Nuevo rol: " + nuevoRol + ", Resultado: " + resultado);

            return resultado;

        } catch (Exception e) {
            Log.e("DATABASE", "Error actualizando rol: " + e.getMessage());
            return 0;
        } finally {
            db.close();
        }
    }

    public Cursor obtenerUsuariosPorRol(String rol) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.query(
                    TABLE_USUARIOS,
                    null,
                    COLUMN_ROL + " = ?",
                    new String[]{rol},
                    null,
                    null,
                    COLUMN_USERNAME + " ASC"
            );

            Log.d("DATABASE", "Usuarios por rol '" + rol + "': " + cursor.getCount());
            return cursor;

        } catch (Exception e) {
            Log.e("DATABASE", "Error obteniendo usuarios por rol: " + e.getMessage());
            return null;
        }
    }

    public int contarUsuariosPorRol(String rol) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT COUNT(*) FROM " + TABLE_USUARIOS +
                    " WHERE " + COLUMN_ROL + " = ?";
            cursor = db.rawQuery(query, new String[]{rol});

            if (cursor != null && cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                Log.d("DATABASE", "Contar usuarios - Rol: " + rol + ", Total: " + count);
                return count;
            }
            return 0;

        } catch (Exception e) {
            Log.e("DATABASE", "Error contando usuarios por rol: " + e.getMessage());
            return 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    public Cursor obtenerRolesUnicos() {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String query = "SELECT DISTINCT " + COLUMN_ROL +
                    " FROM " + TABLE_USUARIOS +
                    " ORDER BY " + COLUMN_ROL + " ASC";

            Cursor cursor = db.rawQuery(query, null);
            Log.d("DATABASE", "Roles únicos encontrados: " + cursor.getCount());
            return cursor;

        } catch (Exception e) {
            Log.e("DATABASE", "Error obteniendo roles únicos: " + e.getMessage());
            return null;
        }
    }

    public Cursor obtenerUsuarioCompleto(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.query(
                    TABLE_USUARIOS,
                    null,
                    COLUMN_USERNAME + " = ?",
                    new String[]{username},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                Log.d("DATABASE", "Usuario encontrado: " + username);
            } else {
                Log.d("DATABASE", "Usuario NO encontrado: " + username);
            }

            return cursor;

        } catch (Exception e) {
            Log.e("DATABASE", "Error obteniendo usuario completo: " + e.getMessage());
            return null;
        }
    }

    public String obtenerEstadisticasUsuarios() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT " + COLUMN_ROL + ", COUNT(*) as cantidad " +
                    "FROM " + TABLE_USUARIOS + " " +
                    "GROUP BY " + COLUMN_ROL + " " +
                    "ORDER BY cantidad DESC";

            cursor = db.rawQuery(query, null);

            StringBuilder estadisticas = new StringBuilder();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String rol = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROL));
                    int cantidad = cursor.getInt(cursor.getColumnIndexOrThrow("cantidad"));
                    estadisticas.append(rol).append(": ").append(cantidad).append("\n");
                } while (cursor.moveToNext());
            }

            Log.d("DATABASE", "Estadísticas: " + estadisticas.toString());
            return estadisticas.toString();

        } catch (Exception e) {
            Log.e("DATABASE", "Error obteniendo estadísticas: " + e.getMessage());
            return "Error al obtener estadísticas";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    // ==================================================================
    // MÉTODOS DE CARRITO
    // ==================================================================

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

    public int vaciarCarrito() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT " + COLUMN_ID + ", " + COLUMN_CANTIDAD + " FROM " + TABLE_CARRITO, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int productoId = cursor.getInt(0);
                    int cantidad = cursor.getInt(1);
                    int stockActual = obtenerStockProducto(productoId);
                    actualizarStockProducto(productoId, stockActual + cantidad);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return db.delete(TABLE_CARRITO, null, null);
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
                "INNER JOIN " + TABLE_PRODUCTOS + " p ON c." + COLUMN_ID + " = p." + COLUMN_ID +
                " ORDER BY c." + COLUMN_FECHA_AGREGADO + " DESC;";
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
    // ==================================================================
    // MÉTODOS DE ORDENES / VENTAS
    // ==================================================================

    public static class Orden {
        public int id;
        public String fecha;
        public double total;
        public String estado;
        public String usuario;
        public String productos; // JSON o string con los productos

        public Orden() {}

        public Orden(String fecha, double total, String estado, String usuario, String productos) {
            this.fecha = fecha;
            this.total = total;
            this.estado = estado;
            this.usuario = usuario;
            this.productos = productos;
        }
    }

    // ==================================================================
    // MÉTODOS DE REPORTES
    // ==================================================================

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

    public Cursor obtenerProductosMasVendidos(int limite) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT p." + COLUMN_ID + ", p." + COLUMN_NOMBRE + ", p." + COLUMN_PRECIO + ", " +
                "SUM(c." + COLUMN_CANTIDAD + ") as total_vendido " +
                "FROM " + TABLE_CARRITO + " c " +
                "INNER JOIN " + TABLE_PRODUCTOS + " p ON c." + COLUMN_ID + " = p." + COLUMN_ID + " " +
                "GROUP BY p." + COLUMN_ID + ", p." + COLUMN_NOMBRE + ", p." + COLUMN_PRECIO + " " +
                "ORDER BY total_vendido DESC " +
                "LIMIT " + limite + ";";
        return db.rawQuery(query, null);
    }

    public Cursor obtenerEstadisticasGenerales() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " +
                "(SELECT COUNT(*) FROM " + TABLE_USUARIOS + ") as total_usuarios, " +
                "(SELECT COUNT(*) FROM " + TABLE_PRODUCTOS + ") as total_productos, " +
                "(SELECT SUM(" + COLUMN_CANTIDAD + ") FROM " + TABLE_CARRITO + ") as total_carrito, " +
                "(SELECT SUM(p." + COLUMN_PRECIO + " * c." + COLUMN_CANTIDAD + ") FROM " + TABLE_CARRITO + " c " +
                "INNER JOIN " + TABLE_PRODUCTOS + " p ON c." + COLUMN_ID + " = p." + COLUMN_ID + ") as valor_total_carrito;";
        return db.rawQuery(query, null);
    }

    // ==================================================================
    // CLASE INTERNA DE USUARIO
    // ==================================================================

    public static class Usuario {
        public int id;
        public String username;
        public String password;
        public String salt;
        public String rol;
        public String email;

        public Usuario(int id, String username, String password, String salt, String rol) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.salt = salt;
            this.rol = rol;
        }

        public Usuario(int id, String username, String password, String salt, String rol, String email) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.salt = salt;
            this.rol = rol;
            this.email = email;
        }

        public Usuario(String username, String password, String rol) {
            this.username = username;
            this.password = password;
            this.rol = rol;
        }

        public Usuario(String username, String password, String rol, String email) {
            this.username = username;
            this.password = password;
            this.rol = rol;
            this.email = email;
        }
    }
}