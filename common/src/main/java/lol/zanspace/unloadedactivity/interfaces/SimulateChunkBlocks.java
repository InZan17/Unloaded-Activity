package lol.zanspace.unloadedactivity.interfaces;

import lol.zanspace.unloadedactivity.OccurrencesAndLeftover;
import lol.zanspace.unloadedactivity.datapack.SimulationData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public interface SimulateChunkBlocks {

    SimulationData getSimulationData();

    default double getOdds(ServerLevel level, BlockState state, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {return 0;};

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
        return !getSimulationData().isEmpty();
    };

    default boolean canSimulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        return true;
    }
    default BlockState simulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName, RandomSource random, long timePassed, int randomTickSpeed, Optional<OccurrencesAndLeftover> returnLeftoverTicks) {return state;}

    default boolean implementsSimulatePrecTicks() {
        return false;
    }
    default boolean canSimulatePrecTicks(BlockState state, ServerLevel level, BlockPos pos, long timeInWeather, Biome.Precipitation precipitation) {
        return this.implementsSimulatePrecTicks();
    }
    default void simulatePrecTicks(BlockState state, ServerLevel level, BlockPos pos, long timeInWeather, long timePassed, Biome.Precipitation precipitation, double precipitationPickChance) {}


}
