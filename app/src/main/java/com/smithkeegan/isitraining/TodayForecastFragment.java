package com.smithkeegan.isitraining;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment class that populates today's forecast and handles the results returned
 * from the async loader.
 * //TODO request user location
 * //TODO settings for user to set location
 * //TODO popup screen with more weather information
 * //TODO widget?
 * //TODO store connection result, only query if result is one hour? old
 * //TODO more strings for more weather accuracy
 * //TODO Multiple weather sources?
 * @author Keegan Smith
 * @since 11/21/2016
 */

public class TodayForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<WeatherEntry>> {

    private ProgressBar mProgressBar;
    private TextView mWeatherCurrentText;

    /**
     * Inflates the view for this fragment and handles initialization.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.today_forecast_fragment,container,false);
        setMemberViews(rootView);
        showLayout(mProgressBar);
        getLoaderManager().initLoader(0,null,this);
        return rootView;
    }

    /**
     * Refreshes the loader on resume.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (getLoaderManager().getLoader(0) != null) {
         //   getLoaderManager().getLoader(0).onContentChanged();
         //   showLayout(mProgressBar);
        }
    }

    /**
     * Sets the passed in view to VISIBLE while hiding other views.
     * @param view the view to show
     */
    private void showLayout(View view){
        mProgressBar.setVisibility(View.INVISIBLE);
        mWeatherCurrentText.setVisibility(View.INVISIBLE);

        view.setVisibility(View.VISIBLE);
    }

    /**
     * Initialize member views of this fragment.
     * @param rootView parent view of this fragment
     */
    private void setMemberViews(View rootView){
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.today_forecast_progress_bar);
        mWeatherCurrentText = (TextView) rootView.findViewById(R.id.today_forecast_weather_today_text);
    }


    /**
     * Callback that is hit when the forecast loader delivers its results.
     * @param loader the loader that did the work
     * @param data the result from the loader
     */
    @Override
    public void onLoadFinished(Loader<List<WeatherEntry>> loader, List<WeatherEntry> data) {
        if (data == null){ //Error loading data, show error message
            mWeatherCurrentText.setText(getResources().getString(R.string.today_forecast_current_error_loading));
            showLayout(mWeatherCurrentText);
            return;
        }

        //Openweathermap sometimes returns older data, find the record closest to the current time to display
        long rightNowEpoch = Calendar.getInstance().getTime().getTime();
        boolean currentWeatherFound = false,nextWeatherFound = false;
        int weatherIndex = 0, updateWeatherIndex = 0;

        //Find the entry that is closest to the current time to use as the current weather forecast
        WeatherEntry previous = null, following = null, targetEntry = null;
        while (weatherIndex < data.size() && !currentWeatherFound){
            WeatherEntry currentEntry = data.get(weatherIndex);
            long entryEpoch = currentEntry.getDateObject().getTime();

            if (rightNowEpoch - entryEpoch > 0){ //This is a historical weather entry
                previous = currentEntry;
            }else if (rightNowEpoch - entryEpoch < 0){ //This is a future weather entry
                following = currentEntry;
            }else{ //This entry is for this exact moment (very rare)
                targetEntry = currentEntry;
                updateWeatherIndex = weatherIndex + 1;
            }

            if (previous != null && following != null){ //Found surrounding entries, find the closest.
                long previousEpoch = previous.getDateObject().getTime();
                long followingEpoch = following.getDateObject().getTime();
                if (Math.abs(rightNowEpoch - previousEpoch) < Math.abs(rightNowEpoch - followingEpoch)){ //Past entry is closer
                    targetEntry = previous;
                    updateWeatherIndex = weatherIndex;
                }else{ //Future entry is closer
                    targetEntry = following;
                    updateWeatherIndex = weatherIndex +1;
                }
            }else if (previous == null && following != null){ //Only future weather found, use that entry
                targetEntry = following;
                updateWeatherIndex = weatherIndex +1;
            } // Only other situations are target entry already found or only past found and need a future

            //If we found a target entry
            if (targetEntry != null){
                currentWeatherFound = true;
            }

            weatherIndex++;
        }

        if (targetEntry == null){ //No appropriate entry was found display error.
            mWeatherCurrentText.setText(getResources().getString(R.string.today_forecast_current_error_loading));
            showLayout(mWeatherCurrentText);
        }else { //Appropriate entry found, display it and the updated weather
            Date targetDate = targetEntry.getDateObject();
            String currentWeatherDate = new SimpleDateFormat("h a",Locale.getDefault()).format(targetDate);
            String currentWeatherDateLong = new SimpleDateFormat("EEE MMM d 'at' h:mm a",Locale.getDefault()).format(targetDate);
            String timeOfCalculation = new SimpleDateFormat("EEE MMM d 'at' h:mm a",Locale.getDefault()).format(new Date(rightNowEpoch));
            String currentWeather = getCurrentWeatherString(targetEntry.getWeatherCode());

            String weatherText = currentWeather +"\n Weather Code: "+targetEntry.getWeatherCode()+"\n"+targetEntry.weatherMain+"\n"+targetEntry.weatherDescription+ " \n Weather data as of "+currentWeatherDateLong+" \n Calculated "+timeOfCalculation;
            mWeatherCurrentText.setText(weatherText);


            //TODO Parse through remaining data to see if there is a weather change the user would want to know about
            //while (updateWeatherIndex < data.size() || !nextWeatherFound){

            //}
            showLayout(mWeatherCurrentText); //Hide loading layout
        }


    }

    @Override
    public Loader<List<WeatherEntry>> onCreateLoader(int id, Bundle args) {
        showLayout(mProgressBar);
        return new ForecastLoader(getActivity());
    }

    @Override
    public void onLoaderReset(Loader<List<WeatherEntry>> loader) {
    }

    /**
     * Returns a string to display to the user based on the weather code that is passed in.
     * @param weatherCode the weather code to check
     * @return a user facing string representing the current weather
     */
    private String getCurrentWeatherString(int weatherCode){
        String result = null;
        if (weatherCode >= 200 && weatherCode <= 232){ //Thunderstorm
            result = getResources().getString(R.string.today_forecast_current_thunderstorm);
        } else if (weatherCode >= 300 && weatherCode <= 321){ //Drizzle
            result = getResources().getString(R.string.today_forecast_current_drizzle);
        } else if (weatherCode >= 500 && weatherCode <= 531){ //Rain
            result = getResources().getString(R.string.today_forecast_current_raining);
        } else if (weatherCode >= 600 && weatherCode <= 622){ //Snow
            result = getResources().getString(R.string.today_forecast_current_snow);
        } else{
            result = getResources().getString(R.string.today_forecast_current_no_precipitation);
        }

        return result;
    }

}
