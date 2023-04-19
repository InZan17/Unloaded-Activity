package com.github.inzan123;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

import static java.lang.Math.max;
import static java.lang.Math.pow;

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
    default boolean canSimulate() {
        return false;
    }
    default void simulateTime(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {
    }

    default int getOccurrences(long cycles, double odds, int maxOccurrences,  Random random) {

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
}
