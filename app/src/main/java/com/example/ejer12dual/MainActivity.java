package com.example.ejer12dual;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
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
    ImageApi iApi = new ImageApi();
    int casetteIsOn = 0;
    ImageView fotoDisco;

    public MainActivity() throws IOException {
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        iApi.execute();
        super.onCreate(savedInstanceState);

        /**
         * Creamos un nuevo SQLiteManager para realizar las opciones SQL.
         */
        sqLiteManager = new SQLiteManager(this);
        lv = findViewById(R.id.songList);
        //Instanciamos MediaPlayer para reproducir la música
        mp = new MediaPlayer();
        //La variable isSeeking nos servirá para saber si el hilo que controla la
        // barra de progreso de la canción está puesto en marcha
        isSeeking = false;

        //Instanciamos un nuevo MediaPlayer que reproducirá el sonido del casette al introducirlo.
        mp2 = MediaPlayer.create(this, R.raw.casettein);
        mp2.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //Handler para utilizar en el hilo de la barra de progreso
        handlerSB = new Handler();

        setContentView(R.layout.activity_main);
        iV = findViewById(R.id.iV);

        //Almacenamos el casette en pausa en un drawable.
        teleGif = getDrawable(R.drawable.casetteoff);

        //Almacenamos el casette en play en un drawable.
        @SuppressLint("UseCompatLoadingForDrawables") Drawable teleRun = getDrawable(R.drawable.casetteon);
        teleGif.setFilterBitmap(false);
        //Mediante Glide, hacemos que iV (La imagen principal del Layout) cargue el casette.
        Glide.with(this).load(teleGif).into(iV);

        /**
         * El método updateList se encarga de actualizar la lista de canciones almacenada en la base de datos
         * del dispositivo.
         */
        try {
            updateList();
        } catch (NoSuchFieldException | IllegalAccessException e) {
        }

        /**
         * Almacenamos todos los elementos del layout para poder animarlos luego.
         */
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
        //El método disappear hace que los elementos sean invisibles e inutilizables.
        disappear();
        //Creamos los nuevos animadores.
        animadorBoton = new AnimatorSet();
        animadorBoton2 = new AnimatorSet();
        animadorBoton3 = new AnimatorSet();

        //Hacemos que btnLoop llame a su método loopControl, pasamos por parámetro 0
        //para que acceda a la configuración destinada al botón de Loop (explicado en el método)
        btnLoop.setOnClickListener(v -> loopControl(0));

        /**
         * Al hacer click en la lista de canciones creamos la nueva carátula del casette.
         * La carátula consta de una imagen cargada desde picsum con la API creada (y explicada en
         * su propia clase)
         *
         * Al conseguir el Bitmap de la API lo pasamos a Drawable, y hacemos que la foto del casette
         * sea el Drawable conseguido del Bitmap.
         */
        lv.setOnItemClickListener((adapterView, view, i, l) -> {
            Drawable d;
            Bitmap bitUrl;

            bitUrl = iApi.getBitmap();

            d = new BitmapDrawable(bitUrl);
            fotoDisco.setImageDrawable(d);

            /**
             * Si el casette no está puesto (0) haremos que llame a la animación de intro.
             * En caso contrario no pasará nada, pues significa que ya hay un casette insertado.
             *
             * (El cambio entre canciones no comprende un cambio de casette aunque sea lo más realista,
             * pues puede llegar a ser molesto el sonido y la animación cada vez que cambiemos de canción)
             *
             */
            if ((casetteIsOn == 0)) {
                animIntro();
            }

            /* Una vez hayamos seleccionado la canción conseguimos todos los Drawables con los
            *  que interactúa el usuario (Botón de play, pause, borrar, loop, etc...)
            *
            *  Hacemos que el drawable por defecto de ellos sea no pulsado.
            *
             */

            drawablePlay = getDrawable(R.drawable.newplay);
            Drawable drawableStop = getDrawable(R.drawable.stopnotpressed);
            drawableDelete = getDrawable(R.drawable.trashnotpressed);
            drawablePause = getDrawable(R.drawable.newpause);

            btnLoop.setImageDrawable(getDrawable(R.drawable.loopnotpressed));
            btnPlay.setImageDrawable(drawablePlay);
            btnStop.setImageDrawable(drawableStop);
            btnDelete.setImageDrawable(drawableDelete);
            /**
             * Nos aseguramos de que al cambiar de canción el gif del casette
             * vuelva a ser el del casette quieto.
             */
            Glide.with(this).load(teleGif).into(iV);
            /**
             * Almacenamos en un objeto canción la canción seleccionada desde la lista
             */
            newSong = (Song) lv.getItemAtPosition(i);

            /**
             * Le quitamos la extensión de archivo
             */
            StringBuilder sb = new StringBuilder(newSong.getNombre());

            if (sb.charAt(sb.length() - 1) == 'c') {
                sb.setLength(sb.length() - 5);
            } else {
                sb.setLength(sb.length() - 4);
            }

            /**
             * Una vez todo el proceso de selección ha ocurrido hacemos que el título del casette
             * sea el del objeto canción, y abrimos el reproductor con la canción.
             *
             * (La clase canción almacena la canción en bytes[])
             */
            songName.setText(sb);
            abrirPlayer(newSong);

        });

        /**
         * Al pulsar el botón stop este se quedará pulsado hasta que hagamos cualquier otra acción.
         * Además, llama al método stopSong, que detendrá la canción junto con todas las animaciones.
         */
        btnStop.setOnClickListener(v -> {
            try {
                btnStop.setImageDrawable(getDrawable(R.drawable.stoppressed));
                stopSong(newSong);
            } catch (IOException ignored) {
            }
        });

        /**
         * El botón delete hace saltar un AlertDialog en caso de que lo hayamos pulsado sin querer.
         */

        btnDelete.setOnClickListener(v -> {

            btnDelete.setImageDrawable(getDrawable(R.drawable.trashpressed));
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);

            /**
             * Si elegimos la opción afirmativa, llamará al método borrarSong con el parámetro de la canción
             * actual, borrándola de la DB.
             */

            final AlertDialog dialog = builder.setNegativeButton("OK", (dialog1, id) -> {
                try {
                    borrarSong(newSong);
                } catch (NoSuchFieldException | IOException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                dialog1.cancel();
                /**
                 * En caso negativo no sucede nada.
                 */
            }).setPositiveButton("CANCELAR", (dialog12, id) -> {
                dialog12.cancel();
                btnDelete.setImageDrawable(getDrawable(R.drawable.trashnotpressed));
            }).create();

            TextView myMsg = new TextView(MainActivity.this);
            myMsg.setText("Es un temazo, ¿la vas a borrar de verdad?");
            myMsg.setTextSize(15);
            myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
            dialog.setView(myMsg);
            /**
            *  Creación y construcción del dialog:
            */
            dialog.setTitle("¿Borrar canción?");

            dialog.setOnShowListener(arg0 -> {
                Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                negativeButton.setTextColor(0xff00ffff);
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setTextColor(0xff00ffff);
            });
            dialog.show();
        });

        /**
         * Al pulsar en la barra de progreso de la canción haremos que el mediaPlayer se mueva a
         * esa distancia de progreso, al igual que el SeekBar. Esto nos permite adelantar o volver
         * a reproducir partes de la canción arbitrariamente.
         */

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
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        /**
         * Al pulsar el botón de play llamamos a loopControl con parámetro 1, para que aplique
         * la configuración destinada al botón de play (explicado en el método)
         */

        btnPlay.setOnClickListener(v -> {
                    loopControl(1);
                    /**
                     * Si el mp está sonando, haremos que se pare y pondremos el botón
                     * con su aspecto de preparado para pulsarse de nuevo y reproducir.
                     */
                    if (mp.isPlaying()) {
                        mp.pause();
                        btnPlay.setImageDrawable(drawablePlay);
                        /**
                         * Cargamos el gif del casette quieto, ya que hemos entrado en el if
                         * de pausa.
                         */
                        Glide.with(this).load(teleGif).into(iV);

                    } else {
                        /**
                         * En caso contrario (que el casette no esté activo)
                         * haremos que el gif de casette en marcha cargue en la imagen
                         * (una de las formas de hacerlo es mediante Glide)
                         */
                        Glide.with(this).load(teleRun).into(iV);
                        //Y le damos al play.
                        mp.start();

                        /**
                         * Si la barra de progreso no está buscando progreso en el MediaPlayer lo
                         * controlamos, y haremos que lo haga llamando a su método (explicado en el
                         * método)
                         */
                        if (!isSeeking) {
                    liveSeekBar();
                }

                btnStop.setImageDrawable(getDrawable(R.drawable.stopnotpressed));
                btnPlay.setImageDrawable(drawablePause);
            }
        }
        );
    }

    /**
     * Método newSong que llama a openFile
     * @param view
     */
    public void newSong(View view) {
        openFile();
    }

    /**
     * openFile se encarga de crear una intención en Android. Las intenciones (INTENT) en android sirven
     * por ejemplo como en este caso para abrir el explorador de archivos.
     */
    private void openFile() {
        Intent intent;
        intent = new Intent();
        //Hacemos que solo podamos seleccionar archivos de tipo audio, como mp3 o flac
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");

        //Marcamos la intención con la ID 230 y lo lanzamos
        startActivityForResult(intent, 230);
    }

    /**
     *
     * Detecta cuando ha acabado la intención y recoge el código de la intención recibida
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     *
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {

            //Si la intención recibida es igual a la id que nosotros pusimos anteriormente
            //se encargará de ejecutar lo siguiente:
        if(requestCode == 230){
            if(resultCode == RESULT_OK){
                /**
                 * Almacena la URI del archivo, y con un cursor recorre los datos del archivo destino
                 * de esa misma URI
                 */
                Uri audioUri = data.getData();

                @SuppressLint("Recycle") Cursor returnCursor =
                        getContentResolver().query(audioUri, null, null, null, null);

                /**
                 * Vamos creando los parámetros de la canción con el cursor de la URI
                 * entre ellos el nombre y el tamaño
                 */
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                String nombre = returnCursor.getString(nameIndex);
                int size = returnCursor.getInt(sizeIndex);

                byte[] bytes = new byte[size];

                    @SuppressLint("Recycle") InputStream inputStream = getContentResolver().openInputStream(audioUri);
                    inputStream.read(bytes);

                /**
                 * Una vez almacenados todos los datos mandamos el nombre y el array de bytes de la canción
                 * a la lista con el método songPaLista()
                 */
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

    /**
     *
     * El método songPaLista se encarga de añadir a la lista la canción que haya recogido
     * la intención.
     *
     * @param file el nombre del archivo junto con su extensión
     * @param bytes el array de bytes que contiene la canción
     * @throws IOException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public void songPaLista(String file, byte[] bytes) throws IOException, NoSuchFieldException, IllegalAccessException {

        /**
         * En un ArrayList de canciones almacena todas las canciones que hay
         * en la base de datos
         */

        ArrayList<Song> arraySongs = sqLiteManager.getSongs();

        /**
         * Con el tamaño del array creamos una canción con la ID, nombre y array
         * de bytes adecuados.
         */
        Song s1 = new Song(arraySongs.toArray().length+1, file, bytes);

        /**
         * Añadimos la canción a la base de datos con el método addSong de la clase
         * SQLiteManager.
         */
        sqLiteManager.addSong(s1);
        /**
         * Almacenamos las canciones de la base de datos en un array.
         */
        arraySongs = sqLiteManager.getSongs();


        /**
         * Adaptamos el array al ListView para que almacene las canciones.
         */
        ArrayAdapter<Song> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                arraySongs);

        lv = findViewById(R.id.songList);
        lv.setAdapter(arrayAdapter);
    }

    /**
     *
     * El método borrarSongs pertenece al botón grande que indica "BORRAR TODO".
     * Este método limpia la base de datos y el ListView con un AlertDialog similar al
     * visto anteriormente.
     * @param view
     */
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

    /**
     * El método updateList se encarga de recoger las canciones de la DB en un array
     * y ponerlas en la ListView
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
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

        /**
         * Al abrir el reproductor hacemos todos los elementos visibles para
         * poder interactuar con ellos
         */

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

            /**
             * Creamos un fichero temporal en el teléfono, donde se almacenará el array
             * de bytes que contiene la canción.
             *
             * Entonces pasamos por parámetro un objeto de tipo Song a este metodo, el cual lo
             * utilizara para acceder a su funcion getSong (y conseguir el array de bytes)
             *
             * Escribimos el array de bytes en el archivo mp3 creado en el directorio temporal
             * del telefono
             *
             * Cerramos el OutputStream.
             */
            File tempMp3 = File.createTempFile("kurchina", "mp3", getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(song.getSong());
            fos.close();

            /**
             *Reseteamos el MediaPlayer para que no dé error si lo intentamos reproducir de nuevo.
             */

            mp.reset();

            /**
             * Entonces mediante un InputStream le pasamos por parámetro la canción almacenada en el
             * directorio temporal
             */
            FileInputStream fis = new FileInputStream(tempMp3);
            mp.setDataSource(fis.getFD());

            mp.prepare();
            /**
             * Establecemos la duración de la barra de progreso con la duración del MediaPlayer
             * tras hacerle .prepare()
             */
            seekBar.setMax(mp.getDuration());
            /**
             * Hacemos que la duración total mostrada sea la duración de la canción pasada por parámetro
             * al player
             */
            totalDur.setText(getTimeString(mp.getDuration()));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void stopSong(Song song) throws IOException {

        /**
         * Al parar la canción hacemos que la barra de progreso vuelva a su estado inicial.
         *
         * Entonces paramos el MediaPlayer, y para evitar errores lo reseteamos, creando de nuevo
         * un archivo temporal donde guardar la canción para entonces poder pasárselo de nuevo
         * por parámetro al MediaPlayer con un InputStream.
         *
         * De esta manera nos aseguramos de que la canción se detiene y el MediaPlayer no tenga ningún error.
         */

        seekBar.setProgress(0);
        mp.stop();
        File tempMp3 = File.createTempFile("tempmp3", "mp3", getCacheDir());
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

    /**
     * El método borrarSong se encarga de borrar la canción pasada por parámetro llamando al SQLiteManager.
     * Además, hace la animación de sacar casette y actualiza la lista de canciones para que el borrado
     * sea visible
     * @param s
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IOException
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    public void borrarSong(Song s) throws NoSuchFieldException, IllegalAccessException, IOException {

        sqLiteManager.borrarCancion(s);
        animOutro();
        updateList();
    }


    /**
     * El método liveSeekBar se encarga de ejecutarse cada un segundo con el Handler.
     */
    public void liveSeekBar(){
        //Lo primero que hace es activarse poniendo isSeeking = true;
        isSeeking = true;
        //Almacena la posición del MediaPlayer actual en una variable
        int currPos = mp.getCurrentPosition();
        //Mediante el método getTimeString que transforma los ms en minutos y segundos
        //cambiamos el texto de progreso de la izquierda.
        currentDur.setText(getTimeString(mp.getCurrentPosition()));

        //A su vez, cambiamos el progreso de la barra de progreso al actual del mediaplayer.
         seekBar.setProgress(currPos);

        /**
         * El siguiente if es necesario puesto que el cálculo de horas y minutos del método getTimeString
         * puede no ser exacto, y hacer que el MediaPlayer deje de ejecutar liveSeekBar(), dejando por ejemplo
         * la duración total en 124999 pero la actual en 124789, estropeando los métodos para pararse automáticamente
         * o volver a reproducirse
         *
         * Si se detecta que el Player no está en marcha pero el botón de play está pulsado y el MediaPlayer no
         * está en bucle, paramos la canción y establecemos la variable isSeeking en false, pues ya no está buscando.
         */
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

    /**
     * getTimeString es una clase que transforma los milisegundos en minutos y segundos
     * @param millis
     * @return
     */
    @SuppressLint("DefaultLocale")
    private String getTimeString(long millis) {

        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

        return String.format("%02d", minutes) +
                ":" +
                String.format("%02d", seconds);
    }

    /**
     * animIntro() reproduce la animación de entrada del casette y los botones
     * así como el sonido introductorio del casette.
     */
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

    /**
     * La animación de salida animOutro() hace un trabajo muy similar al de animIntro() pero con animaciones
     * ligeralmente diferentes.
     */
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

    /**
     * El método disappear hace invisibles e imposibles de utilizar ciertos objetos
     * como los controles de reproducción y el casette.
     */
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

    /**
     * Utilizando el mismo Handler que el de la barra de progreso controlamos cuando
     * se detienen las animaciones de outro y en cuanto acaban ejecutamos disappear para
     * simular un efecto de desaparición.
     * @param o
     */
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

        /**
         * Al pasar por parámetro el número 1, estamos haciendo que se controle el hipotético caso
         * de que pulsemos el botón de play y el botón de Loop esté pulsado, pero en realidad
         * el MediaPlayer no esté en bucle, para evitar esto utilizamos un if que lo controla
         * y pone el MediaPlayer en bucle
         */
        if (n==1) {

            if (!(mp.isLooping())&&btnLoop.getDrawable()==getDrawable(R.drawable.looppressed)) {
                mp.setLooping(true);
            }

            /**
             * Si pasamos por parámetro cualquier otro número (en este caso el único que lo usa
             * es el botón de loop con un 0) controlaremos que si no está en bucle lo esté y viceversa.
             */
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



