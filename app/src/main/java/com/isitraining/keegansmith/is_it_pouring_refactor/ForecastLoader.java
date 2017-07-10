package com.isitraining.keegansmith.is_it_pouring_refactor;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.isitraining.keegansmith.is_it_pouring_refactor.ui.TodayForecastFragment;
import com.isitraining.keegansmith.is_it_pouring_refactor.util.ForecastJSONParser;
import com.isitraining.keegansmith.is_it_pouring_refactor.util.WeatherEntry;

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

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.isitraining.keegansmith.is_it_pouring_refactor.ui.TodayForecastActivity.LOG_TAG;

/**
 * Loader Class that pulls forecast information from opeaweathermap.org.
 * Fetches weather data as raw JSON which is parsed into WeatherEntry objects before being passed back to caller.
 * A copy of the weather data is stored to internal storage to avoid duplicate calls.
 * @author Keegan
 * @since 12/14/16
 * //TODO MARK FOR DELETION
 */
public class ForecastLoader extends AsyncTaskLoader<ArrayList<WeatherEntry>> {

    public ForecastLoader(Context context) {
        super(context);
    }

    /**
     * Performs the background process of querying openweathermap.org for forecast information. Formats the returned json and returns this
     * formatted data as the result.
     * @return formatted forecast data returned from openweathermap.org
     */
    @Override
    public ArrayList<WeatherEntry> loadInBackground() {
        ArrayList<WeatherEntry> result = null;
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        //Values for url parameters
        String format = "json";
        String appID = "d048a247a1abec98e1fb96785f3ef9cf";

        //Use saved device location
        SharedPreferences preferences = getDefaultSharedPreferences(getContext());
        String location = preferences.getString(getContext().getResources().getString(R.string.user_device_location_lat_long),"");
        String[] coords = location.split(" ");

        final String URL_BASE = "http://api.openweathermap.org/data/2.5/forecast";
        final String MODE_PARAM = "mode";
        final String APPID_PARAM = "appid";
        final String LAT_PARAM = "lat";
        final String LON_PARAM = "lon";

        try{
            //Build uri
            Uri uri = Uri.parse(URL_BASE).buildUpon()
                    .appendQueryParameter(LAT_PARAM, coords[0])
                    .appendQueryParameter(LON_PARAM, coords[1])
                    .appendQueryParameter(MODE_PARAM,format)
                    .appendQueryParameter(APPID_PARAM,appID).build();;

            URL url = new URL(uri.toString());
            Log.v(ForecastLoader.class.getSimpleName(),uri.toString());

            //Begin connection
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(8000); //Timeout after 8 seconds
            connection.connect();

            InputStream inputStream = connection.getInputStream();
            StringBuilder builder = new StringBuilder();

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line).append("/n");
            }

            if (builder.length() == 0){ //Handle empty JSON
                return null;
            }

            String rawJSON = builder.toString();
            Log.v(ForecastLoader.class.getSimpleName(),rawJSON);

            //Parse the raw json data
            result = ForecastJSONParser.getEntriesFromJSON(rawJSON, getContext());

            //Cache the JSON data to avoid frequent queries
            saveJSONData(rawJSON);

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

    /**
     * Saves the retrieved JSON str to avoid duplicate url connections.
     * @param jsonstr the raw json str
     */
    private void saveJSONData(String jsonstr){
        try {
            FileOutputStream outputStream = getContext().openFileOutput(TodayForecastFragment.FORECAST_FILENAME, Context.MODE_PRIVATE);
            OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream);
            Log.v(ForecastLoader.class.getSimpleName(),"Saved JSON: "+jsonstr);
            streamWriter.write(jsonstr);
            streamWriter.close();
            outputStream.close();

            //Save the timestamp for this data
            Calendar dataTimestamp = Calendar.getInstance();
            getDefaultSharedPreferences(getContext()).edit().putLong(getContext().getResources().getString(R.string.last_forecast_timestamp), dataTimestamp.getTimeInMillis()).apply();
        } catch (IOException exception) {
            Log.e(TodayForecastFragment.class.getSimpleName(), "Error caching data: " + exception.getMessage());
        }
    }

    /**
     * Sends the resulting data to the caller.
     * @param data the result of the opeanweathermap query
     */
    @Override
    public void deliverResult(ArrayList<WeatherEntry> data) {
        super.deliverResult(data);
    }

}
