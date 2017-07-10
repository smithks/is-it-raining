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

    public int getCod() {
        return cod;
    }

    public void setCod(int cod) {
        this.cod = cod;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public List<ForecastInstant> getList() {
        return list;
    }

    public void setList(List<ForecastInstant> list) {
        this.list = list;
    }

    public ForecastLocation getCity() {
        return city;
    }

    public void setCity(ForecastLocation city) {
        this.city = city;
    }
}