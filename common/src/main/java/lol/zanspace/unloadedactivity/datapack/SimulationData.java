package lol.zanspace.unloadedactivity.datapack;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import lol.zanspace.unloadedactivity.ExpectPlatform;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.mixin.CropBlockInvoker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;



import javax.swing.*;
import java.util.*;
import java.util.stream.Stream;

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

            thisSimulateProperty.propertyType = otherSimulateProperty.propertyType.or(() -> otherSimulateProperty.propertyType);
            thisSimulateProperty.advanceProbability = otherSimulateProperty.advanceProbability.or(() -> otherSimulateProperty.advanceProbability);
            thisSimulateProperty.maxValue = otherSimulateProperty.maxValue.or(() -> otherSimulateProperty.maxValue);
        }
    }

    public static class SimulateProperty {
        public Optional<String> propertyType;
        public Optional<CalculateValue> advanceProbability;
        public Optional<Integer> maxValue;
        public List<Condition> conditions;

        SimulateProperty() {
            this.propertyType = Optional.empty();
            this.advanceProbability = Optional.empty();
            this.maxValue = Optional.empty();
            this.conditions = new ArrayList<>();
        }

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
                switch (variableName.toLowerCase()) {
                    case "growth_speed" -> {
                        return FetchValue.GROWTH_SPEED;
                    }
                    case "super" -> {
                        return FetchValue.SUPER;
                    }
                    default -> {
                        throw new RuntimeException("Invalid variable: " + variableName);
                    }
                }
            }

            var mapValue = ops.getMap(input);
            if (mapValue.result().isPresent()) {
                MapLike<T> map = mapValue.result().get();

                UnloadedActivity.LOGGER.info(map.entries().toList().toString());

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
                        return new OperatorValue(Operator.FLOOR, parseProbability(ops, oneValue), null);
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

                switch (check.toLowerCase()) {
                    case "raw_brightness" -> {
                        return new Condition(new RawBrightness(), comparison, value);
                    }
                    default -> throw new RuntimeException("Invalid check value: " + check);
                }
            }

            throw new RuntimeException("Invalid condition");
        }

    }

    public sealed interface CalculateValue permits NumberValue, FetchValue, OperatorValue {
        double calculateValue(ServerLevel level, BlockState state, BlockPos pos);
    }

    record NumberValue(double v) implements CalculateValue {
        @Override
        public double calculateValue(ServerLevel level, BlockState state, BlockPos pos) {
            return v;
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

        SUPER {
            @Override
            public double calculateValue(ServerLevel level, BlockState state, BlockPos pos) {
                return 1;
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

    record OperatorValue(Operator operator, CalculateValue value, CalculateValue secondaryValue) implements CalculateValue {
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
    }

    public sealed interface GetValueForCondition permits RawBrightness {
        int getValue(ServerLevel level, BlockState state, BlockPos pos);
    }

    public static final class RawBrightness implements GetValueForCondition {
        @Override
        public int getValue(ServerLevel level, BlockState state, BlockPos pos) {
            return level.getRawBrightness(pos, 0);
        }
    }

    public sealed interface CompareTheThing {
        boolean compare(int v1, int v2);
    }

    public enum Comparison implements CompareTheThing {
        EQ {
            @Override
            public boolean compare(int v1, int v2) {
                return false;
            }
        },
        NE {
            @Override
            public boolean compare(int v1, int v2) {
                return v1 != v2;
            }
        },
        LT {
            @Override
            public boolean compare(int v1, int v2) {
                return v1 < v2;
            }
        },
        LE {
            @Override
            public boolean compare(int v1, int v2) {
                return v1 <= v2;
            }
        },
        GT {
            @Override
            public boolean compare(int v1, int v2) {
                return v1 > v2;
            }
        },
        GE {
            @Override
            public boolean compare(int v1, int v2) {
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

    public record Condition (GetValueForCondition valueGetter, Comparison comparison, int value) {
        public boolean isValid(ServerLevel level, BlockState state, BlockPos pos) {
            return comparison.compare(valueGetter.getValue(level, state, pos), value);
        }
    }

    static {
        CODEC = new Codec<SimulationData>() {
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
        return DataResult.error(info + dataResult.error().get().message());
        #endif
    }
}
