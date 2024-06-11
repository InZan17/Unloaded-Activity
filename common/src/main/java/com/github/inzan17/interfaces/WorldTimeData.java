package com.github.inzan17.interfaces;

import com.github.inzan17.WorldWeatherData;

public interface WorldTimeData {
    default WorldWeatherData getWeatherData() {
        return null;
    };
}
