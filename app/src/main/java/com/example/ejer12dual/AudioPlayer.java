package com.example.ejer12dual;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AudioPlayer {
    private AudioTrack audioTrack;
    private Thread playThread;

    public void playMp3FromByteArray(byte[] mp3Data) {
        int bufferSizeInBytes = AudioTrack.getMinBufferSize(
                44100, // sample rate
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        // Create a new AudioTrack object and set its properties.
        audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                44100, // sample rate
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSizeInBytes,
                AudioTrack.MODE_STREAM
        );

        // Create a new thread to play the audio data.
        playThread = new Thread(() -> {
            try {
                // Create a new ByteArrayInputStream from the byte array.
                InputStream inputStream = new ByteArrayInputStream(mp3Data);

                // Create a buffer to read the data into.
                byte[] buffer = new byte[bufferSizeInBytes];

                // Start playing the audio data.
                audioTrack.play();

                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    audioTrack.write(buffer, 0, bytesRead);
                }

                // Stop playing the audio data and release the resources.
                audioTrack.stop();
                audioTrack.release();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Start the thread.
        playThread.start();
    }
}
