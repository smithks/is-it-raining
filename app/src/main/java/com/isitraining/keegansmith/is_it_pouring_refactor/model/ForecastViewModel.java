package com.isitraining.keegansmith.is_it_pouring_refactor.model;

import android.content.Context;
import android.preference.PreferenceManager;

import com.isitraining.keegansmith.is_it_pouring_refactor.R;
import com.isitraining.keegansmith.is_it_pouring_refactor.network.Location;
import com.isitraining.keegansmith.is_it_pouring_refactor.util.WeatherData;
import com.isitraining.keegansmith.is_it_pouring_refactor.util.WeatherEntry;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Provides access to RxJava observable objects that contain the current forecast information to be displayed.
 * Created by keegansmith on 7/10/17.
 */

public class ForecastViewModel{

    Context appContext;
    PublishSubject<WeatherData> forecastObservable;
    ForecastModel forecastModel;
    public static ForecastViewModel viewModel;

    //TODO: Use this singleton??
    public static ForecastViewModel getViewModel(Context appContext){
        if (viewModel == null){
            viewModel = new ForecastViewModel(appContext);
        }

        return viewModel;
    }

    public ForecastViewModel(Context appContext){
        this.appContext = appContext;
        String location = PreferenceManager.getDefaultSharedPreferences(appContext).getString(appContext.getResources().getString(R.string.user_device_location_lat_long),"");
        String[] latLon = location.split(" ");
        forecastModel = new ForecastModel(new Location(latLon[0],latLon[1]),this);
        forecastObservable = PublishSubject.create();
    }

    public Observable<WeatherData> getForecastObservable(){
        return forecastObservable;
    }

    /**
     * Called by the ForecastModel class to pass in the forecast information to be translated to
     * the observable data.
     * @param forecast the new forecast data
     */
    public void setWeatherData(WeatherData forecast) {
        WeatherEntry current = forecast.getCurrent();
        current.setWeatherDescriptionFriendly(getCurrentWeatherString(current.getWeatherCode()));
        current.setWeatherIcon(getWeatherIcon(current.getWeatherIconIdentifier()));
        List<WeatherEntry> friendlyExtended = new ArrayList<>();
        for (WeatherEntry entry: forecast.getExtended()){
            WeatherEntry friendlyEntry = makeUserFriendly(entry);
            friendlyExtended.add(friendlyEntry);
        }
        forecast.setExtended(friendlyExtended);
        forecastObservable.onNext(forecast);
    }

    /**
     * Updates the raw weather data with more user friendly data before sending to view.
     * @param entry the entry to update
     * @return the entry with fields updated
     */
    private WeatherEntry makeUserFriendly(WeatherEntry entry){
        entry.setWeatherDescriptionFriendly(getWeatherDescription(entry.getWeatherCode(),entry.getWeatherDescriptionRaw()));
        entry.setWeatherIcon(getWeatherIcon(entry.getWeatherIconIdentifier()));
        return entry;
    }

    /**
     * Returns a string to display to the user based on the weather code that is passed in.
     * @param weatherCode the weather code to check
     * @return a user facing string representing the current weather
     */
    private String getCurrentWeatherString(int weatherCode) {
        String result;
        if (weatherCode >= 200 && weatherCode <= 232) { //Thunderstorm
            result = appContext.getResources().getString(R.string.today_forecast_current_thunderstorm);
        } else if (weatherCode >= 300 && weatherCode <= 321) { //Drizzle
            result = appContext.getResources().getString(R.string.today_forecast_current_small_chance_rain);
        } else if (weatherCode == 500) {
            result = appContext.getResources().getString(R.string.today_forecast_current_small_chance_rain);
        } else if (weatherCode >= 501 && weatherCode <= 531) { //Rain
            result = appContext.getResources().getString(R.string.today_forecast_current_raining);
        } else if (weatherCode >= 600 && weatherCode <= 622) { //Snow
            result = appContext.getResources().getString(R.string.today_forecast_current_snow);
        } else {
            result = appContext.getResources().getString(R.string.today_forecast_current_no_precipitation);
        }
        return result;
    }

