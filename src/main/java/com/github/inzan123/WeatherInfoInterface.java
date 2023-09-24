package com.github.inzan123;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.world.World;

import java.util.ArrayList;

public interface WeatherInfoInterface extends ComponentV3 {
    ArrayList<Long> getValues();
    void updateValues(World world);
    long getTimeInWeather(long timePassed, long currentTime);

}

