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
        /*switch (oldVersion) {

            case 1:
                db.execSQL("ALTER TABLE" + TABLE_NAME + " ADD COLUMN" + NEW_COLUMN + " TEXT");

            case 2:
                db.execSQL("ALTER TABLE" + TABLE_NAME + " ADD COLUMN" + NEW_COLUMN + " TEXT");
        }*/
    }

    public void addSong(Song song) {

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(ID_FIELD, song.getId());
        cv.put(NOMBRE_FIELD, song.getNombre());
        cv.put(MP3_FIELD, song.getSong());

        sqLiteDatabase.insert(TABLE_NAME, null, cv);

    }

    public void borrarSongs () {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL("DELETE FROM " + TABLE_NAME);

    }

    public ArrayList<Song> getSongs() throws IllegalAccessException, NoSuchFieldException {

        ArrayList<Song> arraySongs = new ArrayList<>();

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

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

}
