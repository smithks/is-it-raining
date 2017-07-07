package com.isitraining.keegansmith.is_it_pouring_refactor.Network.Model;

import java.util.List;

/**
 * Created by keegansmith on 7/7/17.
 */

public class ForecastAll {
    public int cod;
    public String message;
    public int cnt;
    List<ForecastInstant> list;
    public ForecastLocation city;
}