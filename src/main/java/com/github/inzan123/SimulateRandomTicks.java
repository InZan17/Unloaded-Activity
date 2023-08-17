package com.github.inzan123;

import com.mojang.datafixers.DataFixer;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.storage.StorageIoWorker;

import java.nio.file.Path;

import static java.lang.Math.*;
import static java.lang.Math.floorMod;

public interface SimulateRandomTicks {

    public class OccurrencesAndLeftover {
        public int occurrences;
        public long leftover;
        public OccurrencesAndLeftover(int occurrences, long leftover) {
            this.occurrences = occurrences;
            this.leftover = leftover;
        }
    }
    default double getChoose(long x, long y) {
        double choose = 1;
        for (int i = 0; i < x; i++) {
            choose *= (double) (y - i) / (i+1);
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
        if (UnloadedActivity.instance.config.debugLogs)
            UnloadedActivity.LOGGER.info("Ran getOccurrences. cycles: "+cycles+" odds: "+odds+" maxOccurrences: "+maxOccurrences);
        return getOccurrencesBinomial(cycles, odds, maxOccurrences, random);
    }

    //41ms, 200 chunks
    default int getOccurrencesBinomial(long cycles, double odds, int maxOccurrences,  Random random) {

        if (odds <= 0)
            return 0;

        if (maxOccurrences <= 0)
            return 0;

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

    default long randomRound(double number, Random random) {
        return (long) floor(number+random.nextDouble());
    }

    default OccurrencesAndLeftover getOccurrencesAndLeftoverTicksFast(long cycles, double normalOdds, int randomTickSpeed, int maxOccurrences,  Random random) {
        if (UnloadedActivity.instance.config.debugLogs)
            UnloadedActivity.LOGGER.info("Ran getOccurrencesAndLeftoverTicksFast. cycles: "+cycles+" normalOdds: "+normalOdds+" maxOccurrences: "+maxOccurrences);

        double multiplier = randomTickSpeed/4096.0;

        double newCycles = cycles*multiplier;
        long randRoundCycles = randomRound(newCycles, random);
        OccurrencesAndLeftover oal = getOccurrencesAndLeftoverTicks(randRoundCycles, normalOdds, maxOccurrences, random);
        oal.leftover = (int) (oal.leftover/multiplier);
        return oal;
    }

    //595ms, 200 chunks
    default OccurrencesAndLeftover getOccurrencesAndLeftoverTicks(long cycles, double odds, int maxOccurrences,  Random random) {

        if (odds <= 0)
            return new OccurrencesAndLeftover(0,0);

        if (maxOccurrences <= 0)
            return new OccurrencesAndLeftover(0,cycles);

        int successes = 0;
        long leftover = 0;

        for (int i = 0; i<cycles;i++) {

            if (successes >= maxOccurrences) {
                leftover = cycles-i;
                break;
            }

            if (random.nextDouble() < odds) {
                ++successes;
            }
        }
        return new OccurrencesAndLeftover(successes, leftover);
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
