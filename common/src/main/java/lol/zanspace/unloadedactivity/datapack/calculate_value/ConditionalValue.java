package lol.zanspace.unloadedactivity.datapack.calculate_value;

import lol.zanspace.unloadedactivity.datapack.CalculateValue;
import lol.zanspace.unloadedactivity.datapack.Condition;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class ConditionalValue implements CalculateValue {

    Condition condition;
    CalculateValue trueValue;
    CalculateValue falseValue;

    public ConditionalValue(Condition condition, CalculateValue trueValue, CalculateValue falseValue) {
        this.condition = condition;
        this.trueValue = trueValue;
        this.falseValue = falseValue;
    }

    @Override
    public double calculateValue(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {
        if (condition.isValid(level, state, pos, currentTime, isRaining, isThundering)) {
            return trueValue.calculateValue(level, state, pos, currentTime, isRaining, isThundering);
        } else {
            return falseValue.calculateValue(level, state, pos, currentTime, isRaining, isThundering);
        }
    }

    @Override
    public boolean isAffectedByWeather(ServerLevel level, BlockState state, BlockPos pos) {
        return condition.isAffectedByWeather(level, state, pos)
                || trueValue.isAffectedByWeather(level, state, pos)
                || falseValue.isAffectedByWeather(level, state, pos);
    }

    @Override
    public long getNextOddsSwitchDuration(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {
        boolean isValid = condition.isValid(level, state, pos, currentTime, isRaining, isThundering);
        long conditionSwitch = condition.getNextConditionSwitchDuration(level, state, pos, currentTime, isRaining, isThundering);

        return Math.min(
                isValid ?
                        trueValue.getNextOddsSwitchDuration(level, state, pos, currentTime, isRaining, isThundering) :
                        falseValue.getNextOddsSwitchDuration(level, state, pos, currentTime, isRaining, isThundering)
                ,
                conditionSwitch
        );
    }

    @Override
    public CalculateValue replicate() {
        return new ConditionalValue(condition, trueValue.replicate(), falseValue.replicate());
    }

    @Override
    public void replaceSuper(CalculateValue superValue) {
        if (trueValue.isSuper()) {
            trueValue = superValue;
        } else {
            trueValue.replaceSuper(superValue);
        }

        if (falseValue.isSuper()) {
            falseValue = superValue;
        } else {
            falseValue.replaceSuper(superValue);
        }
    }
}