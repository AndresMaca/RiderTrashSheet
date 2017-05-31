package com.macapps.developer.ridertrash;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Developer on 30/5/2017.
 */

public class Bus implements Parcelable {

    int imagen,tiempo;
    String route,parada_inicial,parada_final;

    public Bus(int imagen, int tiempo, String route, String parada_inicial, String parada_final) {
        this.imagen = imagen;
        this.tiempo = tiempo;
        this.route = route;
        this.parada_inicial = parada_inicial;
        this.parada_final = parada_final;
    }
    protected Bus(Parcel in) {

        imagen = in.readInt();
        tiempo=in.readInt();
        route = in.readString();
        parada_final=in.readString();
        parada_inicial=in.readString();

    }

    public static final Creator<Bus> CREATOR = new Creator<Bus>() {
        @Override
        public Bus createFromParcel(Parcel in) {
            return new Bus(in);
        }

        @Override
        public Bus[] newArray(int size) {
            return new Bus[size];
        }
    };

    public int getImagen() {
        return imagen;
    }

    public void setImagen(int imagen) {
        this.imagen = imagen;
    }

    public int getTiempo() {
        return tiempo;
    }

    public void setTiempo(int tiempo) {
        this.tiempo = tiempo;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getParada_inicial() {
        return parada_inicial;
    }

    public void setParada_inicial(String parada_inicial) {
        this.parada_inicial = parada_inicial;
    }

    public String getParada_final() {
        return parada_final;
    }

    public void setParada_final(String parada_final) {
        this.parada_final = parada_final;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(imagen);
        dest.writeInt(tiempo);
        dest.writeString(route);
        dest.writeString(parada_inicial);
        dest.writeString(parada_final);


    }
}
