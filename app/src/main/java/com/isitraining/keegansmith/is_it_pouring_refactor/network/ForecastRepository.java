package com.isitraining.keegansmith.is_it_pouring_refactor.network;

import com.isitraining.keegansmith.is_it_pouring_refactor.network.model.ForecastAll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Util class to fetch an interface of the network api.
 * Created by keegansmith on 7/10/17.
 */

public class ForecastRepository {

    private IsItPouringInterfaceAPI serviceApi;
    private ForecastAll forecastData;
    private List<ForecastSubscriber> subscribers;
    private Location location;

    public ForecastRepository(Location location){
        serviceApi = IsItPouringClientAPI.getClient().create(IsItPouringInterfaceAPI.class);
        subscribers = new ArrayList<>();
        this.location = location;
        loadData();
    }

    public void subscribe(ForecastSubscriber subscriber){
        if (!subscribers.contains(subscriber)){
            subscribers.add(subscriber);
        }

        if (forecastData != null){
            subscriber.onForecastUpdated(forecastData);
        }
    }

    private void notifySubscribers(){
        for (ForecastSubscriber subscriber : subscribers){
            subscriber.onForecastUpdated(forecastData);
        }
    }

    private void loadData(){
        final String LAT_PARAM = "lat";
        final String LON_PARAM = "lon";
        final String MODE_PARAM = "mode";
        final String APPID_PARAM = "appid";

        String mode = "json";
        String appId = "d048a247a1abec98e1fb96785f3ef9cf";

        Map<String, String> forecastMap = new HashMap<>();
        forecastMap.put(LAT_PARAM,location.getLat());
        forecastMap.put(LON_PARAM,location.getLon());
        forecastMap.put(MODE_PARAM,mode);
        forecastMap.put(APPID_PARAM,appId);

        serviceApi.getForecast(forecastMap).enqueue(new Callback<ForecastAll>() {
            @Override
            public void onResponse(Call<ForecastAll> call, Response<ForecastAll> response) {
                forecastData = response.body();
                notifySubscribers();
            }

            @Override
            public void onFailure(Call<ForecastAll> call, Throwable t) {
                //Log.e(this.getClass().getSimpleName(), t.getMessage());
            }
        });
    }

}
