package com.isitraining.keegansmith.is_it_pouring_refactor.util;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Helper class to hold information about an entry returned from a weather query.
 * A WeatherEntry object is created for each block of JSON weather data returned in the
 * ForecastLoader. WeatherEntry objects are also created from the saved JSON data when loading existing
 * forecast information from a file in TodayForecastFragment.
 * @author Keegan Smith
 * @since 11/23/2016
 */

public class WeatherEntry implements Parcelable {

    private String location;
    private Date dateObject;
    private int weatherCode;
    private double temperature;
    private String dateShort; // ex. Wed, January 25
    private String weatherTime; //ex. 4:00 pm
    private boolean isToday;
    private String weatherDescriptionRaw;
    private String weatherDescriptionFriendly;
    private String weatherIconIdentifier;
    private int weatherIcon;

    protected WeatherEntry(Parcel in) {
        weatherCode = in.readInt();
        temperature = in.readDouble();
        dateShort = in.readString();
        weatherTime = in.readString();
        isToday = in.readInt() == 1;
        weatherDescriptionRaw = in.readString();
        weatherDescriptionFriendly = in.readString();
        weatherIconIdentifier = in.readString();
        weatherIcon = in.readInt();
    }

    public WeatherEntry() {

    }

    public static final Creator<WeatherEntry> CREATOR = new Creator<WeatherEntry>() {
        @Override
        public WeatherEntry createFromParcel(Parcel in) {
            return new WeatherEntry(in);
        }

        @Override
        public WeatherEntry[] newArray(int size) {
            return new WeatherEntry[size];
        }
    };

    public boolean isToday() {
        return isToday;
    }

    public void setIsToday(boolean today) {
        isToday = today;
    }

    public String getWeatherTime() {
        return weatherTime;
    }

    public void setWeatherTime(String weatherTime) {
        this.weatherTime = weatherTime;
    }

    public String getWeatherIconIdentifier() {
        return weatherIconIdentifier;
    }

    public void setWeatherIconIdentifier(String weatherIconIdentifier) {
        this.weatherIconIdentifier = weatherIconIdentifier;
    }

    public int getWeatherIcon(){
        return weatherIcon;
    }

    public void setWeatherIcon(int icon){
        weatherIcon = icon;
    }

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWeatherDescriptionRaw() {
        return weatherDescriptionRaw;
    }

    public void setWeatherDescriptionRaw(String weatherDescriptionRaw) {
        this.weatherDescriptionRaw = weatherDescriptionRaw;
    }

    public String getWeatherDescriptionFriendly(){
        return weatherDescriptionFriendly;
    }

    public void setWeatherDescriptionFriendly(String descriptionFriendly){
        weatherDescriptionFriendly = descriptionFriendly;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(weatherCode);
        parcel.writeDouble(temperature);
        parcel.writeString(dateShort);
        parcel.writeString(weatherTime);
        parcel.writeInt(isToday? 1: 0);
        parcel.writeString(weatherDescriptionRaw);
        parcel.writeString(weatherDescriptionFriendly);
        parcel.writeString(weatherIconIdentifier);
        parcel.writeInt(weatherIcon);
    }
}
