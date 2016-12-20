package com.smithkeegan.isitraining;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog that displays additional weather information. Uses a listview populated from data passed by the object
 * that creates this dialog.
 * @author Keegan Smith
 * @since 12/19/2016
 */

public class ExtendedForecastDialog extends DialogFragment {

    public static final String FORECAST_KEY = "forecast_key";
    private ArrayList<WeatherEntry> mWeatherEntries;
    private ListView mForecastListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.extended_forecast_layout,container,false);


        if (savedInstanceState == null){
            Bundle params = getArguments();
            ArrayList<WeatherEntry> entriesArray = params.getParcelableArrayList(FORECAST_KEY);
            mWeatherEntries = new ArrayList<>();
            //Only use the first 12 entries of the array
            for (int i = 0; i < 6 && (entriesArray.get(i) != null); i++){
                mWeatherEntries.add(entriesArray.get(i));
            }
        }else {
            mWeatherEntries = savedInstanceState.getParcelableArrayList(FORECAST_KEY);
        }

        mForecastListView = (ListView) rootView.findViewById(R.id.extended_forecast_list_view);

        populateListView();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(FORECAST_KEY,mWeatherEntries);
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

            ((TextView)convertView.findViewById(R.id.extended_forecast_item_time)).setText(entry.getDateShort());

            String tempStr = (int)entry.getTemperature() + "\u2109";
            ((TextView)convertView.findViewById(R.id.extended_forecast_item_temperature)).setText(tempStr);
            ((TextView)convertView.findViewById(R.id.extended_forecast_item_weather)).setText(entry.weatherMain);

            return convertView;
        }
    }
}
