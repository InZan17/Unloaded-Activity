package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.OccurrencesAndDuration;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import lol.zanspace.unloadedactivity.datapack.SimulationData;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
#if MC_VER >= MC_1_20_4
import net.minecraft.world.level.block.grower.TreeGrower;
#else
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
#endif
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

import static java.lang.Math.*;

@Mixin(SaplingBlock.class)
public abstract class SaplingMixin extends #if MC_VER >= MC_1_21_5 VegetationBlock #else BushBlock #endif {

    protected SaplingMixin(Properties properties) {
        super(properties);
    }

    #if MC_VER >= MC_1_20_4
    @Shadow @Final private TreeGrower treeGrower;
    #else
    @Shadow @Final private AbstractTreeGrower treeGrower;
    #endif

    @Override
    public boolean isRandTicksFinished(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        if (propertyName.equals("@grow_tree")) {
            return false;
        }
        return super.isRandTicksFinished(state, level, pos, simulateProperty, propertyName);
    }

    @Override
    public @Nullable Triple<BlockState, OccurrencesAndDuration, BlockPos> simulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName, RandomSource random, long timePassed, int randomTickSpeed, boolean calculateDuration) {

        if (propertyName.equals("@grow_tree")) {
            if (level.getBrightness(LightLayer.BLOCK, pos.above()) < 9) { // If there isnt enough block lights we will do a calculation on how many ticks the tree could have spent in sunlight.
                int stopGrowTime = 13027; //stops growing at 12739 ticks when raining, 13027 when no rain
                int startGrowTime = 22974; //starts growing at 23267 ticks when raining, 22974 when no rain

                timePassed = Utils.getTicksSinceTime(level.getDayTime(),timePassed,startGrowTime,stopGrowTime);
            }

            double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
            double randomGrowChance = getOdds(level, state, pos, simulateProperty, propertyName);
            double totalOdds = randomPickChance * randomGrowChance;

            OccurrencesAndDuration result = Utils.getOccurrences(timePassed, totalOdds, 1, false, random);

            if (result.occurrences() == 0)
                return Triple.of(state, result, pos);

            this.treeGrower.growTree(level, level.getChunkSource().getGenerator(), pos, state, random);

            return null;
        }
        return super.simulateRandTicks(state, level, pos, simulateProperty, propertyName, random, timePassed, randomTickSpeed, calculateDuration);
    }
}