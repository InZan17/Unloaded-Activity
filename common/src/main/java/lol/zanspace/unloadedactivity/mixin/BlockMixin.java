package lol.zanspace.unloadedactivity.mixin;

import lol.zanspace.unloadedactivity.OccurrencesAndDuration;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import lol.zanspace.unloadedactivity.datapack.SimulateProperty;
import lol.zanspace.unloadedactivity.datapack.SimulationData;
import lol.zanspace.unloadedactivity.datapack.SimulationDataResource;
import lol.zanspace.unloadedactivity.interfaces.SimulateChunkBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;

#if MC_VER >= MC_1_21_11
import net.minecraft.resources.Identifier;
#else
#endif

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.Optional;

@Mixin(Block.class)
public abstract class BlockMixin implements SimulateChunkBlocks {

    @Shadow @Final private Holder.Reference<Block> builtInRegistryHolder;

    @Override
    public Optional<Property<?>> getProperty(BlockState state, String propertyName) {
        return state.getProperties().stream().filter(p -> p.getName().equals(propertyName)).findFirst();
    }

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
                finalSimulationData.merge(tagSimulationData);
            }
        }

        if (blockSimulationData != null) {
            finalSimulationData.merge(blockSimulationData);
        }

        for (var entry : finalSimulationData.propertyMap.entrySet()) {
            String fallbackTarget = entry.getKey();
            SimulateProperty simulateProperty = entry.getValue();
            simulateProperty.finalize(fallbackTarget);
            simulateProperty.throwIfInvalid();
        }

        SimulationDataResource.BLOCK_MAP.put(blockId, finalSimulationData);

        return finalSimulationData;
    }
}