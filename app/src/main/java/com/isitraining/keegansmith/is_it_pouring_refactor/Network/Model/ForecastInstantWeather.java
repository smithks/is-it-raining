package com.isitraining.keegansmith.is_it_pouring_refactor.Network.Model;

/**
 * Created by keegansmith on 7/7/17.
 */

public class ForecastInstantWeather {
    public int id;
    public String main;
    public String description;
    public String icon;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
