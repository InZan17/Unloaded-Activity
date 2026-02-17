package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.OccurrencesAndDuration;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import lol.zanspace.unloadedactivity.datapack.SimulateProperty;
import lol.zanspace.unloadedactivity.datapack.SimulationData;
import lol.zanspace.unloadedactivity.datapack.SimulationType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LeavesBlock.class)
public abstract class LeavesMixin extends Block{

    public LeavesMixin(Properties properties) {
        super(properties);
    }

    @Shadow
    protected boolean decaying(BlockState state) {
        return true;
    }

    @Override
    public boolean isRandTicksFinished(BlockState state, ServerLevel level, BlockPos pos, SimulateProperty simulateProperty) {
        if (simulateProperty.simulationType.get() == SimulationType.ACTION && simulateProperty.target.get().equals("decay")) {
            return !decaying(state);
        }
        return super.isRandTicksFinished(state, level, pos, simulateProperty);
    }


    @Override
    public @Nullable Triple<BlockState, OccurrencesAndDuration, BlockPos> simulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulateProperty simulateProperty, RandomSource random, long timePassed, int randomTickSpeed, boolean calculateDuration) {
        if (simulateProperty.simulationType.get() == SimulationType.ACTION && simulateProperty.target.get().equals("decay")) {
            OccurrencesAndDuration result = Utils.getOccurrences(level, state, pos, level.getDayTime(), timePassed, simulateProperty.advanceProbability.get(), 1, randomTickSpeed, calculateDuration, random);

            if (result.occurrences() == 0)
                return Triple.of(state, result, pos);

            dropResources(state, level, pos);
            level.removeBlock(pos, false);
            state = level.getBlockState(pos);

            return Triple.of(state, result, pos);
        }

        return super.simulateRandTicks(state, level, pos, simulateProperty, random, timePassed, randomTickSpeed, calculateDuration);
    }
}
