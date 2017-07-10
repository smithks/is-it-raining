package com.isitraining.keegansmith.is_it_pouring_refactor.Network.Model;

/**
 * Created by keegansmith on 7/7/17.
 */

public class ForecastLocation {
    public int id;
    public String name;
    public ForecastLocationCoord coord;
    public String country;
    public int population;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ForecastLocationCoord getCoord() {
        return coord;
    }

    public void setCoord(ForecastLocationCoord coord) {
        this.coord = coord;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }
}
