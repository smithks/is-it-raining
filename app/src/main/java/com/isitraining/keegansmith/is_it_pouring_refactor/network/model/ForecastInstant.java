package com.isitraining.keegansmith.is_it_pouring_refactor.network.model;

import java.util.List;

/**
 * Created by keegansmith on 7/7/17.
 */

public class ForecastInstant {
    public long dt;
    public ForecastInstantMain main;
    public List<ForecastInstantWeather> weather;
    public ForecastInstantClouds clouds;
    public ForecastInstantWind wind;
    public ForecastInstantSys sys;
    public String dt_txt;

    public long getDt() {
        return dt;
    }

    public void setDt(long dt) {
        this.dt = dt;
    }

    public ForecastInstantMain getMain() {
        return main;
    }

    public void setMain(ForecastInstantMain main) {
        this.main = main;
    }

    public List<ForecastInstantWeather> getWeather() {
        return weather;
    }

    public void setWeather(List<ForecastInstantWeather> weather) {
        this.weather = weather;
    }

    public ForecastInstantClouds getClouds() {
        return clouds;
    }

    public void setClouds(ForecastInstantClouds clouds) {
        this.clouds = clouds;
    }

    public ForecastInstantWind getWind() {
        return wind;
    }

    public void setWind(ForecastInstantWind wind) {
        this.wind = wind;
    }

    public ForecastInstantSys getSys() {
        return sys;
    }

    public void setSys(ForecastInstantSys sys) {
        this.sys = sys;
    }

    public String getDt_txt() {
        return dt_txt;
    }

    public void setDt_txt(String dt_txt) {
        this.dt_txt = dt_txt;
    }
}
