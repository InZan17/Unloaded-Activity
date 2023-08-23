package com.github.inzan123;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;

import static java.lang.Math.floorMod;

public interface SimulateChunkBlocks {
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
    default boolean implementsSimulateRandTicks() {
        return false;
    }
    default boolean canSimulateRandTicks(BlockState state, ServerWorld world, BlockPos pos) {
        return this.implementsSimulateRandTicks();
    }
    default void simulateRandTicks(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {}

    default boolean implementsSimulatePrecTicks() {
        return false;
    }
    default boolean canSimulatePrecTicks(BlockState state, ServerWorld world, BlockPos pos, Biome.Precipitation precipitation) {
        return this.implementsSimulateRandTicks();
    }
    default void simulatePrecTicks(BlockState state, ServerWorld world, BlockPos pos, long[] weatherTimes, long timePassed, Biome.Precipitation precipitation) {}
}
