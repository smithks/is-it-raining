package com.isitraining.keegansmith.is_it_pouring_refactor.network;

import com.isitraining.keegansmith.is_it_pouring_refactor.network.model.ForecastAll;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Api for GET request to retrieve forecast information.
 * Created by keegansmith on 7/7/17.
 */

public interface IsItPouringInterfaceAPI {

    //Query params: lat, lon, mode, appid
    @GET("data/2.5/forecast")
    Call<ForecastAll> getForecast(@QueryMap Map<String,String> query);
}
