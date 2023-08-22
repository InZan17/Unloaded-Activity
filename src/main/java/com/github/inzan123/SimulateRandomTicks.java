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
}