    /**
     * Formats a weather description for display.
     * @param weatherID the weather type to describe
     * @param weatherDesc the current description of the weather
     * @return the formatted string
     */
    private String getWeatherDescription(int weatherID, String weatherDesc){
        String weatherDescription = weatherDesc;
        switch (weatherID){
            case 300: //light intensity drizzle
                weatherDescription = appContext.getResources().getString(R.string.extended_forecast_weather_light_drizzle);
                break;
            case 302: //heavy intensity drizzle
                weatherDescription = appContext.getResources().getString(R.string.extended_forecast_weather_heavy_drizzle);
                break;
            case 310: //light intensity drizzle rain
                weatherDescription = appContext.getResources().getString(R.string.extended_forecast_weather_light_drizzle_rain);
                break;
            case 312: //heavy intensity drizzle rain
                weatherDescription = appContext.getResources().getString(R.string.extended_forecast_weather_heavy_drizzle_rain);
                break;
            case 313: //shower rain and drizzle
            case 321: //shower drizzle
                weatherDescription = appContext.getResources().getString(R.string.extended_forecast_weather_showers_and_drizzle);
                break;
            case 314: //heavy shower rain and drizzle
                weatherDescription = appContext.getResources().getString(R.string.extended_forecast_weather_heavy_showers_and_drizzle);
                break;
            case 502: //heavy intensity rain
                weatherDescription = appContext.getResources().getString(R.string.extended_forecast_weather_heavy_rain);
                break;
            case 520: //light intensity shower rain
                weatherDescription = appContext.getResources().getString(R.string.extended_forecast_weather_light_showers);
                break;
            case 521: //shower rain
                weatherDescription = appContext.getResources().getString(R.string.extended_forecast_weather_rain_showers);
                break;
            case 522: //heavy intensity shower rain
                weatherDescription = appContext.getResources().getString(R.string.extended_forecast_weather_heavy_showers);
                break;
            case 531: //ragged shower rain
                weatherDescription = appContext.getResources().getString(R.string.extended_forecast_weather_ragged_showers);
                break;
            case 612: //shower sleet
                weatherDescription = appContext.getResources().getString(R.string.extended_forecast_weather_sleet_showers);
                break;
            case 620: //light shower snow
                weatherDescription = appContext.getResources().getString(R.string.extended_forecast_weather_light_snow_showers);
                break;
            case 621: //shower snow
                weatherDescription = appContext.getResources().getString(R.string.extended_forecast_weather_snow_showers);
                break;
            case 622: //heavy shower snow
                weatherDescription = appContext.getResources().getString(R.string.extended_forecast_weather_heavy_snow_showers);
                break;
            case 800:
                weatherDescription = "Clear";
                break;
        }
        weatherDescription = weatherDescription.substring(0,1).toUpperCase() + weatherDescription.substring(1); //Capitalize the first character
        return weatherDescription;
    }

    /**
     * Returns the id of a drawable that represents the icon of the weather type that is passed into the method.
     * @param iconName the name of the weather icon
     * @return the id of a drawable resource
     */
    private static int getWeatherIcon(String iconName){
        int weatherIcon;
        switch (iconName){
            case "01d":
                weatherIcon = R.drawable.weather_icon_01d;
                break;
            case "01n":
                weatherIcon = R.drawable.weather_icon_01n;
                break;
            case "02d":
                weatherIcon = R.drawable.weather_icon_02d;
                break;
            case "02n":
                weatherIcon = R.drawable.weather_icon_02n;
                break;
            case "03d":
                weatherIcon = R.drawable.weather_icon_03d;
                break;
            case "03n":
                weatherIcon = R.drawable.weather_icon_03n;
                break;
            case "04d":
                weatherIcon = R.drawable.weather_icon_04d;
                break;
            case "04n":
                weatherIcon = R.drawable.weather_icon_04n;
                break;
            case "09d":
                weatherIcon = R.drawable.weather_icon_09d;
                break;
            case "09n":
                weatherIcon = R.drawable.weather_icon_09n;
                break;
            case "10d":
                weatherIcon = R.drawable.weather_icon_10d;
                break;
            case "10n":
                weatherIcon = R.drawable.weather_icon_10n;
                break;
            case "11d":
                weatherIcon = R.drawable.weather_icon_11d;
                break;
            case "11n":
                weatherIcon = R.drawable.weather_icon_11n;
                break;
            case "13d":
                weatherIcon = R.drawable.weather_icon_13d;
                break;
            case "13n":
                weatherIcon = R.drawable.weather_icon_13n;
                break;
            default:
                weatherIcon = R.drawable.weather_icon_50d;
        }
        return weatherIcon;
    }
}
