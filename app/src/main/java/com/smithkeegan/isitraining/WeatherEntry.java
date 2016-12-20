package com.smithkeegan.isitraining;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Helper class to hold information about an entry returned from a weather query.
 * @author Keegan Smith
 * @since 11/23/2016
 */

public class WeatherEntry implements Parcelable {

    private Date dateObject;
    private int weatherCode;
    private double temperature;
    private String dateShort;
    public String weatherMain;
    public String weatherDescription;

    protected WeatherEntry(Parcel in) {
        weatherCode = in.readInt();
        temperature = in.readDouble();
        dateShort = in.readString();
        weatherMain = in.readString();
        weatherDescription = in.readString();
    }

    public WeatherEntry(){

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(weatherCode);
        parcel.writeDouble(temperature);
        parcel.writeString(dateShort);
        parcel.writeString(weatherMain);
        parcel.writeString(weatherDescription);
    }
}
