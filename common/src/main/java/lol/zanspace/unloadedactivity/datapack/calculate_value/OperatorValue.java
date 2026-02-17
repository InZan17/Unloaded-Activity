package lol.zanspace.unloadedactivity.datapack.calculate_value;

import lol.zanspace.unloadedactivity.datapack.CalculateValue;
import lol.zanspace.unloadedactivity.datapack.SimulationData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class OperatorValue implements CalculateValue {
    public final Operator operator;
    public CalculateValue value;
    @Nullable
    public CalculateValue secondaryValue;

    public OperatorValue(Operator operator, CalculateValue value) {
        this(operator, value, null);
    };

    public OperatorValue(Operator operator, CalculateValue value, @Nullable CalculateValue secondaryValue) {
        this.operator = operator;
        this.value = value;
        this.secondaryValue = secondaryValue;
    };

    @Override
    public double calculateValue(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {

        double value1 = value.calculateValue(level, state, pos, currentTime, isRaining, isThundering);
        double value2;
        if (secondaryValue != null) {
            value2 = secondaryValue.calculateValue(level, state, pos, currentTime, isRaining, isThundering);
        } else {
            value2 = 0.0;
        }

        switch (operator) {
            case ADD -> {
                return value1 + value2;
            }
            case SUB -> {
                return value1 - value2;
            }
            case DIV -> {
                return value1 / value2;
            }
            case MUL -> {
                return value1 * value2;
            }
            case FLOOR -> {
                return Math.floor(value1);
            }
        }

        return 0;
    }

    @Override
    public boolean isAffectedByWeather(ServerLevel level, BlockState state, BlockPos pos) {
        if (value.isAffectedByWeather(level, state, pos))
            return true;

        if (secondaryValue != null)
            return secondaryValue.isAffectedByWeather(level, state, pos);

        return false;
    }

    @Override
    public long getNextOddsSwitchDuration(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {
        long firstLong = value.getNextOddsSwitchDuration(level, state, pos, currentTime, isRaining, isThundering);

        if (secondaryValue != null) {
            long secondaryLong = secondaryValue.getNextOddsSwitchDuration(level, state, pos, currentTime, isRaining, isThundering);
            return Math.min(firstLong, secondaryLong);
        }

        return firstLong;
    }

    @Override
    public CalculateValue replicate() {
        return new OperatorValue(operator, value.replicate(), secondaryValue == null ? null : secondaryValue.replicate());
    }

    @Override
    public void replaceSuper(CalculateValue superValue) {
        if (value.isSuper()) {
            value = superValue;
        } else {
            value.replaceSuper(superValue);
        }

        if (secondaryValue != null) {
            if (secondaryValue.isSuper()) {
                secondaryValue = superValue;
            } else {
                secondaryValue.replaceSuper(superValue);
            }
        }
    }
}
