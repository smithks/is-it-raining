package com.smithkeegan.isitraining;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog that displays additional weather information. Uses a listview populated from an array of
 * WeatherEntry objects passed by the TodayForecastFragment that creates this dialog.
 * @author Keegan Smith
 * @since 12/19/2016
 */
public class ExtendedForecastDialog extends DialogFragment {

    public static final String FORECAST_KEY = "forecast_key";
    public static final String LOCATION_KEY = "location_key";
    private ArrayList<WeatherEntry> mWeatherEntries;
    private ListView mForecastListView;
    private String mLocation;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.extended_forecast_layout,container,false);


        if (savedInstanceState == null){
            Bundle params = getArguments();
            ArrayList<WeatherEntry> entriesArray = params.getParcelableArrayList(FORECAST_KEY);
            mLocation = params.getString(LOCATION_KEY);
            mWeatherEntries = new ArrayList<>();
            //Only use the first 12 entries of the array
            for (int i = 0; i < 6 && (entriesArray.get(i) != null); i++){
                mWeatherEntries.add(entriesArray.get(i));
            }
        }else {
            mWeatherEntries = savedInstanceState.getParcelableArrayList(FORECAST_KEY);
            mLocation = savedInstanceState.getString(LOCATION_KEY);
        }

        mForecastListView = (ListView) rootView.findViewById(R.id.extended_forecast_list_view);
        ((TextView)rootView.findViewById(R.id.extended_forecast_location)).setText(mLocation);

        populateListView();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(FORECAST_KEY,mWeatherEntries);
        outState.putString(LOCATION_KEY,mLocation);
    }

    /**
     * Close
     */
    @Override
    public void onPause() {
        if (getDialog() != null) {
            getDialog().dismiss();
        }
        super.onPause();
    }

    /**
     * Assigns the weather entry data to the listview for display.
     */
    private void populateListView(){
        ForecastArrayAdapter arrayAdapter = new ForecastArrayAdapter(getContext(),R.layout.extended_forecast_item_layout,mWeatherEntries);
        mForecastListView.setAdapter(arrayAdapter);
    }

    /**
     * Array Adapter class used to connect an array of weather objects with a listview.
     */
    private class ForecastArrayAdapter extends ArrayAdapter<WeatherEntry>{

        private ArrayList<WeatherEntry> mInnerWeatherEntries;

        public ForecastArrayAdapter(Context context, int resource, List<WeatherEntry> objects) {
            super(context, resource, objects);
            mInnerWeatherEntries = new ArrayList<>();
            mInnerWeatherEntries.addAll(objects);
        }


        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                convertView = getActivity().getLayoutInflater().inflate(R.layout.extended_forecast_item_layout,null);
            }

            WeatherEntry entry = mInnerWeatherEntries.get(position);

            if (entry.isToday()){
                convertView.findViewById(R.id.extended_forecast_today_text).setVisibility(View.VISIBLE);
            }else {
                convertView.findViewById(R.id.extended_forecast_today_text).setVisibility(View.GONE);
            }

            ((TextView)convertView.findViewById(R.id.extended_forecast_item_date)).setText(entry.getDateShort());
            ((TextView)convertView.findViewById(R.id.extended_forecast_item_time)).setText(entry.getWeatherTime());

            String units = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getResources().getString(R.string.settings_temperature_units_key),getResources().getString(R.string.settings_temperature_units_default));
            int unitTemp = convertKelvin(entry.getTemperature(),units);
            String tempStr = unitTemp + (units.equals("imperial")?"\u2109":"\u2103"); //Use appropriate unit label
            ((TextView)convertView.findViewById(R.id.extended_forecast_item_temperature)).setText(tempStr);
            ((TextView)convertView.findViewById(R.id.extended_forecast_item_weather)).setText(entry.getWeatherDescription());
            ((ImageView)convertView.findViewById(R.id.extended_forecast_item_weather_icon)).setImageDrawable(ContextCompat.getDrawable(getContext(),entry.getWeatherIcon()));
            return convertView;
        }
    }

    /**
     * Converts the given temp in Kelvin to the specified new temperature unit.
     * @param temp temp to convert
     * @param newUnit new temperature  unit
     * @return converted temperature
     */
    private int convertKelvin(double temp, String newUnit){
        int newTemp;
        if (newUnit.equals("metric")){
            newTemp =(int)(temp - 273.15); //Convert Kelvin to celsius
        }else{
            newTemp = (int)(temp * 9 / 5 - 459.67); //Convert Kelvin to fahrenheit
        }
        return newTemp;
    }
}
