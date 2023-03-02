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

public class ImageApi extends AsyncTask {

    private InputStream is;
    private Bitmap bitUrl;

    public ImageApi() throws IOException {

    }

    public Bitmap getBitmap() {
        return bitUrl;
    }

    public InputStream getIS() {
        return this.is;
    }

    @Override
    protected Object doInBackground(Object[] o) {
        try {

            URLConnection con = new URL(otrafuncion().toString()).openConnection();
            con.connect();
            otrafuncion();
            System.out.println("JSKLDADJKLASKLAJSDKLSDJDAJSKLKADSKLAJDSK" + con.getURL());
            this.is = con.getInputStream();
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
            connection.setConnectTimeout(60000);
            connection.setReadTimeout(60000);
            connection.connect();

            int code = connection.getResponseCode();
            String message = connection.getResponseMessage();

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
