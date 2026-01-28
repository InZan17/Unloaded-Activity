package lol.zanspace.unloadedactivity.mixin;

import lol.zanspace.unloadedactivity.OccurrencesAndLeftover;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import lol.zanspace.unloadedactivity.datapack.SimulationData;
import lol.zanspace.unloadedactivity.datapack.SimulationDataResource;
import lol.zanspace.unloadedactivity.interfaces.SimulateChunkBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;

#if MC_VER >= MC_1_21_11
import net.minecraft.resources.Identifier;
#else
import net.minecraft.resources.ResourceLocation;
#endif

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.Optional;

@Mixin(Block.class)
public abstract class BlockMixin implements SimulateChunkBlocks {

    @Shadow @Final private Holder.Reference<Block> builtInRegistryHolder;

    @Override
    public SimulationData getSimulationData() {

        var blockId = this.builtInRegistryHolder.key()#if MC_VER >= MC_1_21_11 .identifier() #else .location() #endif;

        SimulationData blockSimulationData = SimulationDataResource.BLOCK_MAP.get(blockId);

        if (blockSimulationData != null)
            if (blockSimulationData.isFinal)
                return blockSimulationData;

        SimulationData finalSimulationData = new SimulationData();

        for (Iterator<TagKey<Block>> it = builtInRegistryHolder.tags().iterator(); it.hasNext(); ) {
            TagKey<Block> tag = it.next();
            var tagId = tag.location();

            SimulationData tagSimulationData = SimulationDataResource.TAG_MAP.get(tagId);

            if (tagSimulationData != null) {
                finalSimulationData.absorb(tagSimulationData);
            }
        }

        if (blockSimulationData != null) {
            finalSimulationData.absorb(blockSimulationData);
        }

        SimulationDataResource.BLOCK_MAP.put(blockId, finalSimulationData);

        return finalSimulationData;
    }

    @Override
    public double getOdds(ServerLevel level, BlockState state, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        return simulateProperty.advanceProbability.map(calculateValue -> calculateValue.calculateValue(level, state, pos)).orElse(0.0);
    }

    @Override
    public boolean canSimulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        Optional<Property<?>> maybeProperty = state.getProperties().stream().filter(p -> p.getName().equals(propertyName)).findFirst();

        if (maybeProperty.isPresent()) {
            Property<?> property = maybeProperty.get();

            if (property instanceof IntegerProperty integerProperty) {
                int propertyMax = ((IntegerPropertyAccessor)integerProperty).unloaded_activity$getMax();
                int max = Math.min(propertyMax, simulateProperty.maxValue.orElse(propertyMax));
                int current = state.getValue(integerProperty);

                if (current >= max) {
                    return false;
                }
            }
        }

        for (SimulationData.Condition condition : simulateProperty.conditions) {
            if (!condition.isValid(level, state, pos)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public BlockState simulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName, RandomSource random, long timePassed, int randomTickSpeed, Optional<OccurrencesAndLeftover> returnLeftoverTicks) {
        Optional<Property<?>> maybeProperty = state.getProperties().stream().filter(p -> p.getName().equals(propertyName)).findFirst();

        if (maybeProperty.isEmpty())
            return state;

        Property<?> property = maybeProperty.get();

        if (property instanceof IntegerProperty integerProperty) {
            int propertyMax = ((IntegerPropertyAccessor)integerProperty).unloaded_activity$getMax();
            int max = Math.min(propertyMax, simulateProperty.maxValue.orElse(propertyMax));
            int current = state.getValue(integerProperty);

            int difference = max - current;

            if (difference <= 0)
                return state;

            double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
            double totalOdds = getOdds(level, state, pos, simulateProperty, propertyName) * randomPickChance;

            int addAmount = Utils.getOccurrences(timePassed, totalOdds, difference, random);

            if (addAmount == 0)
                return state;

            state = state.setValue(integerProperty, current + addAmount);
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }

        return state;
    }
}