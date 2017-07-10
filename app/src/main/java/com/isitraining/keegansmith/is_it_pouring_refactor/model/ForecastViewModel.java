package com.isitraining.keegansmith.is_it_pouring_refactor.model;

import android.content.Context;
import android.preference.PreferenceManager;

import com.isitraining.keegansmith.is_it_pouring_refactor.R;
import com.isitraining.keegansmith.is_it_pouring_refactor.network.Location;
import com.isitraining.keegansmith.is_it_pouring_refactor.network.model.ForecastAll;
import com.isitraining.keegansmith.is_it_pouring_refactor.util.WeatherData;

import rx.Observable;
/**
 * Provides access to RxJava observable objects that contain the current forecast information to be displayed.
 * Created by keegansmith on 7/10/17.
 */

public class ForecastViewModel implements ForecastViewModelInterface{

    Context appContext;
    Observable<WeatherData> forecastObservable;
    ForecastModel forecastModel;

    public ForecastViewModel(Context appContext){
        this.appContext = appContext;
        String location = PreferenceManager.getDefaultSharedPreferences(appContext).getString(appContext.getResources().getString(R.string.user_device_location_lat_long),"");
        String[] latLon = location.split(" ");
        forecastModel = new ForecastModel(new Location(latLon[0],latLon[1]),this);

    }

    public Observable<WeatherData> getForecastObservable(){
        return forecastObservable;
    }

    /**
     * Called by the ForecastModel class to pass in the forecast information to be translated to
     * the observable data.
     * @param forecast
     */
    @Override
    public void setWeatherData(WeatherData forecast) {
        forecastObservable = Observable.just(forecast);
    }
}
