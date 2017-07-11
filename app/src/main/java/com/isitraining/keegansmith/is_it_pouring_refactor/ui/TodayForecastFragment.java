package com.isitraining.keegansmith.is_it_pouring_refactor.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

import com.isitraining.keegansmith.is_it_pouring_refactor.model.ForecastViewModel;
import com.isitraining.keegansmith.is_it_pouring_refactor.R;
import com.isitraining.keegansmith.is_it_pouring_refactor.util.WeatherData;
import com.isitraining.keegansmith.is_it_pouring_refactor.util.WeatherEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import rx.Subscriber;

/**
 * Fragment class that displays today's forecast. If stored forecast data is out of date on resume then a ForecastLoader is used to fetch weather
 * data in the background. When new weather data is fetched the loader stores it in internal storage and a time stamp stored in shared
 * preferences is used to see if the data is current(<1h old). This fragment also populates the data in the ExtendedForecastDialog and handles its launch.
 * @author Keegan Smith
 * @since 11/21/2016
 */

public class TodayForecastFragment extends Fragment {

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

    ForecastViewModel viewModel;

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
    }

    /**
     * Refreshes the forecast on fragment resume. TODO: refresh user location?
     */
    @Override
    public void onResume() {
        super.onResume();
        bindToViewModel();
    }

    //TODO: remove this block if we aren't going to cache manually
//    /**
//     * Checks the current state of forecast data and refreshes data if needed or pulls the data from
//     * storage.
//     */
//    public void refreshForecast() {
//        //Pull the timestamp of the last load to see if the data is out of date
//        boolean dataOutOfDate = false;
//        long lastForecastTimestamp = PreferenceManager.getDefaultSharedPreferences(getContext()).getLong(getResources().getString(R.string.last_forecast_timestamp), 0);
//        if (lastForecastTimestamp > 0) { //Data present, see if it is current
//            long currentTimestamp = Calendar.getInstance().getTimeInMillis();
//            if (currentTimestamp - lastForecastTimestamp > 3600000) { //If the saved forecast data is over an hour old then refresh the data
//                dataOutOfDate = true;
//            }
//        }
//
//        boolean manualRefreshTriggered = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getResources().getString(R.string.trigger_reload_key),false);
//
//        //Make sure we have internet access
//        NetworkInfo networkStatus = ((ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
//        if (!(networkStatus != null && networkStatus.isConnectedOrConnecting())) { //Display no internet error
//            handleLoadError(true, getResources().getString(R.string.today_forecast_error_no_internet));
//        } else if (dataOutOfDate || lastForecastTimestamp == 0 || manualRefreshTriggered) { //Fetch new data from loader if data is out of date or no saved data was found or a manual refresh was triggered
//            //startForecastLoader();
//        } else if (!mDataLoaded) { //If data is not out of date and this is the first load of this fragment then pull from storage
//            boolean storageLoadSuccess = readForecastFromFile();
//            //If we failed to load from storage then use loader.
//            if (!storageLoadSuccess) {
//                //startForecastLoader();
//            }
//        } //Otherwise data is loaded and it is still current, no change to data needed.
//    }

//    /**
//     * Saves the retrieved JSON str to avoid duplicate url connections.
//     * @param jsonstr the raw json str
//     */
//    private void saveJSONData(String jsonstr){
//        try {
//            FileOutputStream outputStream = getContext().openFileOutput(TodayForecastFragment.FORECAST_FILENAME, Context.MODE_PRIVATE);
//            OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream);
//            Log.v(ForecastLoader.class.getSimpleName(),"Saved JSON: "+jsonstr);
//            streamWriter.write(jsonstr);
//            streamWriter.close();
//            outputStream.close();
//
//            //Save the timestamp for this data
//            Calendar dataTimestamp = Calendar.getInstance();
//            getDefaultSharedPreferences(getContext()).edit().putLong(getContext().getResources().getString(R.string.last_forecast_timestamp), dataTimestamp.getTimeInMillis()).apply();
//        } catch (IOException exception) {
//            Log.e(TodayForecastFragment.class.getSimpleName(), "Error caching data: " + exception.getMessage());
//        }
//    }


//    /**
//     * Populates the forecast from JSON data saved to internal storage.
//     * @return false if the read fails, true if it succeeds
//     */
//    private boolean readForecastFromFile() {
//        try {
//            FileInputStream inputStream = getActivity().openFileInput(FORECAST_FILENAME);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//            String line;
//            StringBuilder savedForecast = new StringBuilder();
//            while ((line = reader.readLine()) != null) {
//                savedForecast.append(line).append("\n");
//            }
//
//            if (savedForecast.length() == 0) { //File was empty, reload from loader
//                return false;
//            }
//
//            Log.v(TodayForecastFragment.class.getSimpleName(), "Retrieved JSON: " + savedForecast);
//
//            ArrayList<WeatherEntry> entries = ForecastJSONParser.getEntriesFromJSON(savedForecast.toString(), getContext());
//            //Set the forecast
//            processWeatherObjects(entries);
//            mDataLoaded = true;
//
//            reader.close();
//            inputStream.close();
//            return true;
//        } catch (IOException exception) { //Error reading from file
//            Log.e(TodayForecastFragment.class.getSimpleName(), exception.getMessage());
//            return false;
//        } catch (JSONException exception) {
//            Log.e(TodayForecastFragment.class.getSimpleName(), "Error parsing JSON: " + exception.getMessage());
//            return false;
//        }
//    }

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
     * Binds this view to the WeatherData observable exposed by the ForecastViewModel class.
     * Called on resume or whenever we want to fetch the current weather data.
     */
    private void bindToViewModel(){
        viewModel = new ForecastViewModel(getActivity().getApplicationContext());
        showLayout(mProgressBar);
        viewModel.getForecastObservable().subscribe(new ForecastSubscriber());
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
                bindToViewModel();
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
            bindToViewModel();
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
     * Subscriber class that listens for changes to the WeatherData subject in the ForecastViewModel.
     */
    private class ForecastSubscriber extends Subscriber<WeatherData>{

        @Override
        public void onCompleted() {
            Log.v(this.getClass().getSimpleName()," Completed");
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(WeatherData weatherData) {
            updateForecastText(weatherData.getCurrent().getWeatherDescriptionFriendly());
            updateExtendedForecast((ArrayList<WeatherEntry>)weatherData.getExtended());
            showLayout(mWeatherLayout);
        }
    }
}
