package com.gpscloudalert;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by harshi on 04/11/17.
 */

public class MyLocationList {

    private LatLng latLng;
    private int image;
    private int warningLevel;


    MyLocationList(){



    }

    public int getWarningLevel() {
        return warningLevel;
    }




    public MyLocationList(LatLng latLng, int image, int warningLevel){
        this.latLng = latLng;
        this.image = image;
        this.warningLevel = warningLevel;
    }
    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }


}
