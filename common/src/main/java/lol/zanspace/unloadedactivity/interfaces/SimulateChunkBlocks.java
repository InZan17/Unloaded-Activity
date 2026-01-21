package lol.zanspace.unloadedactivity.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

public interface SimulateChunkBlocks {
    default double getOdds(ServerLevel level, BlockPos pos) {
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
    default boolean canSimulateRandTicks(BlockState state, ServerLevel level, BlockPos pos) {
        return this.implementsSimulateRandTicks();
    }
    default void simulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, long timePassed, int randomTickSpeed) {}

    default boolean implementsSimulatePrecTicks() {
        return false;
    }
    default boolean canSimulatePrecTicks(BlockState state, ServerLevel level, BlockPos pos, long timeInWeather, Biome.Precipitation precipitation) {
        return this.implementsSimulatePrecTicks();
    }
    default void simulatePrecTicks(BlockState state, ServerLevel level, BlockPos pos, long timeInWeather, long timePassed, Biome.Precipitation precipitation, double precipitationPickChance) {}
}
