package lol.zanspace.unloadedactivity.datapack;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import lol.zanspace.unloadedactivity.ExpectPlatform;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import lol.zanspace.unloadedactivity.mixin.CropBlockInvoker;
import lol.zanspace.unloadedactivity.mixin.chunk.randomTicks.StemMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class SimulationData {
    public static final Codec<SimulationData> CODEC;

    public Map<String, SimulateProperty> propertyMap;

    public boolean isFinal = false;

    public SimulationData() {
        this.propertyMap = new HashMap<>();
    }

    public boolean isEmpty() {
        return this.propertyMap.isEmpty();
    }

    public void absorb(SimulationData otherSimulationData) {
        for (var entry : otherSimulationData.propertyMap.entrySet()) {
            var thisSimulateProperty = this.propertyMap.computeIfAbsent(entry.getKey(), k -> new SimulateProperty());
            var otherSimulateProperty = entry.getValue();


            thisSimulateProperty.propertyType = otherSimulateProperty.propertyType.or(() -> thisSimulateProperty.propertyType);
            thisSimulateProperty.maxValue = otherSimulateProperty.maxValue.or(() -> thisSimulateProperty.maxValue);
            thisSimulateProperty.maxHeight = otherSimulateProperty.maxHeight.or(() -> thisSimulateProperty.maxHeight);
            thisSimulateProperty.dependencies.addAll(otherSimulateProperty.dependencies);
            thisSimulateProperty.updateType = otherSimulateProperty.updateType.or(() -> thisSimulateProperty.updateType);
            thisSimulateProperty.resetOnHeightChange = otherSimulateProperty.resetOnHeightChange.or(() -> thisSimulateProperty.resetOnHeightChange);
            thisSimulateProperty.keepUpdatingAfterMaxHeight = otherSimulateProperty.keepUpdatingAfterMaxHeight.or(() -> thisSimulateProperty.keepUpdatingAfterMaxHeight);
            thisSimulateProperty.conditions.addAll(otherSimulateProperty.conditions);

            if (otherSimulateProperty.advanceProbability.isPresent() && thisSimulateProperty.advanceProbability.isPresent()) {
                var oldProbability = thisSimulateProperty.advanceProbability.get();
                var newProbability = otherSimulateProperty.advanceProbability.get().replicate();

                newProbability.replaceSuper(oldProbability);

                thisSimulateProperty.advanceProbability = Optional.of(newProbability);
            } else {
                thisSimulateProperty.advanceProbability = otherSimulateProperty.advanceProbability.map(CalculateValue::replicate).or(() -> thisSimulateProperty.advanceProbability);
            }
        }
    }

    public static class SimulateProperty {
        public Set<String> dependencies = new HashSet<>();
        public Optional<String> propertyType = Optional.empty();
        public Optional<Integer> maxHeight = Optional.empty();
        public Optional<Boolean> resetOnHeightChange = Optional.empty();
        public Optional<Boolean> keepUpdatingAfterMaxHeight = Optional.empty();
        public Optional<Integer> updateType = Optional.empty();
        public Optional<CalculateValue> advanceProbability = Optional.empty();
        public Optional<Integer> maxValue = Optional.empty();
        public ArrayList<Condition> conditions = new ArrayList<>();

        public <T> void parseAndApplyProbability(DynamicOps<T> ops, T input) {
            CalculateValue calculateValue = parseProbability(ops, input);
            this.advanceProbability = Optional.of(calculateValue);
        }

        public static <T> CalculateValue parseProbability(DynamicOps<T> ops, T input) {

            var numberValue = ops.getNumberValue(input);
            if (numberValue.result().isPresent()) {
                return new NumberValue(numberValue.result().get().doubleValue());
            }

            var stringValue = ops.getStringValue(input);
            if (stringValue.result().isPresent()) {
                String variableName = stringValue.result().get();
                return FetchValue.fromString(variableName);
            }

            var mapValue = ops.getMap(input);
            if (mapValue.result().isPresent()) {
                MapLike<T> map = mapValue.result().get();

                DataResult<String> stringResult = ops.getStringValue(map.get("operator"));
                if (stringResult.error().isPresent()) {
                    throw new RuntimeException(stringResult.error().get().message());
                }
                String operatorValue = stringResult.result().get();
                T oneValue = map.get("value");
                T value1 = map.get("value1");
                T value2 = map.get("value2");

                switch (operatorValue.toLowerCase()) {
                    case "+" -> {
                        return new OperatorValue(Operator.ADD, parseProbability(ops, value1), parseProbability(ops, value2));
                    }
                    case "-" -> {
                        return new OperatorValue(Operator.SUB, parseProbability(ops, value1), parseProbability(ops, value2));
                    }
                    case "/" -> {
                        return new OperatorValue(Operator.DIV, parseProbability(ops, value1), parseProbability(ops, value2));
                    }
                    case "*" -> {
                        return new OperatorValue(Operator.MUL, parseProbability(ops, value1), parseProbability(ops, value2));
                    }
                    case "floor" -> {
                        return new OperatorValue(Operator.FLOOR, parseProbability(ops, oneValue));
                    }
                }
            }

            throw new RuntimeException("Invalid probability");
        }

        public <T> void parseAndApplyCondition(DynamicOps<T> ops, T input) {
            Condition condition = parseCondition(ops, input);
            this.conditions.add(condition);
        }

        public static <T> Condition parseCondition(DynamicOps<T> ops, T input) {
            var mapValue = ops.getMap(input);
            if (mapValue.result().isPresent()) {
                MapLike<T> map = mapValue.result().get();

                DataResult<String> comparisonResult = ops.getStringValue(map.get("comparison"));
                if (comparisonResult.error().isPresent()) {
                    throw new RuntimeException(comparisonResult.error().get().message());
                }
                String comparisonString = comparisonResult.result().get();
                Comparison comparison = Comparison.fromString(comparisonString);

                DataResult<String> checkResult = ops.getStringValue(map.get("check"));
                if (checkResult.error().isPresent()) {
                    throw new RuntimeException(checkResult.error().get().message());
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
                        throw new RuntimeException("Invalid value. Must be a number or a boolean");
                    }
                }

                return new Condition(FetchValue.fromString(check), comparison, value);
            }

            throw new RuntimeException("Invalid condition");
        }

    }

    public sealed interface CalculateValue permits NumberValue, FetchValue, OperatorValue {
        double calculateValue(ServerLevel level, BlockState state, BlockPos pos);

        /// Doesn't guarantee a clone. If a type doesn't get mutated, it's able to return itself.
        CalculateValue replicate();

        default void replaceSuper(CalculateValue superValue) {};
    }

    record NumberValue(double v) implements CalculateValue {
        @Override
        public double calculateValue(ServerLevel level, BlockState state, BlockPos pos) {
            return v;
        }

        @Override
        public CalculateValue replicate() {
            return this;
        }
    }

    public enum FetchValue implements CalculateValue {
        GROWTH_SPEED {
            @Override
            public double calculateValue(ServerLevel level, BlockState state, BlockPos pos) {
                #if MC_VER >= MC_1_21_1
                return ExpectPlatform.getGrowthSpeed(state, level, pos);
                #else
                return CropBlockInvoker.invokeGetGrowthSpeed(state.getBlock(), level, pos);
                #endif
            }
        },

        AVAILABLE_SPACE_FOR_GOURD {
            @Override
            public double calculateValue(ServerLevel level, BlockState state, BlockPos pos) {
                return (Utils.isValidGourdPosition(Direction.NORTH, pos, level) ? 1 : 0)
                    + (Utils.isValidGourdPosition(Direction.EAST, pos, level) ? 1 : 0)
                    + (Utils.isValidGourdPosition(Direction.SOUTH, pos, level) ? 1 : 0)
                    + (Utils.isValidGourdPosition(Direction.WEST, pos, level) ? 1 : 0);

            }
        },

        RAW_BRIGHTNESS {
            @Override
            public double calculateValue(ServerLevel level, BlockState state, BlockPos pos) {
                return level.getRawBrightness(pos, 0);
            }
        },

        SUPER {
            @Override
            public double calculateValue(ServerLevel level, BlockState state, BlockPos pos) {
                return 1;
            }
        };

        @Override
        public CalculateValue replicate() {
            return this;
        }

        public static FetchValue fromString(String variableName) {
            switch (variableName.toLowerCase()) {
                case "growth_speed" -> {
                    return GROWTH_SPEED;
                }
                case "available_space_for_gourd" -> {
                    return AVAILABLE_SPACE_FOR_GOURD;
                }
                case "raw_brightness" -> {
                    return RAW_BRIGHTNESS;
                }
                case "super" -> {
                    return SUPER;
                }
                default -> {
                    throw new RuntimeException("Invalid variable: " + variableName);
                }
            }
        };
    }

    public enum Operator {
        ADD,
        SUB,
        DIV,
        MUL,
        FLOOR,
    }

    static final class OperatorValue implements CalculateValue {
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
        public double calculateValue(ServerLevel level, BlockState state, BlockPos pos) {

            double value1 = value.calculateValue(level, state, pos);
            double value2;
            if (secondaryValue != null) {
                value2 = secondaryValue.calculateValue(level, state, pos);
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
        public CalculateValue replicate() {
            return new OperatorValue(operator, value.replicate(), secondaryValue == null ? null : secondaryValue.replicate());
        }

        @Override
        public void replaceSuper(CalculateValue superValue) {
            if (value instanceof FetchValue fetchValue) {
                if (fetchValue == FetchValue.SUPER) {
                    value = superValue;
                }
            }

            if (secondaryValue instanceof FetchValue fetchValue) {
                if (fetchValue == FetchValue.SUPER) {
                    secondaryValue = superValue;
                }
            }
        }
    }

    public sealed interface CompareTheThing {
        boolean compare(double v1, double v2);
    }

    public enum Comparison implements CompareTheThing {
        EQ {
            @Override
            public boolean compare(double v1, double v2) {
                return false;
            }
        },
        NE {
            @Override
            public boolean compare(double v1, double v2) {
                return v1 != v2;
            }
        },
        LT {
            @Override
            public boolean compare(double v1, double v2) {
                return v1 < v2;
            }
        },
        LE {
            @Override
            public boolean compare(double v1, double v2) {
                return v1 <= v2;
            }
        },
        GT {
            @Override
            public boolean compare(double v1, double v2) {
                return v1 > v2;
            }
        },
        GE {
            @Override
            public boolean compare(double v1, double v2) {
                return v1 >= v2;
            }
        };

        public static Comparison fromString(String comparisonString) {
            switch (comparisonString.toLowerCase()) {
                case "equal" -> {
                    return Comparison.EQ;
                }
                case "not_equal" -> {
                    return Comparison.NE;
                }
                case "less_than" -> {
                    return Comparison.LT;
                }
                case "less_or_equal" -> {
                    return Comparison.LE;
                }
                case "greater_than" -> {
                    return Comparison.GT;
                }
                case "greater_or_equal" -> {
                    return Comparison.GE;
                }
                default -> throw new RuntimeException("Invalid comparison: "+comparisonString);

            }
        }
    }

    public record Condition (FetchValue valueGetter, Comparison comparison, double value) {
        public boolean isValid(ServerLevel level, BlockState state, BlockPos pos) {
            return comparison.compare(valueGetter.calculateValue(level, state, pos), value);
        }
    }

    static {
        CODEC = new Codec<>() {
            @Override
            public <T> DataResult<T> encode(SimulationData input, DynamicOps<T> ops, T prefix) {
                throw new UnsupportedOperationException("I am never using this. Therefore, it does not need to be implemented.");
            }

            @Override
            public <T> DataResult<Pair<SimulationData, T>> decode(DynamicOps<T> ops, T input) {
                SimulationData simulationData = new SimulationData();

                var mapResult = ops.getMap(input);

                if (mapResult.error().isPresent()) {
                    return returnError(mapResult);
                }
                MapLike<T> map = mapResult.result().get();

                for (var pair : map.entries().toList()) {
                    T key = pair.getFirst();
                    T value = pair.getSecond();

                    var propertyNameResult = ops.getStringValue(key);

                    if (propertyNameResult.error().isPresent()) {
                        return returnError(propertyNameResult);
                    }

                    String propertyName = propertyNameResult.result().get();

                    var propertyInfoResult = ops.getMap(value);

                    if (propertyInfoResult.error().isPresent()) {
                        return returnError(propertyInfoResult);
                    }

                    MapLike<T> propertyInfo = propertyInfoResult.result().get();

                    SimulationData.SimulateProperty simulateProperty = new SimulationData.SimulateProperty();

                    {
                        T mapValue = propertyInfo.get("property_type");
                        if (mapValue != null) {
                            DataResult<String> valueResult = ops.getStringValue(mapValue);
                            if (valueResult.error().isPresent()) {
                                return returnError(valueResult);
                            }
                            simulateProperty.propertyType = valueResult.result();
                        }
                    }

                    {
                        T mapValue = propertyInfo.get("update_type");
                        if (mapValue != null) {
                            DataResult<String> stringResult = ops.getStringValue(mapValue);
                            if (stringResult.result().isPresent()) {
                                String updateType = stringResult.result().get();
                                switch (updateType.toLowerCase()) {
                                    case "update_clients" -> simulateProperty.updateType = Optional.of(Block.UPDATE_CLIENTS);
                                    case "update_invisible" -> simulateProperty.updateType = Optional.of(Block.UPDATE_INVISIBLE);
                                    case "update_all" -> simulateProperty.updateType = Optional.of(Block.UPDATE_ALL);
                                    default -> {
                                        return returnError("Invalid update type: " + updateType);
                                    }
                                }
                            } else {
                                DataResult<Number> numberResult = ops.getNumberValue(mapValue);
                                if (numberResult.error().isPresent()) {
                                    return returnError("Must be a number or a string.", numberResult);
                                }
                                simulateProperty.updateType = Optional.of(numberResult.result().get().intValue());
                            }
                        }
                    }

                    {
                        T mapValue = propertyInfo.get("advance_probability");
                        if (mapValue != null) {
                            simulateProperty.parseAndApplyProbability(ops, mapValue);
                        }
                    }

                    {
                        T mapValue = propertyInfo.get("max_value");
                        if (mapValue != null) {
                            DataResult<Number> valueResult = ops.getNumberValue(mapValue);
                            if (valueResult.error().isPresent()) {
                                return returnError(valueResult);
                            }
                            simulateProperty.maxValue = valueResult.result().map(Number::intValue);
                        }
                    }

                    {
                        T mapValue = propertyInfo.get("max_height");
                        if (mapValue != null) {
                            DataResult<Number> valueResult = ops.getNumberValue(mapValue);
                            if (valueResult.error().isPresent()) {
                                return returnError(valueResult);
                            }
                            simulateProperty.maxHeight = valueResult.result().map(Number::intValue);
                            UnloadedActivity.LOGGER.info("" + simulateProperty.maxHeight);
                        }
                    }

                    {
                        T mapValue = propertyInfo.get("reset_on_height_change");
                        if (mapValue != null) {
                            DataResult<Boolean> valueResult = ops.getBooleanValue(mapValue);
                            if (valueResult.error().isPresent()) {
                                return returnError(valueResult);
                            }
                            simulateProperty.resetOnHeightChange = valueResult.result();
                        }
                    }

                    {
                        T mapValue = propertyInfo.get("keep_updating_after_max_height");
                        if (mapValue != null) {
                            DataResult<Boolean> valueResult = ops.getBooleanValue(mapValue);
                            if (valueResult.error().isPresent()) {
                                return returnError(valueResult);
                            }
                            simulateProperty.keepUpdatingAfterMaxHeight = valueResult.result();
                        }
                    }

                    {
                        T mapValue = propertyInfo.get("conditions");
                        if (mapValue != null) {
                            var listResult = ops.getStream(mapValue);
                            if (listResult.error().isPresent()) {
                                return returnError(listResult);
                            }

                            for (T condition : listResult.result().get().toList()) {
                                simulateProperty.parseAndApplyCondition(ops, condition);
                            }
                        }
                    }

                    {
                        T mapValue = propertyInfo.get("dependencies");
                        if (mapValue != null) {
                            var dependencies = ops.getStream(mapValue);
                            if (dependencies.error().isPresent()) {
                                return returnError(dependencies);
                            }

                            for (T dependencyValue : dependencies.result().get().toList()) {
                                var stringResult = ops.getStringValue(dependencyValue);

                                if (stringResult.error().isPresent()) {
                                    return returnError(stringResult);
                                }

                                simulateProperty.dependencies.add(stringResult.result().get());
                            }
                        }
                    }

                    simulationData.propertyMap.put(propertyName, simulateProperty);
                }
                return DataResult.success(Pair.of(simulationData, ops.empty()));
            }
        };
    }

    static <R> DataResult<R> returnError(DataResult<?> dataResult) {
        #if MC_VER >= MC_1_19_4
        return DataResult.error(() -> dataResult.error().get().message());
        #else
        return DataResult.error(dataResult.error().get().message());
        #endif
    }

    static <R> DataResult<R> returnError(String info, DataResult<?> dataResult) {
        #if MC_VER >= MC_1_19_4
        return DataResult.error(() -> info + dataResult.error().get().message());
        #else
        return DataResult.error(info + "\n" + dataResult.error().get().message());
        #endif
    }

    static <R> DataResult<R> returnError(String info) {
        #if MC_VER >= MC_1_19_4
        return DataResult.error(() -> info);
        #else
        return DataResult.error(info);
        #endif
    }
}
