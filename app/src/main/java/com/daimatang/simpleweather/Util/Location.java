package com.daimatang.simpleweather.Util;

/**
 * Created by 陈益堂 on 2016/5/11.
 */
public class Location {
    // data
    public String location;
    public WeatherInfoToShow info;
    public HourlyData hourlyData;

    public Location(String location) {
        this.location = location;
        info = null;
    }

    public Location(String location, WeatherInfoToShow info) {
        this.location = location;
        this.info = info;
    }

    public Location(String location, WeatherInfoToShow info, HourlyData hourlyData) {
        this.location = location;
        this.info = info;
        this.hourlyData = hourlyData;
    }

    public static boolean engLocation(String location) {
        return location.replaceAll(" ", "").matches("[a-zA-Z]+");
    }


}

