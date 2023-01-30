package com.github.inzan123;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.event.GameEvent;

import static java.lang.Math.pow;

public interface SimulateTimePassing {

    default double getChoose(long successes, long draws) {
        double choose = 1;
        for (int j = 0; j < successes; j++) {
            choose *= (draws - j)/(j+1);
        }
        return choose;
    }
    default double getGrowthOdds(ServerWorld world, BlockPos pos) {
        return 0;
    }
    default void simulateTime(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {
    }

    default int getOccurrences(long cycles, double odds, int maxOccurrences,  Random random) {

        double choose = 1;

        double invertedOdds = 1-odds;

        double totalProbability = 0;

        double randomFloat = random.nextDouble();

        for (int i = 0; i<maxOccurrences;i++) {

            if (i != 0) {
                choose *= (cycles - i - 1)/(i);
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
