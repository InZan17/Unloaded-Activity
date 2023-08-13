package com.github.inzan123;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

import static java.lang.Math.*;
import static java.lang.Math.floorMod;

public interface SimulateRandomTicks {
    default double getChoose(long successes, long draws) {
        double choose = 1;
        for (int j = 0; j < successes; j++) {
            choose *= (double) (draws - j) /(j+1);
        }
        return choose;
    }
    default double getRandomPickOdds(int randomTickSpeed) {
        return 1.0-pow(1.0 - 1.0 / 4096.0, randomTickSpeed);
    }
    default double getOdds(ServerWorld world, BlockPos pos) {
        return 0;
    }

    default int getCurrentAgeUA(BlockState state) {
        return 0;
    }

    default int getMaxAgeUA() {
        return 0;
    }

    default int getMaxHeightUA() {
        return 0;
    }
    default boolean implementsSimulate() {
        return false;
    }
    default boolean canSimulate(BlockState state, ServerWorld world, BlockPos pos) {
        return this.implementsSimulate();
    }
    default void simulateTime(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {}

    default int getOccurrences(long cycles, double odds, int maxOccurrences,  Random random) {

        if (odds <= 0)
            return 0;

        maxOccurrences = max(maxOccurrences, 0);

        double choose = 1;

        double invertedOdds = 1-odds;

        double totalProbability = 0;

        double randomFloat = random.nextDouble();

        for (int i = 0; i<maxOccurrences;i++) {
            
            if (i == cycles) return i;

            if (i != 0) {
                choose *= (cycles - (i - 1))/i;
            }

            double finalProbability = choose * pow(odds, i) * pow(invertedOdds, cycles-i); //Probability of it happening "i" times

            totalProbability += finalProbability;

            if (randomFloat < totalProbability) {
                return i;
            }
        }
        return maxOccurrences;
    }

    default long getTicksSinceTime(long currentTime, long timePassed, int startTime, int stopTime) {

        long dayLength = 24000;

        long window = floorMod(stopTime-startTime-1, dayLength)+1; //we + and - 1 because we want dayLength to still be dayLength and not 0

        //the amount of ticks we calculated from the amount of days passed.
        long usefulTicks = window * (timePassed / dayLength);

        long previousTime = currentTime-timePassed;

        long currentIncompleteTime = floorMod(currentTime-startTime, dayLength);
        long previousIncompleteTime = floorMod(previousTime-startTime, dayLength);

        //the amount of ticks we calculated from the incomplete day.
        long restOfDayTicks = min(currentIncompleteTime, window) - min(previousIncompleteTime, window);

        if (currentIncompleteTime < previousIncompleteTime)
            restOfDayTicks+=window;

        if (restOfDayTicks < 0)
            restOfDayTicks = floorMod(restOfDayTicks, window);

        return restOfDayTicks + usefulTicks;
    }
}
