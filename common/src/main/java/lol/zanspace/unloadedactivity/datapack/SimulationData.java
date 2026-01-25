package lol.zanspace.unloadedactivity.datapack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import jdk.dynalink.linker.ConversionComparator;
import lol.zanspace.unloadedactivity.mixin.CropBlockInvoker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import javax.swing.*;
import java.util.*;

public class SimulationData {
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

        public void parseAndApplyProbability(JsonElement value) {
            CalculateValue calculateValue = parseProbability(value);
            this.advanceProbability = Optional.of(calculateValue);
        }

        public static CalculateValue parseProbability(JsonElement value) {
            if (value.isJsonPrimitive()) {
                JsonPrimitive jsonPrimitive = value.getAsJsonPrimitive();

                if (jsonPrimitive.isNumber()) {
                    return new NumberValue(jsonPrimitive.getAsDouble());
                }

                if (jsonPrimitive.isString()) {
                    String fetchValue = jsonPrimitive.getAsString();
                    switch (fetchValue.toLowerCase()) {
                        case "growth_speed" -> {
                            return FetchValue.GROWTH_SPEED;
                        }
                    }
                }

            }

            if (value.isJsonObject()) {
                JsonObject jsonObject = value.getAsJsonObject();
                String operator = jsonObject.get("operator").getAsString();
                JsonElement oneValue = jsonObject.get("value");
                JsonElement value1 = jsonObject.get("value1");
                JsonElement value2 = jsonObject.get("value2");

                switch (operator.toLowerCase()) {
                    case "+" -> {
                        return new OperatorValue(Operator.ADD, parseProbability(value1), parseProbability(value2));
                    }
                    case "-" -> {
                        return new OperatorValue(Operator.SUB, parseProbability(value1), parseProbability(value2));
                    }
                    case "/" -> {
                        return new OperatorValue(Operator.DIV, parseProbability(value1), parseProbability(value2));
                    }
                    case "*" -> {
                        return new OperatorValue(Operator.MUL, parseProbability(value1), parseProbability(value2));
                    }
                    case "floor" -> {
                        return new OperatorValue(Operator.FLOOR, parseProbability(oneValue), null);
                    }
                }
            }

            throw new RuntimeException("Invalid probability");
        }

        public void parseAndApplyCondition(JsonElement element) {
            Condition condition = parseCondition(element);
            this.conditions.add(condition);
        }

        public static Condition parseCondition(JsonElement element) {
            if (element.isJsonObject()) {
                JsonObject jsonObject = element.getAsJsonObject();
                String comparisonString = jsonObject.get("comparison").getAsString();
                String check = jsonObject.get("check").getAsString();
                JsonPrimitive jsonValue = jsonObject.get("value").getAsJsonPrimitive();

                Comparison comparison = Comparison.fromString(comparisonString);

                int value;

                if (jsonValue.isNumber()) {
                    value = jsonValue.getAsInt();
                } else if (jsonValue.isBoolean()) {
                    boolean boolValue = jsonValue.getAsBoolean();
                    value = boolValue ? 1 : 0;
                } else {
                    throw new RuntimeException("Invalid value. Must be a number or a boolean");
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
}
