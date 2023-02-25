package com.example.ejer12dual;

import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifBitmapProvider;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawableResource;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView iV = findViewById(R.id.iV);
        Drawable teleGif = getDrawable(R.drawable.quieto);
        Drawable teleRun = getDrawable(R.drawable.corriendo);
        Button btPixel = findViewById(R.id.btPixel);
        teleGif.setFilterBitmap(false);
        Glide.with(this).load(teleGif).into(iV);

        SQLiteDatabase sqLiteDatabase;

       // sqLiteDatabase.execSQL

        MediaPlayer mp = new MediaPlayer();

        try {mp.setDataSource("");} catch (IOException e) {}






        btPixel.setOnClickListener(v -> {

            if (iV.getDrawable()==teleGif) {

                Glide.with(this).load(teleRun).into(iV);
                mp.start();
            } else {
                Glide.with(this).load(teleGif).into(iV);
                if (mp.isPlaying()) {
                    mp.stop();
                }
            }

        });
    }
}