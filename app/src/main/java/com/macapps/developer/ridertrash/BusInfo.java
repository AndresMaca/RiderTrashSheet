package com.macapps.developer.ridertrash;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Developer on 30/5/2017.
 */

public class BusInfo {


    private  LatLng latLng;
    private String ruta;
    private int speed;
    private int id;
    public BusInfo(LatLng latLng, String ruta, int speed, int id) {
        this.latLng = latLng;
        this.ruta = ruta;
        this.speed = speed;
        this.id = id;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }







}
