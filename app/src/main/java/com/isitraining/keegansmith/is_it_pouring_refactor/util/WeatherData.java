package com.isitraining.keegansmith.is_it_pouring_refactor.util;

import java.util.List;

/**
 * Created by keegansmith on 7/10/17.
 */

public class WeatherData {


    WeatherEntry current;
    List<WeatherEntry> extended;

    public WeatherData(WeatherEntry current, List<WeatherEntry> extended){
        this.current = current;
        this.extended = extended;
    }

    public WeatherEntry getCurrent() {
        return current;
    }

    public List<WeatherEntry> getExtended() {
        return extended;
    }
}
