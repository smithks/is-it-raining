package com.isitraining.keegansmith.is_it_pouring_refactor.model;

import com.isitraining.keegansmith.is_it_pouring_refactor.network.ForecastRepository;
import com.isitraining.keegansmith.is_it_pouring_refactor.network.ForecastSubscriber;
import com.isitraining.keegansmith.is_it_pouring_refactor.network.Location;
import com.isitraining.keegansmith.is_it_pouring_refactor.network.model.ForecastAll;
import com.isitraining.keegansmith.is_it_pouring_refactor.network.model.ForecastInstant;
import com.isitraining.keegansmith.is_it_pouring_refactor.util.WeatherData;
import com.isitraining.keegansmith.is_it_pouring_refactor.util.WeatherEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Processes the forecast information retrieved from the repository and passes that information to the viewmodel.
 * Created by keegansmith on 7/10/17.
 */

public class ForecastModel implements ForecastSubscriber {

    private ForecastRepository repository;
    private ForecastViewModel viewModel;
    private Location location;

    public ForecastModel(Location location, ForecastViewModel viewModel) {
        this.location = location;
        this.viewModel = viewModel;
        repository = new ForecastRepository(location);
        repository.subscribe(this);
    }

    /**
     * Called by the ForecastRepository when new forecast data is retrieved.
     *
     * @param forecast the new forecast data to present
     */
    @Override
    public void onForecastUpdated(ForecastAll forecast) {
        List<WeatherEntry> entries = translateForecastData(forecast);
        WeatherData allData = createWeatherData(entries);
        viewModel.setWeatherData(allData);
    }

    /**
     * Parses the forecast data from the source format of POJOs to a list of WeatherEntry objects.
     */
    private List<WeatherEntry> translateForecastData(ForecastAll forecastAll) {
        //Openweathermap sometimes returns older data, find the record closest to the current time to display
        List<ForecastInstant> source = forecastAll.getList();
        List<WeatherEntry> destination = new ArrayList<>();

        String location = forecastAll.getCity().getName() + ", " + forecastAll.getCity().getCountry();

        for (ForecastInstant instant : source) {
            long instantDateTime = instant.getDt(); //Get the UTC timestamp for this block

            //Format the date as the hour followed by am/pm marker. ex "5 PM"
            Date dateObject = new Date(instantDateTime * 1000); //Convert from seconds to milliseconds
            Calendar calendarObject = Calendar.getInstance();
            calendarObject.setTime(dateObject);
            String shortDate = new SimpleDateFormat("EEE, MMMM d", Locale.getDefault()).format(dateObject);
            String shortTime = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(dateObject);

            double temperature = instant.getMain().getTemp(); //Get temperature from "main" object

            //Get the weather id from the weather json array titled "weather" that contains a single object
            int weatherID = instant.getWeather().get(0).getId();
            String weatherDescription = instant.getWeather().get(0).getDescription();
            String weatherIcon = instant.getWeather().get(0).getIcon();

            //Create a weather entry and pass in the collected values.
            WeatherEntry newEntry = new WeatherEntry();
            newEntry.setLocation(location);
            newEntry.setDateObject(dateObject);
            newEntry.setDateShort(shortDate);
            newEntry.setIsToday(Calendar.getInstance().get(Calendar.DATE) == calendarObject.get(Calendar.DATE)); //True if this weather information is from today
            newEntry.setWeatherTime(shortTime);
            newEntry.setTemperature(temperature);
            newEntry.setWeatherCode(weatherID);
            newEntry.setWeatherIconIdentifier(weatherIcon);
            newEntry.setWeatherDescriptionRaw(weatherDescription);

            destination.add(newEntry);
        }
        return destination;
    }

    /**
     * Uses the transformed WeatherEntry list to create the weather data object which is composed
     * of a WeatherEntry object representing today and the 12 following WeatherEntry objects
     * for an extened forecast.
     * @param entries the weather entries to pull from
     * @return a WeatherData object composed of all relevant forecast information
     */
    private WeatherData createWeatherData(List<WeatherEntry> entries){
        long rightNowEpoch = Calendar.getInstance().getTime().getTime();
        boolean currentEntryFound = false;
        int currentIndex = 0;

        //Find the weather entry closest to the current time to use as the current weather.
        WeatherEntry previous = null;
        WeatherEntry following = null;
        WeatherEntry current = null;
        for (int i = 0; i < entries.size() && !currentEntryFound; i++){
            currentIndex = i;
            long instantEpoch = entries.get(i).getDateObject().getTime();
            if (rightNowEpoch - instantEpoch > 0){ //Set this as the previous entry if it falls before the current time
                previous = entries.get(i);
            }else{ //Otherwise set this as the following entry.
                following = entries.get(i);
            }

            if (previous != null && following != null){ //We found surrounding instants, find the one closest to right now.
                if (following.getDateObject().getTime() - rightNowEpoch < rightNowEpoch - previous.getDateObject().getTime()){
                    current = following;
                }else {
                    current = previous;
                    //Decrease the currentIndex by one since we will use the previous entry as the current entry;
                    currentIndex = currentIndex -1;
                }
            }else if (following != null){ //We found an instant after the current time but no previous, use it as the current.
                current = following;
            }

            if (current != null){
                currentEntryFound = true;
            }
        }

        //Grab the 12 weather entries following the entry corresponding to the current time to display in the
        //extended forecast view.
        List<WeatherEntry> extendedEntries = new ArrayList<>();
        boolean endOfList = false;
        for (int i = currentIndex+1; i <= currentIndex+12 && !endOfList; i++){
            if (i < entries.size()){
                extendedEntries.add(entries.get(i));
            }else{
                endOfList = true;
            }
        }

        return new WeatherData(current,extendedEntries);
    }
}
