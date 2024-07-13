package lol.zanspace.unloadedactivity.interfaces;

import lol.zanspace.unloadedactivity.WorldWeatherData;

public interface WorldTimeData {
    default WorldWeatherData getWeatherData() {
        return null;
    };
}
