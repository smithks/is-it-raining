package com.smithkeegan.isitraining;

import java.util.Date;

/**
 * Helper class to hold information about an entry returned from a weather query.
 * @author Keegan Smith
 * @since 11/23/2016
 */

public class WeatherEntry {

    private Date dateObject;
    private int weatherCode;
    private double temperature;
    private String dateShort;

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(int weatherCode) {
        this.weatherCode = weatherCode;
    }

    public String getDateShort() {
        return dateShort;
    }

    public void setDateShort(String dateShort) {
        this.dateShort = dateShort;
    }

    public Date getDateObject() {
        return dateObject;
    }

    public void setDateObject(Date dateObject) {
        this.dateObject = dateObject;
    }
}
