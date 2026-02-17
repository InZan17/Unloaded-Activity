package lol.zanspace.unloadedactivity.datapack.condition;

import lol.zanspace.unloadedactivity.datapack.Comparison;
import lol.zanspace.unloadedactivity.datapack.Condition;
import lol.zanspace.unloadedactivity.datapack.SimulationData;
import lol.zanspace.unloadedactivity.datapack.calculate_value.FetchValue;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

// Condition will not change on its own
public record StaticCondition (FetchValue valueGetter, Comparison comparison, double value) implements Condition {
    @Override
    public boolean isValid(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {
        return comparison.compare(valueGetter.calculateValue(level, state, pos, currentTime, isRaining, isThundering), value);
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public boolean isAffectedByWeather(ServerLevel level, BlockState state, BlockPos pos) {
        return false;
    }

    @Override
    public long getNextConditionSwitchDuration(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {
        return Long.MAX_VALUE;
    }
}