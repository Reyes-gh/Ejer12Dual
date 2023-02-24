package com.example.ejer12dual;

import androidx.appcompat.app.AppCompatActivity;

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

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MediaPlayer mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mp.setDataSource("https://www.youtube.com/watch?v=bcUaeVQYJC4");

        } catch (IOException e) {}

        ImageView iV = findViewById(R.id.iV);
        Drawable teleGif = getDrawable(R.drawable.quieto);
        Drawable teleRun = getDrawable(R.drawable.corriendo);
        Button btPixel = findViewById(R.id.btPixel);
        teleGif.setFilterBitmap(false);
        Glide.with(this).load(teleGif).into(iV);

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