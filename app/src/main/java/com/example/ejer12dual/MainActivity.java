package com.example.ejer12dual;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ImageView iV;
    ListView lv;
    SQLiteManager sqLiteManager;
    TextView songName;
    ImageButton btnPlay;
    MediaPlayer mp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sqLiteManager = new SQLiteManager(this);
        lv = findViewById(R.id.songList);
        mp = new MediaPlayer();

        setContentView(R.layout.activity_main);
        iV = findViewById(R.id.iV);

        Drawable teleGif = getDrawable(R.drawable.quieto);
        Drawable teleRun = getDrawable(R.drawable.corriendo);

        teleGif.setFilterBitmap(false);
        Glide.with(this).load(teleGif).into(iV);

        try {
            updateList();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        songName = (TextView) findViewById(R.id.songName);
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        iV.setVisibility(View.INVISIBLE);
        songName.setVisibility(View.INVISIBLE);
        btnPlay.setVisibility(View.INVISIBLE);

        lv.setOnItemClickListener((adapterView, view, i, l) -> {

            Glide.with(this).load(teleGif).into(iV);
            Song newSong = (Song) lv.getItemAtPosition(i);
            songName.setText(newSong.getNombre());
            abrirPlayer(newSong);

        });

        btnPlay.setOnClickListener(v -> {

            if (iV.getDrawable()==teleGif) {

                Glide.with(this).load(teleRun).into(iV);
                mp.start();
            } else {
                Glide.with(this).load(teleGif).into(iV);
                if (mp.isPlaying()) {
                    mp.pause();

                }
            }

        }

        );
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

                Cursor returnCursor =
                        getContentResolver().query(audioUri, null, null, null, null);

                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                String nombre = returnCursor.getString(nameIndex);
                int size = returnCursor.getInt(sizeIndex);


                File mp3File = new File(audioUri.getPath());
                byte[] bytes = new byte[size];
                System.out.println(mp3File.length() + "sies");

                    InputStream inputStream = getContentResolver().openInputStream(audioUri);
                    inputStream.read(bytes);

                    songPaLista(nombre, bytes);

                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        super.onActivityResult(requestCode, resultCode, data);
        }

    public void songPaLista(String file, byte[] bytes) throws IOException, NoSuchFieldException, IllegalAccessException {

        ArrayList<Song> arraySongs = sqLiteManager.getSongs();

        Song s1 = new Song(arraySongs.toArray().length+1, file, bytes);

        sqLiteManager.addSong(s1);
        arraySongs = sqLiteManager.getSongs();


        ArrayAdapter<Song> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                arraySongs);

        lv = findViewById(R.id.songList);
        lv.setAdapter(arrayAdapter);
    }

    public void borrarSongs(View view) throws NoSuchFieldException, IllegalAccessException {
        sqLiteManager.borrarSongs();
        updateList();
    }

    public void updateList() throws NoSuchFieldException, IllegalAccessException {
        ArrayList<Song> arraySongs = sqLiteManager.getSongs();
        ArrayAdapter<Song> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                arraySongs);

        lv = findViewById(R.id.songList);
        lv.setAdapter(arrayAdapter);
    }

    public void abrirPlayer(Song song) {

        iV.setVisibility(View.VISIBLE);
        btnPlay.setVisibility(View.VISIBLE);
        songName.setVisibility(View.VISIBLE);

        try {

            File tempMp3 = File.createTempFile("kurchina", "mp3", getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(song.getSong());
            fos.close();

            mp.reset();

            FileInputStream fis = new FileInputStream(tempMp3);
            mp.setDataSource(fis.getFD());

            mp.prepare();
        } catch (IOException ex) {
            String s = ex.toString();
            ex.printStackTrace();
        }


    }

}



