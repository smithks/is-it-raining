package com.smithkeegan.isitraining;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Fragment class that displays today's forecast. If stored forecast data is out of date on resume then a ForecastLoader is used to fetch weather
 * data in the background. When new weather data is fetched the loader stores it in internal storage and a time stamp stored in shared
 * preferences is used to see if the data is current(<1h old). This fragment also populates the data in the ExtendedForecastDialog and handles its launch.
 * @author Keegan Smith
 * @since 11/21/2016
 */

public class TodayForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<WeatherEntry>> {

    private ProgressBar mProgressBar;
    private RelativeLayout mErrorLayout;
    private LinearLayout mWeatherLayout;
    private ImageButton mShowMoreButton;
    private ImageButton mSettingsButton;

    public static final String FORECAST_DIALOG_TAG = "forecast_dialog_tag";
    public static final String FORECAST_FILENAME = "forecast_stored";
    private boolean mDataLoaded;
    private int ERROR_COUNT; //Keeps track of the current number of web query erros from this launch

    private Toast mLastToast;

    private String mLocation;

    /**
     * Inflates the view for this fragment and handles initialization.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.today_forecast_fragment, container, false);

        setMemberViews(rootView);
        initializeMemberVariables();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(0, null, this);
    }

    /**
     * Refreshes the forecast on fragment resume.
     */
    @Override
    public void onResume() {
        super.onResume();
        refreshForecast();
    }

    /**
     * Checks the current state of forecast data and refreshes data if needed or pulls the data from
     * storage.
     */
    public void refreshForecast() {
        //Pull the timestamp of the last load to see if the data is out of date
        boolean dataOutOfDate = false;
        long lastForecastTimestamp = PreferenceManager.getDefaultSharedPreferences(getContext()).getLong(getResources().getString(R.string.last_forecast_timestamp), 0);
        if (lastForecastTimestamp > 0) { //Data present, see if it is current
            long currentTimestamp = Calendar.getInstance().getTimeInMillis();
            if (currentTimestamp - lastForecastTimestamp > 3600000) { //If the saved forecast data is over an hour old then refresh the data
                dataOutOfDate = true;
            }
        }

        boolean manualRefreshTriggered = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getResources().getString(R.string.trigger_reload_key),false);

