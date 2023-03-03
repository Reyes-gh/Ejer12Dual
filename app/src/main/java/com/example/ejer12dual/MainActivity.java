package com.example.ejer12dual;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ImageView iV;
    ListView lv;
    SQLiteManager sqLiteManager;
    TextView songName;
    ImageButton btnPlay;
    ImageButton btnStop;
    MediaPlayer mp;
    MediaPlayer mp2;
    Song newSong;
    Drawable teleGif;
    Drawable drawablePlay;
    Drawable drawableDelete;
    Drawable drawablePause;
    ImageButton btnDelete;
    ImageButton btnLoop;
    SeekBar seekBar;
    Runnable runSB;
    Handler handlerSB;
    TextView currentDur;
    TextView totalDur;
    AnimatorSet animadorBoton;
    AnimatorSet animadorBoton2;
    AnimatorSet animadorBoton3;
    boolean isSeeking;
    ImageApi  iApi = new ImageApi();
    int casetteIsOn = 0;
    ImageView fotoDisco;

    public MainActivity() throws IOException {
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        iApi.execute();
        super.onCreate(savedInstanceState);

        sqLiteManager = new SQLiteManager(this);
        lv = findViewById(R.id.songList);
        mp = new MediaPlayer();
        isSeeking = false;

        mp2=MediaPlayer.create(this,R.raw.casettein);
        mp2.setAudioStreamType(AudioManager.STREAM_MUSIC);

        handlerSB = new Handler();

        setContentView(R.layout.activity_main);
        iV = findViewById(R.id.iV);

        //teleGif = getDrawable(R.drawable.quieto);
        teleGif = getDrawable(R.drawable.casetteoff);

        //@SuppressLint("UseCompatLoadingForDrawables") Drawable teleRun = getDrawable(R.drawable.corriendo);
        @SuppressLint("UseCompatLoadingForDrawables") Drawable teleRun = getDrawable(R.drawable.casetteon);
        teleGif.setFilterBitmap(false);
        Glide.with(this).load(teleGif).into(iV);

        try {
            updateList();
        } catch (NoSuchFieldException | IllegalAccessException e) {}

        fotoDisco = findViewById(R.id.fotoDisco);
        currentDur = findViewById(R.id.currentDur);
        seekBar = findViewById(R.id.seekBar);
        totalDur = findViewById(R.id.totalDur);
        songName = findViewById(R.id.songName);
        btnPlay = findViewById(R.id.btnPlay);
        btnStop = findViewById(R.id.btnStop);
        btnDelete = findViewById(R.id.btnDelete);
        btnLoop = findViewById(R.id.btnLoop);
        btnPlay.setPadding(0, 0, 0, 0);
        btnStop.setPadding(0, 0, 0, 0);
        btnDelete.setPadding(0, 0, 0, 0);
        btnLoop.setPadding(0, 0, 0, 0);
        disappear();
        animadorBoton = new AnimatorSet();
        animadorBoton2 = new AnimatorSet();
        animadorBoton3 = new AnimatorSet();

        btnLoop.setOnClickListener(v -> loopControl(0));

        lv.setOnItemClickListener((adapterView, view, i, l) -> {
            Drawable d;
            Bitmap bitUrl;

            bitUrl = iApi.getBitmap();

            d = new BitmapDrawable(bitUrl);
            fotoDisco.setImageDrawable(d);

            if ((casetteIsOn==0)) {
                animIntro();
            }

            drawablePlay = getDrawable(R.drawable.newplay);
            Drawable drawableStop = getDrawable(R.drawable.stopnotpressed);
            drawableDelete = getDrawable(R.drawable.trashnotpressed);
            drawablePause = getDrawable(R.drawable.newpause);

            btnLoop.setImageDrawable(getDrawable(R.drawable.loopnotpressed));
            btnPlay.setImageDrawable(drawablePlay);
            btnStop.setImageDrawable(drawableStop);
            btnDelete.setImageDrawable(drawableDelete);
            Glide.with(this).load(teleGif).into(iV);
            newSong = (Song) lv.getItemAtPosition(i);

            StringBuilder sb = new StringBuilder(newSong.getNombre());

            if (sb.charAt(sb.length()-1)=='c') {sb.setLength(sb.length()-5);} else { sb.setLength(sb.length()-4);}

            songName.setText(sb);
            abrirPlayer(newSong);

        });

        btnStop.setOnClickListener(v -> {
            try {
                btnStop.setImageDrawable(getDrawable(R.drawable.stoppressed));
                stopSong(newSong);
            } catch (IOException ignored) {
            }
        });

        btnDelete.setOnClickListener(v -> {

            btnDelete.setImageDrawable(getDrawable(R.drawable.trashpressed));
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);


            final AlertDialog dialog = builder.setNegativeButton("OK", (dialog1, id) -> {
                try {
                    borrarSong(newSong);
                } catch (NoSuchFieldException | IOException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                dialog1.cancel();
            }).setPositiveButton("CANCELAR", (dialog12, id) ->  {
                dialog12.cancel();
                btnDelete.setImageDrawable(getDrawable(R.drawable.trashnotpressed));
            }).create();

            TextView myMsg = new TextView(MainActivity.this);
            myMsg.setText("Es un temazo, ¿la vas a borrar de verdad?");
            myMsg.setTextSize(15);
            myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
            dialog.setView(myMsg);

            dialog.setTitle("¿Borrar canción?");

            dialog.setOnShowListener(arg0 -> {
                Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                negativeButton.setTextColor(0xff00ffff);
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setTextColor(0xff00ffff);
            });
            dialog.show();
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mp.seekTo(progress);
                    seekBar.setProgress(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}});

        btnPlay.setOnClickListener(v -> {
            loopControl(1);
            if (mp.isPlaying()) {
                    mp.pause();
                    btnPlay.setImageDrawable(drawablePlay);
                Glide.with(this).load(teleGif).into(iV);
            } else {
                Glide.with(this).load(teleRun).into(iV);
                mp.start();

                if (!isSeeking) {
                    liveSeekBar();
                }

                btnStop.setImageDrawable(getDrawable(R.drawable.stopnotpressed));
                btnPlay.setImageDrawable(drawablePause);
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
        intent.setType("audio/*");

        startActivityForResult(intent, 230);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {

        if(requestCode == 230){
            if(resultCode == RESULT_OK){
                Uri audioUri = data.getData();

                @SuppressLint("Recycle") Cursor returnCursor =
                        getContentResolver().query(audioUri, null, null, null, null);

                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                String nombre = returnCursor.getString(nameIndex);
                int size = returnCursor.getInt(sizeIndex);

                byte[] bytes = new byte[size];

                    @SuppressLint("Recycle") InputStream inputStream = getContentResolver().openInputStream(audioUri);
                    inputStream.read(bytes);

                    songPaLista(nombre, bytes);

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException | IllegalAccessException e) {
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

    @SuppressLint("SetTextI18n")
    public void borrarSongs(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);

        final AlertDialog dialog = builder.setNegativeButton("OK", (dialog1, id) -> {
            try {
                sqLiteManager.borrarSongs();
                animOutro();
                updateList();
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            dialog1.cancel();
        }).setPositiveButton("CANCELAR", (dialog12, id) -> dialog12.cancel()).create();

        TextView myMsg = new TextView(MainActivity.this);
        myMsg.setText("Woah woah espera ¿en serio quieres borrarlo TODO?");
        myMsg.setTextSize(15);
        myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
        dialog.setView(myMsg);

        dialog.setTitle("¿Borrar TODO?");

        dialog.setOnShowListener(arg0 -> {
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setTextColor(0xff00ffff);
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(0xff00ffff);
        });
        dialog.show();

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
        btnDelete.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.VISIBLE);
        btnPlay.setVisibility(View.VISIBLE);
        songName.setVisibility(View.VISIBLE);
        seekBar.setVisibility(View.VISIBLE);
        currentDur.setVisibility(View.VISIBLE);
        totalDur.setVisibility(View.VISIBLE);
        btnLoop.setVisibility(View.VISIBLE);
        fotoDisco.setVisibility(View.VISIBLE);

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
            seekBar.setMax(mp.getDuration());
            totalDur.setText(getTimeString(mp.getDuration()));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void stopSong(Song song) throws IOException {
        seekBar.setProgress(0);
        mp.stop();
        File tempMp3 = File.createTempFile("kurchina", "mp3", getCacheDir());
        tempMp3.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tempMp3);
        fos.write(song.getSong());
        fos.close();

        btnPlay.setImageDrawable(drawablePlay);

        Glide.with(this).load(teleGif).into(iV);

        mp.reset();

        FileInputStream fis = new FileInputStream(tempMp3);
        mp.setDataSource(fis.getFD());

        mp.prepare();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void borrarSong(Song s) throws NoSuchFieldException, IllegalAccessException, IOException {

        sqLiteManager.borrarCancion(s);
        animOutro();
        updateList();
    }

    public void liveSeekBar(){
        isSeeking = true;
        int currPos = mp.getCurrentPosition();
        currentDur.setText(getTimeString(mp.getCurrentPosition()));

         seekBar.setProgress(currPos);

        // DEBUGGER songName.setText("1 = " + !mp.isPlaying() + " 2= " + (btnPlay.getDrawable().equals(drawablePause)));

        /*if ((!mp.isPlaying())&&(btnPlay.getDrawable().equals(drawablePause))) {
            currentDur.setText(totalDur.getText());
        }*/

            if ((!mp.isPlaying()&&(btnPlay.getDrawable().equals(drawablePause)))&&((!mp.isLooping()))) {
                try{
                    stopSong(newSong);
                    isSeeking=false;
                    return;
                } catch (IOException e) {}
            }

            runSB = this::liveSeekBar;
        handlerSB.postDelayed(runSB, 1000);
    }
    @SuppressLint("DefaultLocale")
    private String getTimeString(long millis) {

        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

        return String.format("%02d", minutes) +
                ":" +
                String.format("%02d", seconds);
    }

    public void animIntro() {
        mp2.start();
        casetteIsOn = 1;

        btnDelete.setImageDrawable(drawableDelete);
        ObjectAnimator trasladar2 = ObjectAnimator.ofFloat(btnPlay, "translationY", 350f, 0);
        ObjectAnimator trasladar3 = ObjectAnimator.ofFloat(btnStop, "translationY", 350f, 0);
        ObjectAnimator trasladar4 = ObjectAnimator.ofFloat(btnDelete, "translationY", 350f, 0);
        ObjectAnimator trasladar5 = ObjectAnimator.ofFloat(seekBar, "translationX", -2000f, 0);
        ObjectAnimator trasladar6 = ObjectAnimator.ofFloat(currentDur, "translationX", -800f, 0);
        ObjectAnimator trasladar7 = ObjectAnimator.ofFloat(totalDur, "translationX", -1600f, 0);
        ObjectAnimator trasladar8 = ObjectAnimator.ofFloat(iV, "translationX", -1200f, 0);
        ObjectAnimator trasladar10 = ObjectAnimator.ofFloat(songName, "translationX", -1800f, 0);
        ObjectAnimator trasladar11 = ObjectAnimator.ofFloat(btnLoop, "translationY", 350f, 0);
        ObjectAnimator trasladar12 = ObjectAnimator.ofFloat(fotoDisco, "translationX", -1200, 0);

        trasladar12.setDuration(1500);
        trasladar11.setDuration(2200);
        trasladar10.setDuration(1500);
        trasladar2.setDuration(1400);
        trasladar3.setDuration(1800);
        trasladar4.setDuration(1000);
        trasladar5.setDuration(1500);
        trasladar6.setDuration(1500);
        trasladar7.setDuration(1500);
        trasladar8.setDuration(1500);

        animadorBoton.playTogether(trasladar12, trasladar11, trasladar10, trasladar2, trasladar3, trasladar4, trasladar5, trasladar6, trasladar7, trasladar8);
        animadorBoton.start();

    }

    public void animOutro(){
        ObjectAnimator trasladar = ObjectAnimator.ofFloat(songName, "translationX", 0, -1800f);
        ObjectAnimator trasladar2 = ObjectAnimator.ofFloat(btnPlay, "translationY", 0, 500f);
        ObjectAnimator trasladar3 = ObjectAnimator.ofFloat(btnStop, "translationY", 0, 500f);
        ObjectAnimator trasladar4 = ObjectAnimator.ofFloat(btnDelete, "translationY", 0, 500f);
        ObjectAnimator trasladar9 = ObjectAnimator.ofFloat(btnLoop, "translationY", 0, 500f);
        ObjectAnimator trasladar5 = ObjectAnimator.ofFloat(seekBar, "translationX", 0, -3000f);
        ObjectAnimator trasladar6 = ObjectAnimator.ofFloat(currentDur, "translationX", 0, -800f);
        ObjectAnimator trasladar7 = ObjectAnimator.ofFloat(totalDur, "translationX", 0, -1600f);
        ObjectAnimator trasladar8 = ObjectAnimator.ofFloat(iV, "translationX", 0, -1200f);
        ObjectAnimator trasladar10 = ObjectAnimator.ofFloat(fotoDisco, "translationX", 0, -1200);

        trasladar10.setDuration(1500);
        trasladar9.setDuration(2700);
        trasladar.setDuration(1500);
        trasladar2.setDuration(1900);
        trasladar3.setDuration(2300);
        trasladar4.setDuration(1500);
        trasladar5.setDuration(1500);
        trasladar6.setDuration(1500);
        trasladar7.setDuration(1500);
        trasladar8.setDuration(1500);
        animadorBoton2.playTogether(trasladar10, trasladar, trasladar2, trasladar3, trasladar4, trasladar5, trasladar6, trasladar7, trasladar8, trasladar9);
        animadorBoton2.start();
        checkAnimation(animadorBoton2);
        casetteIsOn = 0;
    }

    public void disappear() {
        btnLoop.setVisibility(View.INVISIBLE);
        currentDur.setVisibility(View.INVISIBLE);
        totalDur.setVisibility(View.INVISIBLE);
        btnStop.setVisibility(View.INVISIBLE);
        iV.setVisibility(View.INVISIBLE);
        songName.setVisibility(View.INVISIBLE);
        btnPlay.setVisibility(View.INVISIBLE);
        btnDelete.setVisibility(View.INVISIBLE);
        seekBar.setVisibility(View.INVISIBLE);
        fotoDisco.setVisibility(View.INVISIBLE);

    }

    public void checkAnimation(AnimatorSet o) {

        if (o.isRunning()) {
            runSB = () -> checkAnimation(o);
            handlerSB.postDelayed(runSB, 200);
        } else {
            disappear();
            o.end();
        }

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void loopControl(int n) {

        if (n==1) {

            if (!(mp.isLooping())&&btnLoop.getDrawable()==getDrawable(R.drawable.looppressed)) {
                mp.setLooping(true);
            }

        } else {
            if (!(mp.isLooping())) {
                btnLoop.setImageDrawable(getDrawable(R.drawable.looppressed));
                mp.setLooping(true);
            } else {
                btnLoop.setImageDrawable(getDrawable(R.drawable.loopnotpressed));
                mp.setLooping(false);
            }
        }
    }

}



