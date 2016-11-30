package com.smithkeegan.isitraining;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.smithkeegan.isitraining.TodayForecastActivity.LOG_TAG;

/**
 * Loader Class that pulls forecast information from opeaweathermap.org
 */
public class ForecastLoader extends AsyncTaskLoader<List<WeatherEntry>> {

    public ForecastLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * Performs the background process of querying openweathermap.org for forecast information. Formats the returned json and returns this
     * formatted data as the result.
     * @return formatted forecast data returned from openweathermap.org
     */
    @Override
    public List<WeatherEntry> loadInBackground() {
        ArrayList<WeatherEntry> result = null;
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        Boolean useLocationService = false;

        //Values for url parameters
        String location ="28604";
        String format = "json";
        String units = "imperial";
        String appID = "d048a247a1abec98e1fb96785f3ef9cf";

        //Use saved device location if the user has opted to
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (preferences.getBoolean(getContext().getResources().getString(R.string.use_device_location),false)){
            useLocationService = true;
            location = "?"+preferences.getString(getContext().getResources().getString(R.string.user_device_location_lat_long),"");
        }

        try{
            final String URL_BASE = "http://api.openweathermap.org/data/2.5/forecast/";
            final String LOCATION_PARAM = "q";
            final String MODE_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String APPID_PARAM = "appid";

            //Build uri
            Uri uri = Uri.parse(URL_BASE);

            if (useLocationService){ //Use encoded location string if using phone location
                uri = uri.buildUpon().appendEncodedPath(location).build();
            }else { //Otherwise use location from settings
                uri = uri.buildUpon().appendQueryParameter(LOCATION_PARAM,location).build();
            }

            uri = uri.buildUpon()
                    .appendQueryParameter(MODE_PARAM,format)
                    .appendQueryParameter(UNITS_PARAM,units)
                    .appendQueryParameter(APPID_PARAM,appID).build();

            URL url = new URL(uri.toString());
            Log.v(ForecastLoader.class.getSimpleName(),uri.toString());

            //Begin connection
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            InputStream inputStream = connection.getInputStream();
            StringBuilder builder = new StringBuilder();

            if (inputStream == null){
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line).append("/n");
            }

            if (builder.length() == 0){
                return null;
            }

            //Parse the raw json data
            result = getWeatherDataFromJson(builder.toString());

        }catch (IOException | JSONException exception){

            Log.e(ForecastLoader.class.getSimpleName(),exception.getMessage());
        }
        finally { //Cleanup open objects.
            if (connection != null){
                connection.disconnect();
            }
            if (reader!=null){
                try {
                    reader.close();
                }catch (final IOException e){
                    Log.e(LOG_TAG,"Error Closing stream",e);
                }
            }
        }
        return result;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed.
     */
    private ArrayList<WeatherEntry> getWeatherDataFromJson(String forecastJsonStr)
            throws JSONException {
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
            shortDate = new SimpleDateFormat("h a",Locale.getDefault()).format(dateObject);

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

    /**
     * Sends the resulting data to the caller.
     * @param data the result of the opeanweathermap query
     */
    @Override
    public void deliverResult(List<WeatherEntry> data) {
        super.deliverResult(data);
    }
}
