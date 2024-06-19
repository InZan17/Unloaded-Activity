package com.github.inzan17;

import net.minecraft.nbt.NbtCompound;

#if MC_VER > MC_1_20_4
import net.minecraft.registry.RegistryWrapper;
#endif
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class WorldWeatherData extends PersistentState {

    final int maxWeatherHistory = 3;
    private ArrayList<Long> weatherList;

    public WorldWeatherData(ArrayList<Long> weatherList) {
        this.weatherList = weatherList;
    }

    public WorldWeatherData() {
        this.weatherList = new ArrayList<>();
    }

    @Override
    #if MC_VER <= MC_1_20_4
    public NbtCompound writeNbt(NbtCompound nbt)
    #else
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
    #endif
    {
        nbt.putLongArray("weather_list", this.weatherList);
        return nbt;
    }

    #if MC_VER <= MC_1_20_4
    public static WorldWeatherData fromNbt(NbtCompound nbt)
    #else
    public static WorldWeatherData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
    #endif
    {
        long[] longArray = nbt.getLongArray("weather_list");
        ArrayList<Long> longArrayList = new ArrayList<>();

        for (long value : longArray) {
            longArrayList.add(value);
        }

        return new WorldWeatherData(longArrayList);
    }

    public ArrayList<Long> getWeatherList() {
        return this.weatherList;
    }

    public boolean shouldCheckForRain() {
        return this.weatherList.size() % 2 == 0;
    }

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
            this.markDirty();
        } else if (!world.isRaining() && !checkForRain) {
            this.weatherList.add(0, currentTime);
            this.markDirty();
        }

        int weatherListSize = this.weatherList.size();

        if (weatherListSize > max(maxWeatherHistory, 1)*2) {
            this.weatherList.remove(weatherListSize-1);
            this.weatherList.remove(weatherListSize-2);
        }
    }

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
