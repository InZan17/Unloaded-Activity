package lol.zanspace.unloadedactivity.datapack.calculate_value;

import lol.zanspace.unloadedactivity.datapack.CalculateValue;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public record NumberValue(double v) implements CalculateValue {
    @Override
    public double calculateValue(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {
        return v;
    }

    @Override
    public boolean isAffectedByWeather(ServerLevel level, BlockState state, BlockPos pos) {
        return false;
    }

    @Override
    public long getNextOddsSwitchDuration(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {
        return Long.MAX_VALUE;
    }

    @Override
    public CalculateValue replicate() {
        return this;
    }

    @Override
    public void replaceSuper(CalculateValue superValue) {}
}