package com.github.inzan123;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

import java.util.ArrayList;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class WeatherInfoComponent implements WeatherInfoInterface {
    final int maxWeatherHistory = 3;
    private ArrayList<Long> weatherList = new ArrayList<>();
    @Override
    public void readFromNbt(NbtCompound tag) {
        long[] longArray = tag.getLongArray("weather-list");
        ArrayList<Long> longArrayList = new ArrayList<>();

        for (long value : longArray) {
            longArrayList.add(value);
        }

        this.weatherList = longArrayList;
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putLongArray("weather-list", this.weatherList);
    }

    @Override
    public ArrayList<Long> getValues() {
        return this.weatherList;
    }

    public boolean shouldCheckForRain() {
        return this.weatherList.size() % 2 == 0;
    }

    @Override
    public void updateValues(World world) {
        long currentTime = world.getTimeOfDay();

        //if player decides to go back in time, this will try to reduce any weird behaviour.
        while (this.weatherList.size() > 0) {
            if (this.weatherList.get(0) > currentTime) {
                this.weatherList.remove(0);
                continue;
            }
            break;
        }

        boolean checkForRain = this.shouldCheckForRain();

        if (world.isRaining() && checkForRain) {
            this.weatherList.add(0, currentTime);
        } else if (!world.isRaining() && !checkForRain) {
            this.weatherList.add(0, currentTime);
        }

        int weatherListSize = this.weatherList.size();

        if (weatherListSize > max(maxWeatherHistory, 1)*2) {
            this.weatherList.remove(weatherListSize-1);
            this.weatherList.remove(weatherListSize-2);
        }
    }

    @Override
    public long getTimeInWeather(long timePassed, long currentTime) {
        int time = 0;

        long lastTicked = currentTime-timePassed;

        for (int i=0;i<min(max(maxWeatherHistory,1), (this.weatherList.size()+1)/2);i++) {
            int indexOffset = shouldCheckForRain() ? 0 : 1;
            long weatherStart = this.weatherList.get(i*2-indexOffset+1);
            long weatherEnd = (i-indexOffset) < 0 ? currentTime : this.weatherList.get(i*2-indexOffset);

            long difference = weatherEnd-weatherStart;

            long newWeatherStart = max(weatherStart,lastTicked);
            long newWeatherEnd = min(weatherEnd,currentTime);

            long newDifference = newWeatherEnd-newWeatherStart;

            time+=max(newDifference,0);

            if (newDifference != difference) {
                break;
            }
        }
        return time;
    }
}
