package com.isitraining.keegansmith.is_it_pouring_refactor.network;

import com.isitraining.keegansmith.is_it_pouring_refactor.network.model.ForecastAll;

/**
 * Created by keegansmith on 7/10/17.
 */

public interface ForecastSubscriber {

    void onForecastUpdated(ForecastAll forecast);
}
