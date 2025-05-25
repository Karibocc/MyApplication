package com.example.myapplication.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mi_tienda.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_PRODUCTOS = "productos";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_DESCRIPCION = "descripcion";
    public static final String COLUMN_PRECIO = "precio";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_PRODUCTOS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NOMBRE + " TEXT, " +
                    COLUMN_DESCRIPCION + " TEXT, " +
                    COLUMN_PRECIO + " REAL);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTOS);
        onCreate(db);
    }

    // CRUD OPERATIONS

    public long insertarProducto(String nombre, String descripcion, double precio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_DESCRIPCION, descripcion);
        values.put(COLUMN_PRECIO, precio);
        return db.insert(TABLE_PRODUCTOS, null, values);
    }

    public Cursor obtenerTodosLosProductos() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PRODUCTOS, null, null, null, null, null, null);
    }

    public int actualizarProducto(int id, String nombre, String descripcion, double precio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_DESCRIPCION, descripcion);
        values.put(COLUMN_PRECIO, precio);
        return db.update(TABLE_PRODUCTOS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int eliminarProducto(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_PRODUCTOS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }
}
