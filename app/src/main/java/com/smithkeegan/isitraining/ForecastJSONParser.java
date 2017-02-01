package com.smithkeegan.isitraining;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A JSON parser for creating WeatherEntry objects from weather forecast information passed in
 * as JSON.
 * @author Keegan Smith
 * @since 12/14/2016
 */

public class ForecastJSONParser {

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed.
     */
    public static ArrayList<WeatherEntry> getEntriesFromJSON(String forecastJsonStr, Context context) throws JSONException{
        ArrayList<WeatherEntry> result = new ArrayList<>();

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_DATE = "dt";
        final String OWM_MAIN = "main";
        final String OWM_WEATHER_ID = "id";
        final String OWM_WEATHER_DESCRIPTION = "description";
        final String OWM_WEATHER_ICON = "icon";
        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_CITY_COUNTRY = "country";

        //Create a JSON object from passed in string
        JSONObject forecastJson = new JSONObject(forecastJsonStr);

        //Get the location the weather data is for
        JSONObject cityObject = forecastJson.getJSONObject(OWM_CITY);
        String location = cityObject.getString(OWM_CITY_NAME) + ", "+cityObject.getString(OWM_CITY_COUNTRY);

        //Get array of weather data divided in 3 hour chunks
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        Calendar currentTime = Calendar.getInstance();

        for(int i = 0; i < weatherArray.length(); i++) {
            String shortDate,shortTime;
            double temperature;
            int weatherID;
            String weatherDescription;
            String weatherIcon;

            JSONObject dayForecast = weatherArray.getJSONObject(i); //Get this JSON object representing a 3 hour block

            long dateTime = dayForecast.getLong(OWM_DATE); //Get the UTC timestamp for this block

            //Format the date as the hour followed by am/pm marker. ex "5 PM"
            Date dateObject = new Date(dateTime * 1000); //Convert from seconds to milliseconds
            Calendar calendarObject = Calendar.getInstance();
            calendarObject.setTime(dateObject);
            shortDate = new SimpleDateFormat("EEE, MMMM d", Locale.getDefault()).format(dateObject);
            shortTime = new SimpleDateFormat("h:mm a",Locale.getDefault()).format(dateObject);

            temperature = dayForecast.getJSONObject(OWM_MAIN).getDouble(OWM_TEMPERATURE); //Get temperature from "main" object

            //Get the weather id from the weather json array titled "weather" that contains a single object
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            weatherID = weatherObject.getInt(OWM_WEATHER_ID);
            weatherDescription = weatherObject.getString(OWM_WEATHER_DESCRIPTION);
            weatherIcon = weatherObject.getString(OWM_WEATHER_ICON);

            //Create a weather entry and pass in the collected values.
            WeatherEntry newEntry = new WeatherEntry();
            newEntry.setLocation(location);
            newEntry.setDateObject(dateObject);
            newEntry.setDateShort(shortDate);
            newEntry.setIsToday(currentTime.get(Calendar.DATE) == calendarObject.get(Calendar.DATE)); //True if this weather information is from today
            newEntry.setWeatherTime(shortTime);
            newEntry.setTemperature(temperature);
            newEntry.setWeatherCode(weatherID);
            newEntry.setWeatherDescription(getWeatherDescription(context,weatherID, weatherDescription));
            newEntry.setWeatherIcon(getWeatherIcon(weatherIcon));

            result.add(newEntry);
        }

        return result;
    }

    /**
     * Formats a weather description for display.
     * @param weatherID the weather type to describe
     * @param weatherDesc the current description of the weather
     * @return the formatted string
     */
    private static String getWeatherDescription(Context context, int weatherID, String weatherDesc){
        String weatherDescription = weatherDesc;
        switch (weatherID){
            case 300: //light intensity drizzle
                weatherDescription = context.getResources().getString(R.string.extended_forecast_weather_light_drizzle);
                break;
            case 302: //heavy intensity drizzle
                weatherDescription = context.getResources().getString(R.string.extended_forecast_weather_heavy_drizzle);
                break;
            case 310: //light intensity drizzle rain
                weatherDescription = context.getResources().getString(R.string.extended_forecast_weather_light_drizzle_rain);
                break;
            case 312: //heavy intensity drizzle rain
                weatherDescription = context.getResources().getString(R.string.extended_forecast_weather_heavy_drizzle_rain);
                break;
            case 313: //shower rain and drizzle
            case 321: //shower drizzle
                weatherDescription = context.getResources().getString(R.string.extended_forecast_weather_showers_and_drizzle);
                break;
            case 314: //heavy shower rain and drizzle
                weatherDescription = context.getResources().getString(R.string.extended_forecast_weather_heavy_showers_and_drizzle);
                break;
            case 502: //heavy intensity rain
                weatherDescription = context.getResources().getString(R.string.extended_forecast_weather_heavy_rain);
                break;
            case 520: //light intensity shower rain
                weatherDescription = context.getResources().getString(R.string.extended_forecast_weather_light_showers);
                break;
            case 521: //shower rain
                weatherDescription = context.getResources().getString(R.string.extended_forecast_weather_rain_showers);
                break;
            case 522: //heavy intensity shower rain
                weatherDescription = context.getResources().getString(R.string.extended_forecast_weather_heavy_showers);
                break;
            case 531: //ragged shower rain
                weatherDescription = context.getResources().getString(R.string.extended_forecast_weather_ragged_showers);
                break;
            case 612: //shower sleet
                weatherDescription = context.getResources().getString(R.string.extended_forecast_weather_sleet_showers);
                break;
            case 620: //light shower snow
                weatherDescription = context.getResources().getString(R.string.extended_forecast_weather_light_snow_showers);
                break;
            case 621: //shower snow
                weatherDescription = context.getResources().getString(R.string.extended_forecast_weather_snow_showers);
                break;
            case 622: //heavy shower snow
                weatherDescription = context.getResources().getString(R.string.extended_forecast_weather_heavy_snow_showers);
                break;
            case 800:
                weatherDescription = "Clear";
                break;
        }
        weatherDescription = weatherDescription.substring(0,1).toUpperCase() + weatherDescription.substring(1); //Capitalize the first character
        return weatherDescription;
    }

    /**
     * Called when creating a weather entry object. Returns the id of a drawable that represents
     * the icon of the weather type that is passed into the method.
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
