package com.example.ejer12dual;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

public class SQLiteManager extends SQLiteOpenHelper {

    private static SQLiteManager sqLiteManager;
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "songs";
    private static final String COUNTER = "Counter";
    private static final String DATABASE_NAME = "dbMusic";
    private static final String ID_FIELD = "id";
    private static final String NOMBRE_FIELD = "nombre";
    private static final String MP3_FIELD = "mp3";

    public SQLiteManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static SQLiteManager instanceOfDatabase(Context context) {

        if (sqLiteManager==null) {
            sqLiteManager = new SQLiteManager(context);
        }
        return sqLiteManager;
    }

    /**
     * Al iniciar la aplicación se crea la base de datos con las características de las canciones.
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        StringBuilder sql;
        sql = new StringBuilder()
                .append("CREATE TABLE ")
                .append(TABLE_NAME)
                .append("(")
                .append(COUNTER)
                .append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
                .append(ID_FIELD)
                .append(" INT, ")
                .append(NOMBRE_FIELD)
                .append(" TEXT, ")
                .append(MP3_FIELD)
                .append(" BLOB)");

        db.execSQL(sql.toString());

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**
     * El método addSong recibe por parámetro una canción e introduce los datos en la base
     * de datos.
     * @param song
     */
    public void addSong(Song song) {

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(ID_FIELD, song.getId());
        cv.put(NOMBRE_FIELD, song.getNombre());
        cv.put(MP3_FIELD, song.getSong());

        sqLiteDatabase.insert(TABLE_NAME, null, cv);

    }

    /**
     * borrarSongs limpia toda la base de datos
     */
    public void borrarSongs () {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL("DELETE FROM " + TABLE_NAME);

    }

    /**
     * getSongs devuelve todas las canciones en un array creando canciones nuevas con datos
     * recogidos por un cursor.
     * @return
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    public ArrayList<Song> getSongs() throws IllegalAccessException, NoSuchFieldException {
        ArrayList<Song> arraySongs = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        /**
         * Es importante que mediante Field cambiemos una propiedad del cursor que va a recoger
         * los Blob de la base de datos, pues originalmente el cursor es demasiado pequeño y no puede
         * leer más de 1MB, incluso siendo lo máximo de un Blob 2GB
         */
        Field field = CursorWindow.class.getDeclaredField("sCursorWindowSize");
        field.setAccessible(true);
        field.set(null, 100 * 1024 * 1024);
        try (Cursor result = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_NAME, null)) {

            if (result.getCount()!=0) {

                while (result.moveToNext()) {
                    int id = result.getInt(1);
                    String name = result.getString(2);
                    byte[] song = result.getBlob(3);

                    Song newSong = new Song(id, name, song);
                    arraySongs.add(newSong);
                }

            }

        }
        return arraySongs;
    }

    public void updateSong(Song s) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(ID_FIELD, s.getId());
        cv.put(NOMBRE_FIELD, s.getNombre());
        cv.put(MP3_FIELD, s.getSong());

        sqLiteDatabase.update(TABLE_NAME, cv, ID_FIELD + " =? ", new String[]{String.valueOf(s.getId())});
    }

    /**
     * Borrar canción busca una canción por ID y la borra de la base de datos SQLite.
     * @param s
     */
    public void borrarCancion(Song s) {

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + NOMBRE_FIELD + "='" + s.getNombre() + "'");


    }

}
