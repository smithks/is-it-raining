package com.smithkeegan.isitraining;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.smithkeegan.isitraining.TodayForecastActivity.LOG_TAG;

/**
 * Loader Class that pulls forecast information from opeaweathermap.org
 */
public class ForecastLoader extends AsyncTaskLoader<List<WeatherEntry>> {

    public ForecastLoader(Context context) {
        super(context);
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
        String location =""; //Pull location from preference if allowing manual setting
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
            connection.setReadTimeout(8000); //Timeout after 8 seconds
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

            Log.v(ForecastLoader.class.getSimpleName(),builder.toString());


            //Parse the raw json data
            result = ForecastJSONParser.getEntriesFromJSON(builder.toString());

            //Cache the JSON data so we do not have to read so often
            saveJSONData(builder.toString());

        } catch (IOException exception){
            Log.e(ForecastLoader.class.getSimpleName(),exception.getMessage() == null?"Connection timeout.":exception.getMessage());
        }
        catch (JSONException | NullPointerException exception){

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

    private void saveJSONData(String jsonstr){
        //Cache this data by saving it to a file and saving data timestamp
        try {
            FileOutputStream outputStream = getContext().openFileOutput(TodayForecastFragment.FORECAST_FILENAME, Context.MODE_PRIVATE);
            OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream);
            streamWriter.write(jsonstr);
            streamWriter.close();
            outputStream.close();

            //Save the timestamp for this data
            Calendar dataTimestamp = Calendar.getInstance();
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putLong(getContext().getResources().getString(R.string.last_forecast_timestamp), dataTimestamp.getTimeInMillis()).apply();
        } catch (IOException exception) {
            Log.e(TodayForecastFragment.class.getSimpleName(), "Error caching data: " + exception.getMessage());
        }
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
