package com.example.ejer12dual;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Clase para recoger una imagen de una URL y devolverla como Bitmap
 */
public class ImageApi extends AsyncTask {
    private InputStream is;
    private Bitmap bitUrl;

    public ImageApi() {
    }

    /**
     * función que devuelve el bitmap con la imagen de la URL.
     * @return
     */
    public Bitmap getBitmap() {
        return bitUrl;
    }

    /**
     * Es necesario destacar que para recoger imágenes de una URL con acceso a internet
     * se necesita un hilo asíncrono que se ejecuta nada más iniciar la aplicación.
     * @param o The parameters of the task.
     *
     * @return
     */
    @Override
    protected Object doInBackground(Object[] o) {
        /**
         * Al ejecutarse la aplicación se conecta con la URL y se llama a otrafuncion() (explicada)
         */
        try {
            URLConnection con = new URL(otrafuncion().toString()).openConnection();
            con.connect();
            //otrafuncion se encarga de establecer ciertas directrices y condiciones que se tienen
            //que cumplir si queremos recoger la imagen de la url, estas condiciones se aplican
            //a la conexión con.
            otrafuncion();

            //Almacenamos en el inputStream local el que recibimos de la conexión.

            this.is = con.getInputStream();

            /**
             * Finalmente mediante BitmapFactory decodificamos la información y la pasamos a la
             * variable local bitUrl, lista para usarse con get();
             */

            bitUrl = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public URL otrafuncion() throws IOException {

        URL url = new URL("https://picsum.photos/525");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(true);
        try {
            connection.setRequestMethod("GET");
            connection.connect();
            return connection.getURL();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }
}
