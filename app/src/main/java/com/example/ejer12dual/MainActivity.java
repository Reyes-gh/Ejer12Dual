package com.example.ejer12dual;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    TextView test;
    ImageView iV;

    ListView lv;

    SQLiteManager sqLiteManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        lv = findViewById(R.id.songList);
        updateList();

        lv.setOnItemClickListener((adapterView, view, i, l) -> {

            String s = lv.getItemAtPosition(i).toString();

            Song newSong = (Song) lv.getItemAtPosition(i);
            test.setText(newSong.toString());

        });

        sqLiteManager = new SQLiteManager(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        test = findViewById(R.id.test);
        iV = findViewById(R.id.iV);

        Drawable teleGif = getDrawable(R.drawable.quieto);
        Drawable teleRun = getDrawable(R.drawable.corriendo);
        teleGif.setFilterBitmap(false);
        Glide.with(this).load(teleGif).into(iV);



        SQLiteDatabase sqLiteDatabase;

       // sqLiteDatabase.execSQL

        MediaPlayer mp = new MediaPlayer();
//        btPixel.setOnClickListener(v -> {
//
//            if (iV.getDrawable()==teleGif) {
//
//                Glide.with(this).load(teleRun).into(iV);
//                mp.start();
//            } else {
//                Glide.with(this).load(teleGif).into(iV);
//                if (mp.isPlaying()) {
//                    mp.stop();
//                }
//            }
//
//        });
    }

    public void newSong(View view) {
        openFile();
    }


    private void openFile() {
        Intent intent;
        intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/mpeg");

        startActivityForResult(intent, 230);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {

        if(requestCode == 230){
            if(resultCode == RESULT_OK){
                Uri audioUri = data.getData();
                String mimeType = getContentResolver().getType(audioUri);

                Cursor returnCursor =
                        getContentResolver().query(audioUri, null, null, null, null);

                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                String nombre = returnCursor.getString(nameIndex);

                File mp3File = new File(audioUri.getPath());
                byte[] bytes = new byte[(int) mp3File.length()];

                    InputStream inputStream = getContentResolver().openInputStream(audioUri);
                    inputStream.read(bytes);

                test.setText(audioUri.getPath());

                    songPaLista(nombre, bytes);

                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onActivityResult(requestCode, resultCode, data);
        }

    public void songPaLista(String file, byte[] bytes) throws IOException {

        Song s1 = new Song(1, file, bytes);

        sqLiteManager.addSong(s1);

        ArrayList<Song> arraySongs = sqLiteManager.getSongs();

        ArrayAdapter<Song> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                arraySongs);

        lv = findViewById(R.id.songList);
        lv.setAdapter(arrayAdapter);
    }

    public void borrarSongs(View view) {
        sqLiteManager.borrarSongs();
    }

    public void updateList(){
        ArrayList<Song> arraySongs = sqLiteManager.getSongs();

        ArrayAdapter<Song> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                arraySongs);

        lv = findViewById(R.id.songList);
        lv.setAdapter(arrayAdapter);
    }


}



