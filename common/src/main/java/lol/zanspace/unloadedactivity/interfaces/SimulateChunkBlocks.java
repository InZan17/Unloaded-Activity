package lol.zanspace.unloadedactivity.interfaces;

import lol.zanspace.unloadedactivity.OccurrencesAndDuration;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import lol.zanspace.unloadedactivity.datapack.SimulationData;
import lol.zanspace.unloadedactivity.mixin.IntegerPropertyAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface SimulateChunkBlocks {

    SimulationData getSimulationData();

    default Optional<Property<?>> getProperty(BlockState state, String propertyName) {
        return Optional.empty();
    };

    default double getOdds(ServerLevel level, BlockState state, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        return simulateProperty.advanceProbability.map(calculateValue -> calculateValue.calculateValue(level, state, pos)).orElse(0.0);
    };

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
        boolean isFinished = isRandTicksFinished(state, level, pos, simulateProperty, propertyName);
        if (isFinished)
            return false;

        for (SimulationData.Condition condition : simulateProperty.conditions) {
            if (!condition.isValid(level, state, pos)) {
                return false;
            }
        }

        return true;
    }

    default boolean isRandTicksFinished(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        if (simulateProperty.maxHeight.isPresent()) {
            Block thisBlock = state.getBlock();

            BlockState blockStateAbove = level.getBlockState(pos.above());
            boolean emptyAbove = blockStateAbove.isAir();
            boolean blockingAbove = !emptyAbove;
            boolean continuesAbove = blockStateAbove.is(thisBlock);
            boolean stopUpdatingAfterMaxHeight = !simulateProperty.keepUpdatingAfterMaxHeight.orElse(false);
            if (blockingAbove && (stopUpdatingAfterMaxHeight || continuesAbove)) {
                return true;
            }

            int maxHeight = simulateProperty.maxHeight.get();

            int height;
            for(height = 1; level.getBlockState(pos.below(height)).is(thisBlock) && height <= maxHeight; ++height) {}

            if (height < maxHeight) {
                return false;
            }

            if (stopUpdatingAfterMaxHeight) {
                return true;
            }
        }

        Optional<Property<?>> maybeProperty = getProperty(state, propertyName);

        if (maybeProperty.isPresent()) {
            Property<?> property = maybeProperty.get();

            if (property instanceof IntegerProperty integerProperty) {
                int propertyMax = ((IntegerPropertyAccessor)integerProperty).unloaded_activity$getMax();
                int max = Math.min(propertyMax, simulateProperty.maxValue.orElse(propertyMax));
                int current = state.getValue(integerProperty);

                if (current >= max) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        // No property is present. Say this property is finished.
        return true;
    }

    default @Nullable Triple<BlockState, OccurrencesAndDuration, BlockPos> simulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName, RandomSource random, long timePassed, int randomTickSpeed, boolean calculateDuration) {
        Optional<Property<?>> maybeProperty = getProperty(state, propertyName);

        if (maybeProperty.isEmpty())
            return Triple.of(state, OccurrencesAndDuration.empty(), pos);

        Property<?> property = maybeProperty.get();

        if (property instanceof IntegerProperty integerProperty) {
            Block thisBlock = state.getBlock();

            int propertyMax = ((IntegerPropertyAccessor)integerProperty).unloaded_activity$getMax();
            int max = Math.min(propertyMax, simulateProperty.maxValue.orElse(propertyMax));
            int current = state.getValue(integerProperty);

            int updateCount = max - current;

            if (simulateProperty.maxHeight.isPresent()) {
                int maxHeight = simulateProperty.maxHeight.get();


                int height;
                for(height = 1; level.getBlockState(pos.below(height)).is(thisBlock) && height <= maxHeight; ++height) {}

                int heightDifference = maxHeight - height;

                int freeSpaceAbove;
                for(freeSpaceAbove = 1; level.isEmptyBlock(pos.above(freeSpaceAbove)) && freeSpaceAbove <= heightDifference; ++freeSpaceAbove) {}
                --freeSpaceAbove;

                // Updates for growing in height
                updateCount += freeSpaceAbove;

                boolean stopUpdatingAfterMaxHeight = !simulateProperty.keepUpdatingAfterMaxHeight.orElse(false);

                if (stopUpdatingAfterMaxHeight) {
                    updateCount += max * Math.max(freeSpaceAbove - 1, 0);
                } else {
                    updateCount += max * freeSpaceAbove;
                }


            }

            if (updateCount <= 0)
                return Triple.of(state, OccurrencesAndDuration.empty(), pos);

            double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
            double totalOdds = getOdds(level, state, pos, simulateProperty, propertyName) * randomPickChance;

            OccurrencesAndDuration result = Utils.getOccurrences(timePassed, totalOdds, updateCount, calculateDuration, random);

            if (result.occurrences() == 0)
                return Triple.of(state, result, pos);

            int newPropertyValue = current + result.occurrences();

            if (simulateProperty.maxHeight.isPresent()) {
                int growBlocks = newPropertyValue/(max + 1);
                int valueRemainer = newPropertyValue % (max + 1);

                boolean resetOnHeightChange = simulateProperty.resetOnHeightChange.orElse(true);

                int belowValue = resetOnHeightChange ? 0 : max;

                if (growBlocks != 0) {
                    state = state.setValue(integerProperty, belowValue);
                } else {
                    state = state.setValue(integerProperty, valueRemainer);
                }
                level.setBlock(pos, state, simulateProperty.updateType.orElse(Block.UPDATE_ALL));

                for (int i=0;i<growBlocks;i++) {

                    pos = pos.above();

                    if (i+1==growBlocks) {
                        state = thisBlock.defaultBlockState().setValue(integerProperty, valueRemainer);
                    } else {
                        state = thisBlock.defaultBlockState().setValue(integerProperty, belowValue);
                    }

                    level.setBlockAndUpdate(pos, state);
                    boolean updateNeighbors = simulateProperty.updateNeighbors.orElse(false);
                    if (updateNeighbors) {
                        level.neighborChanged(state, pos, thisBlock, pos, false);
                        level.scheduleTick(pos, thisBlock, 1);
                    }
                }
            } else {
                state = state.setValue(integerProperty, newPropertyValue);
                level.setBlock(pos, state, simulateProperty.updateType.orElse(Block.UPDATE_ALL));
            }


            return Triple.of(state, result, pos);
        }

        return Triple.of(state, OccurrencesAndDuration.empty(), pos);
    };

    default boolean implementsSimulatePrecTicks() {
        return false;
    }
    default boolean canSimulatePrecTicks(BlockState state, ServerLevel level, BlockPos pos, long timeInWeather, Biome.Precipitation precipitation) {
        return this.implementsSimulatePrecTicks();
    }
    default void simulatePrecTicks(BlockState state, ServerLevel level, BlockPos pos, long timeInWeather, long timePassed, Biome.Precipitation precipitation, double precipitationPickChance) {}


}
