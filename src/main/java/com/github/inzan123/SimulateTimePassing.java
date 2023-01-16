package com.github.inzan123;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

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
}
