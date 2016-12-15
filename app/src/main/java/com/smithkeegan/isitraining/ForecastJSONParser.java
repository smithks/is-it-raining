package com.smithkeegan.isitraining;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Helper class that contains JSON parsers.
 * @author Keegan Smith
 * @since 12/14/2016
 */

public class ForecastJSONParser {

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed.
     */
    public static ArrayList<WeatherEntry> getEntriesFromJSON(String forecastJsonStr) throws JSONException{
        ArrayList<WeatherEntry> result = new ArrayList<>();

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_DATE = "dt";
        final String OWM_MAIN = "main";
        final String OWM_WEATHER_ID = "id";
        final String OWM_WEATHER_DESCRIPTION = "description";

        //Create a JSON object from passed in string
        JSONObject forecastJson = new JSONObject(forecastJsonStr);

        //Get array of weather data divided in 3 hour chunks
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        for(int i = 0; i < weatherArray.length(); i++) {
            String shortDate;
            double temperature;
            int weatherID;
            String weatherMain;
            String weatherDescription;

            //Get this JSON object representing a 3 hour block
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            //Get the UTC timestamp for this block
            long dateTime = dayForecast.getLong(OWM_DATE);

            //Format the date as the hour followed by am/pm marker. ex "5 PM"
            Date dateObject = new Date(dateTime * 1000); //Convert from seconds to milliseconds
            shortDate = new SimpleDateFormat("h a", Locale.getDefault()).format(dateObject);

            //Get temperature from "main" object
            temperature = dayForecast.getJSONObject(OWM_MAIN).getDouble(OWM_TEMPERATURE);

            //Get the weather id from the weather json array titled "weather" that contains a single object
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            weatherID = weatherObject.getInt(OWM_WEATHER_ID);
            weatherMain = weatherObject.getString(OWM_MAIN);
            weatherDescription = weatherObject.getString(OWM_WEATHER_DESCRIPTION);

            //Create a weather entry and pass in the collected values.
            WeatherEntry newEntry = new WeatherEntry();
            newEntry.setDateObject(dateObject);
            newEntry.setDateShort(shortDate);
            newEntry.setTemperature(temperature);
            newEntry.setWeatherCode(weatherID);
            newEntry.weatherMain = weatherMain;
            newEntry.weatherDescription = weatherDescription;

            result.add(newEntry);

        }

        return result;
    }
}
