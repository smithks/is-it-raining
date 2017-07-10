package com.isitraining.keegansmith.is_it_pouring_refactor.network;

/**
 * Created by keegansmith on 7/10/17.
 */

public class Location {

    public String lat;
    public String lon;

    public Location(String lat, String lon){
        this.lat = lat;
        this.lon = lon;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }
}
