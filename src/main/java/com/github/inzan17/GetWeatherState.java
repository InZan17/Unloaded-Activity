package com.github.inzan17;

public interface GetWeatherState {
    default WorldWeatherData getWeatherData() {
        return null;
    };
}