        //Make sure we have internet access
        NetworkInfo networkStatus = ((ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (!(networkStatus != null && networkStatus.isConnectedOrConnecting())) { //Display no internet error
            handleLoadError(true, getResources().getString(R.string.today_forecast_error_no_internet));
        } else if (dataOutOfDate || lastForecastTimestamp == 0 || manualRefreshTriggered) { //Fetch new data from loader if data is out of date or no saved data was found or a manual refresh was triggered
            startForecastLoader();
        } else if (!mDataLoaded) { //If data is not out of date and this is the first load of this fragment then pull from storage
            boolean storageLoadSuccess = readForecastFromFile();
            //If we failed to load from storage then use loader.
            if (!storageLoadSuccess) {
                startForecastLoader();
            }
        } //Otherwise data is loaded and it is still current, no change to data needed.
    }

    /**
     * Kicks off the forecast loader to fetch current forecast data.
     */
    private void startForecastLoader() {
        if (getActivity().getSupportLoaderManager().getLoader(0) != null) {
            if (mProgressBar.getVisibility() != View.VISIBLE) {
                showLayout(mProgressBar);
            }
            //Trigger a reload
            getActivity().getSupportLoaderManager().restartLoader(0, null, this).forceLoad();
        }
    }

    /**
     * Populates the forecast from JSON data saved to internal storage.
     * @return false if the read fails, true if it succeeds
     */
    private boolean readForecastFromFile() {
        try {
            FileInputStream inputStream = getActivity().openFileInput(FORECAST_FILENAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder savedForecast = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                savedForecast.append(line).append("\n");
            }

            if (savedForecast.length() == 0) { //File was empty, reload from loader
                return false;
            }

            Log.v(TodayForecastFragment.class.getSimpleName(), "Retrieved JSON: " + savedForecast);

            ArrayList<WeatherEntry> entries = ForecastJSONParser.getEntriesFromJSON(savedForecast.toString(), getContext());
            //Set the forecast
            processWeatherObjects(entries);
            mDataLoaded = true;

            reader.close();
            inputStream.close();
            return true;
        } catch (IOException exception) { //Error reading from file
            Log.e(TodayForecastFragment.class.getSimpleName(), exception.getMessage());
            return false;
        } catch (JSONException exception) {
            Log.e(TodayForecastFragment.class.getSimpleName(), "Error parsing JSON: " + exception.getMessage());
            return false;
        }
    }

    /**
     * Uses an array of Weather Entry items and updates the current forecast and future forecast information.
     * @param entries the weather entries to use
     */
    private void processWeatherObjects(ArrayList<WeatherEntry> entries) {
        //Openweathermap sometimes returns older data, find the record closest to the current time to display
        long rightNowEpoch = Calendar.getInstance().getTime().getTime();
        boolean currentWeatherFound = false;
        int weatherIndex = 0;

        //Find the entry that is closest to the current time to use as the current weather forecast
        WeatherEntry previous = null, following = null, targetEntry = null;
        while (weatherIndex < entries.size() && !currentWeatherFound) {
            WeatherEntry currentEntry = entries.get(weatherIndex);
            long entryEpoch = currentEntry.getDateObject().getTime();

            if (rightNowEpoch - entryEpoch > 0) { //This is a historical weather entry
                previous = currentEntry;
            } else if (rightNowEpoch - entryEpoch < 0) { //This is a future weather entry
                following = currentEntry;
            } else { //This entry is for this exact moment (should never happen)
                targetEntry = currentEntry;
            }

            if (previous != null && following != null) { //Found surrounding entries, find the closest.
                long previousEpoch = previous.getDateObject().getTime();
                long followingEpoch = following.getDateObject().getTime();
                if (Math.abs(rightNowEpoch - previousEpoch) < Math.abs(rightNowEpoch - followingEpoch)) { //Past entry is closer
                    targetEntry = previous;
                } else { //Future entry is closer
                    targetEntry = following;
                }
            } else if (previous == null && following != null) { //Only future weather found, use that entry
                targetEntry = following;
            } // Only other situations are target entry already found or only past found and need a future

            //If we found a target entry
            if (targetEntry != null) {
                currentWeatherFound = true;
            }

            weatherIndex++;
        }

        if (targetEntry == null) { //No appropriate entry was found display error.
            handleLoadError(false, null);
        } else { //Appropriate entry found, display it and the updated weather
            String currentWeatherCode = getCurrentWeatherString(targetEntry.getWeatherCode());

            mLocation = targetEntry.getLocation();

            updateForecastText(currentWeatherCode);
            updateExtendedForecast(entries);

            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(getResources().getString(R.string.trigger_reload_key),false).apply(); //Clear manual refresh trigger
            mDataLoaded = true;
        }
    }

    /**
     * Updates the ui with new forecast information.
     * @param currentForecast The current forecast information
     */
    private void updateForecastText(String currentForecast) {
        TextView weatherCurrentText = (TextView) mWeatherLayout.findViewById(R.id.today_forecast_weather_today_text);
        weatherCurrentText.setText(currentForecast);

        showLayout(mWeatherLayout);
    }

    /**
     * Updates the touch listener and data that is passed to the extended weather forecast dialog.
     * @param entries the weather entries that are passed to the forecast dialog
     */
    private void updateExtendedForecast(final ArrayList<WeatherEntry> entries){

        mShowMoreButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){ //Button pressed
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){ //Hanldle button pressed animation on older versions
                        view.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.show_more_button_pressed));
                    }
                }else if (motionEvent.getAction() == MotionEvent.ACTION_UP){ //Button released
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){ //Handle button released animation on older version
                        view.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.show_more_button));
                    }
                    if (view.isPressed()) {
                        ExtendedForecastDialog extendedForecast = new ExtendedForecastDialog();
                        Bundle args = new Bundle();
                        args.putParcelableArrayList(ExtendedForecastDialog.FORECAST_KEY, entries);
                        args.putString(ExtendedForecastDialog.LOCATION_KEY,mLocation);
                        extendedForecast.setArguments(args);
                        extendedForecast.show(getActivity().getSupportFragmentManager(), FORECAST_DIALOG_TAG);
                    }
                }
                return false;
            }
        });
    }

    /**
     * Hides all parent views of this fragment and sets the passed in view to visible.
     * @param view the view to show
     */
    private void showLayout(View view) {
        mProgressBar.setVisibility(View.INVISIBLE);
        mWeatherLayout.setVisibility(View.INVISIBLE);
        mErrorLayout.setVisibility(View.INVISIBLE);

        view.setVisibility(View.VISIBLE);
    }

    /**
     * Initialize member views of this fragment.
     * @param rootView parent view of this fragment
     */
    private void setMemberViews(View rootView) {
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.today_forecast_progress_bar);
        mErrorLayout = (RelativeLayout) rootView.findViewById(R.id.today_forecast_error_layout);
        mWeatherLayout = (LinearLayout) rootView.findViewById(R.id.today_forecast_weather_layout);
        mShowMoreButton = (ImageButton) rootView.findViewById(R.id.today_forecast_show_extended_button);
        mSettingsButton = (ImageButton) rootView.findViewById(R.id.today_forecast_settings_button);
    }

    /**
     * Initializes the member variables of this fragment.
     */
    private void initializeMemberVariables() {
        mDataLoaded = false;
        ERROR_COUNT = 0;

        //Set listener for retry button.
        mErrorLayout.findViewById(R.id.today_forecast_retry_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToast(getResources().getString(R.string.today_forecast_current_retrying_toast), Toast.LENGTH_SHORT);
                refreshForecast();
            }
        });

        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(getActivity(),view);
                final MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.settings_menu,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.action_settings:
                                Intent intent = new Intent(getContext(),SettingsActivity.class);
                                startActivity(intent);
                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

    }

    /**
     * Called when an error is encountered while loading and the needed data was not obtained.
     * @param displayLayout true if we want to force the error layout to display
     * @param errorMessage  the string to display for the error
     */
    private void handleLoadError(boolean displayLayout, String errorMessage) {
        ERROR_COUNT++;
        if (ERROR_COUNT > 2 || displayLayout) { //If have tried 3 times without success then display server error text.
            showLayout(mErrorLayout);
            if (errorMessage == null) {
                errorMessage = getResources().getString(R.string.today_forecast_error_no_server_error);
            }
            ((TextView) mErrorLayout.findViewById(R.id.today_forecast_error_text)).setText(errorMessage);
            ERROR_COUNT = 0; //Reset error count
        } else { //Automatically retry if the error was encountered from loader.
            showToast(getResources().getString(R.string.today_forecast_current_error_loading_toast), Toast.LENGTH_LONG);
            startForecastLoader();
        }
    }

    /**
     * Central point to launch toasts. Makes sure we do not spam toasts.
     * @param toastMessage the message to display
     * @param toastLength  the length of the message
     */
    private void showToast(String toastMessage, int toastLength) {
        if ((mLastToast != null && mLastToast.getView().getWindowVisibility() != View.VISIBLE) || mLastToast == null) {
            mLastToast = Toast.makeText(getContext(), toastMessage, toastLength);
            mLastToast.show();
        }
    }


    /**
     * Callback that is hit when the forecast loader delivers its results.
     * @param loader the loader that did the work
     * @param data   the result from the loader
     */
    @Override
    public void onLoadFinished(Loader<ArrayList<WeatherEntry>> loader, ArrayList<WeatherEntry> data) {
        if (data == null) { //Error loading data, show error message
            handleLoadError(false, null);
            return;
        }

        //Update the weather forecast
        processWeatherObjects(data);
    }

    @Override
    public Loader<ArrayList<WeatherEntry>> onCreateLoader(int id, Bundle args) {
        return new ForecastLoader(getActivity());
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<WeatherEntry>> loader) {
    }

    /**
     * Returns a string to display to the user based on the weather code that is passed in.
     * @param weatherCode the weather code to check
     * @return a user facing string representing the current weather
     */
    private String getCurrentWeatherString(int weatherCode) {
        String result;
        if (weatherCode >= 200 && weatherCode <= 232) { //Thunderstorm
            result = getResources().getString(R.string.today_forecast_current_thunderstorm);
        } else if (weatherCode >= 300 && weatherCode <= 321) { //Drizzle
            result = getResources().getString(R.string.today_forecast_current_small_chance_rain);
        } else if (weatherCode == 500) {
            result = getResources().getString(R.string.today_forecast_current_small_chance_rain);
        } else if (weatherCode >= 501 && weatherCode <= 531) { //Rain
            result = getResources().getString(R.string.today_forecast_current_raining);
        } else if (weatherCode >= 600 && weatherCode <= 622) { //Snow
            result = getResources().getString(R.string.today_forecast_current_snow);
        } else {
            result = getResources().getString(R.string.today_forecast_current_no_precipitation);
        }
        return result;
    }
}
