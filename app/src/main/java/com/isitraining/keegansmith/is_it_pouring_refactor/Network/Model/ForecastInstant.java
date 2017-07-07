package com.isitraining.keegansmith.is_it_pouring_refactor.Network.Model;

import java.util.List;

/**
 * Created by keegansmith on 7/7/17.
 */

public class ForecastInstant {
    public double dt;
    public ForecastInstantMain main;
    public List<ForecastInstantWeather> weather;
    public ForecastInstantClouds clouds;
    public ForecastInstantWind wind;
    public ForecastInstantSys sys;
    public String dt_txt;

}
