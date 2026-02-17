package lol.zanspace.unloadedactivity.datapack;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import lol.zanspace.unloadedactivity.datapack.calculate_value.FetchValue;
import lol.zanspace.unloadedactivity.datapack.condition.LocalBrightnessAboveCondition;
import lol.zanspace.unloadedactivity.datapack.condition.StaticCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

import static lol.zanspace.unloadedactivity.datapack.SimulationData.returnError;

public interface Condition {
    boolean isValid(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering);
    boolean isDynamic();
    boolean isAffectedByWeather(ServerLevel level, BlockState state, BlockPos pos);
    long getNextConditionSwitchDuration(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering);

    public static <T> DataResult<Condition> parse(DynamicOps<T> ops, T input) {
        var mapValue = ops.getMap(input);
        if (mapValue.result().isPresent()) {
            MapLike<T> map = mapValue.result().get();

            DataResult<String> comparisonResult = ops.getStringValue(map.get("comparison"));
            if (comparisonResult.error().isPresent()) {
                return returnError(comparisonResult);
            }
            String comparisonString = comparisonResult.result().get();
            Optional<Comparison> maybeComparison = Comparison.fromString(comparisonString);

            if (!maybeComparison.isPresent()) {
                throw new RuntimeException(comparisonString + " is not a valid comparison.");
            }

            Comparison comparison = maybeComparison.get();

            DataResult<String> checkResult = ops.getStringValue(map.get("check"));
            if (checkResult.error().isPresent()) {
                return returnError(checkResult);
            }
            String check = checkResult.result().get();

            T compareValue = map.get("value");

            int value;

            DataResult<Number> numberValue = ops.getNumberValue(compareValue);

            if (numberValue.result().isPresent()) {
                value = numberValue.result().get().intValue();
            } else {
                DataResult<Boolean> booleanValue = ops.getBooleanValue(compareValue);
                if (booleanValue.result().isPresent()) {
                    boolean boolValue = booleanValue.result().get();
                    value = boolValue ? 1 : 0;
                } else {
                    throw new RuntimeException("Invalid value to compare to. Must be a number or a boolean");
                }
            }

            Optional<FetchValue> fetchValue = FetchValue.fromString(check);

            if (fetchValue.isPresent()) {
                return DataResult.success(new StaticCondition(fetchValue.get(), comparison, value));
            }

            if (check.equals("local_brightness_above")) {
                return DataResult.success(new LocalBrightnessAboveCondition(comparison, value));
            }
        }

        throw new RuntimeException("Invalid condition");
    }
}
