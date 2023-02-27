package com.example.ejer12dual;

import java.sql.Blob;
import java.util.Arrays;

public class Song {

    private int id;
    private String nombre;
    private byte[] song;


    public Song(int id, String nombre, byte[] song) {
        this.id = id;
        this.nombre = nombre;
        this.song = song;
    }

    @Override
    public String toString() {
        return nombre;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public byte[] getSong() {
        return song;
    }

}