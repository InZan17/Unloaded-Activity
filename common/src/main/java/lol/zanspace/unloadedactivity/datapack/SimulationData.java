package lol.zanspace.unloadedactivity.datapack;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import lol.zanspace.unloadedactivity.mixin.CropBlockInvoker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
            thisSimulateProperty.updateNeighbors = otherSimulateProperty.updateNeighbors.or(() -> thisSimulateProperty.updateNeighbors);
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
        public Optional<Boolean> updateNeighbors = Optional.empty();
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
                Optional<FetchValue> fetchValue = FetchValue.fromString(variableName);
                if (fetchValue.isPresent()) {
                    return fetchValue.get();
                }
                throw new RuntimeException(variableName + " is not a valid fetch value.");
            }

            var mapValue = ops.getMap(input);
            if (mapValue.result().isPresent()) {
                MapLike<T> map = mapValue.result().get();

                DataResult<String> operatorResult = ops.getStringValue(map.get("operator"));
                if (operatorResult.result().isPresent()) {
                    String operatorValue = operatorResult.result().get();
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

                    throw new RuntimeException("Invalid operator " + operatorValue);

                }

                DataResult<Condition> conditionResult = parseCondition(ops, input);

                if (conditionResult.result().isPresent()) {
                    Condition condition = conditionResult.result().get();

                    T trueValue = map.get("true");
                    T falseValue = map.get("false");

                    return new ConditionalValue(condition, parseProbability(ops, trueValue), parseProbability(ops, falseValue));
                }


                ArrayList<Pair<Long, CalculateValue>> list = new ArrayList<>();
                for (Iterator<Pair<T, T>> it = map.entries().iterator(); it.hasNext(); ) {
                    var pair = it.next();
                    var stringKeyResult = ops.getStringValue(pair.getFirst());
                    if (stringKeyResult.error().isPresent()) {
                        throw new RuntimeException(stringKeyResult.error().get().message());
                    }
                    String stringKey = stringKeyResult.result().get();
                    try {
                        long number = Long.parseLong(stringKey);
                        list.add(Pair.of(number, parseProbability(ops, pair.getSecond())));
                    } catch(NumberFormatException e){
                        throw new RuntimeException("Probability value has no valid operator key, but also doesn't only contain integer keys.");
                    }
                }
                if (list.isEmpty()) {
                    throw new RuntimeException("Probability value has no keys.");
                }

                return new TimeValue(list);

            }

            throw new RuntimeException("Invalid probability");
        }

        public <T> void parseAndApplyCondition(DynamicOps<T> ops, T input) {
            Condition condition = parseCondition(ops, input).getOrThrow(true, e->{});
            this.conditions.add(condition);
        }

        public static <T> DataResult<Condition> parseCondition(DynamicOps<T> ops, T input) {
            var mapValue = ops.getMap(input);
            if (mapValue.result().isPresent()) {
                MapLike<T> map = mapValue.result().get();

                DataResult<String> comparisonResult = ops.getStringValue(map.get("comparison"));
                if (comparisonResult.error().isPresent()) {
                    return returnError(comparisonResult);
                }
                String comparisonString = comparisonResult.result().get();
                Comparison comparison = Comparison.fromString(comparisonString);

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
                    return DataResult.success(new LocalBrightnessAboveValue(comparison, value));
                }
            }

            throw new RuntimeException("Invalid condition");
        }

    }

    public sealed interface CalculateValue permits NumberValue, ConditionalValue, TimeValue, FetchValue, OperatorValue {
        double calculateValue(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering);

        boolean isAffectedByWeather(ServerLevel level, BlockState state, BlockPos pos);

        long getNextOddsSwitchDuration(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering);

        /// Doesn't guarantee a clone. If a type doesn't get mutated, it's able to return itself.
        CalculateValue replicate();

        default void replaceSuper(CalculateValue superValue) {};
    }

    record NumberValue(double v) implements CalculateValue {
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
    }

    record ConditionalValue(Condition condition, CalculateValue trueValue, CalculateValue falseValue) implements CalculateValue {

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
    }

    static final class LocalBrightnessAboveValue implements Condition {
        // https://www.desmos.com/calculator/bl10cndxzq
        public final static long[] NORMAL_SKY_DARKNESS_START = {
            12041L, // 1
            12210L, // 2
            12377L, // 3
            12541L, // 4
            12704L, // 5
            12866L, // 6
            13027L, // 7
            13188L, // 8
            13348L, // 9
            13509L, // 10
            13670L, // 11
        };

        public final static long[] NORMAL_SKY_DARKNESS_END = {
            23959L, // 1
            23790L, // 2
            23623L, // 3
            23459L, // 4
            23295L, // 5
            23134L, // 6
            22973L, // 7
            22812L, // 8
            22652L, // 9
            22491L, // 10
            22330L, // 11
        };

        public final static long[] RAIN_SKY_DARKNESS_START = {
            12009L, // 4
            12256L, // 5
            12497L, // 6
            12734L, // 7
            12969L, // 8
            13202L, // 9
            13436L, // 10
            13670L, // 11
        };

        public final static long[] RAIN_SKY_DARKNESS_END = {
            23991L, // 4
            23744L, // 5
            23503L, // 6
            23266L, // 7
            23031L, // 8
            22798L, // 9
            22564L, // 10
            22330L, // 11
        };

        public final static long[] RAIN_THUNDER_SKY_DARKNESS_START = {
            11941L, // 6
            12300L, // 7
            12648L, // 8
            12990L, // 9
            13330L, // 10
            13670L, // 11
        };

        public final static long[] RAIN_THUNDER_SKY_DARKNESS_END = {
            56L, // 6
            23700L, // 7
            23352L, // 8
            23010L, // 9
            22670L, // 10
            22330L, // 11
        };

        static {
            assert NORMAL_SKY_DARKNESS_START.length == NORMAL_SKY_DARKNESS_END.length;
            assert RAIN_SKY_DARKNESS_START.length == RAIN_SKY_DARKNESS_END.length;
            assert RAIN_THUNDER_SKY_DARKNESS_START.length == RAIN_THUNDER_SKY_DARKNESS_END.length;
        }

        public final static int MAX_DARKNESS = 11;

        private int targetBrightness;
        private Comparison comparison;

        LocalBrightnessAboveValue(Comparison comparison, int targetBrightness) {
            this.comparison = comparison;
            this.targetBrightness = targetBrightness;
        }

        @Override
        public boolean isValid(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {
            int blockLight = level.getBrightness(LightLayer.BLOCK, pos.above());
            int skyLight = level.getBrightness(LightLayer.SKY, pos.above());

            int darken = getCurrentSkyDarken(currentTime, isRaining, isThundering);

            int value = Math.max(blockLight, skyLight - darken);

            return comparison.compare(value, targetBrightness);
        }

        @Override
        public boolean isDynamic() {
            return true;
        }

        @Override
        public boolean isAffectedByWeather(ServerLevel level, BlockState state, BlockPos pos) {
            return false;
        }

        @Override
        public long getNextConditionSwitchDuration(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {

            int blockLight = level.getBrightness(LightLayer.BLOCK, pos.above());
            int skyLight = level.getBrightness(LightLayer.SKY, pos.above());

            switch (comparison) {
                case NE, EQ -> {
                    if (blockLight > targetBrightness)
                        return Long.MAX_VALUE;

                    int neededDarken = skyLight - targetBrightness;

                    if (neededDarken > MAX_DARKNESS) {
                        return Long.MAX_VALUE;
                    }

                    int currentDarken = getCurrentSkyDarken(currentTime, isRaining, isThundering);

                    if (currentDarken > neededDarken) {
                        return getNextSkyDarkenStopDuration(neededDarken+1, currentTime, isRaining, isThundering);
                    }

                    if (currentDarken < neededDarken) {
                        return getNextSkyDarkenStartDuration(neededDarken, currentTime, isRaining, isThundering);
                    }

                    return getNextSkyDarkenStartDuration(neededDarken+1, currentTime, isRaining, isThundering);
                }
                case LT -> {
                    // Now we do the checks for Less or Equal with newTargetBrightness.
                    int newTargetBrightness = targetBrightness - 1;

                    if (blockLight > newTargetBrightness)
                        return Long.MAX_VALUE;

                    int neededDarken = skyLight - newTargetBrightness;

                    if (neededDarken > MAX_DARKNESS) {
                        return Long.MAX_VALUE;
                    }

                    int currentDarken = getCurrentSkyDarken(currentTime, isRaining, isThundering);

                    if (currentDarken < neededDarken) {
                        return getNextSkyDarkenStartDuration(neededDarken, currentTime, isRaining, isThundering);
                    }

                    return getNextSkyDarkenStopDuration(neededDarken, currentTime, isRaining, isThundering);
                }
                case LE -> {
                    if (blockLight > targetBrightness)
                        return Long.MAX_VALUE;

                    int neededDarken = skyLight - targetBrightness;

                    if (neededDarken > MAX_DARKNESS) {
                        return Long.MAX_VALUE;
                    }

                    int currentDarken = getCurrentSkyDarken(currentTime, isRaining, isThundering);

                    if (currentDarken < neededDarken) {
                        return getNextSkyDarkenStartDuration(neededDarken, currentTime, isRaining, isThundering);
                    }

                    return getNextSkyDarkenStopDuration(neededDarken, currentTime, isRaining, isThundering);
                }
                case GT -> {
                    // Now we do the checks for Greater or Equal with newTargetBrightness.
                    int newTargetBrightness = targetBrightness + 1;

                    if (blockLight >= newTargetBrightness)
                        return Long.MAX_VALUE;

                    int maxAllowedDarken = skyLight - newTargetBrightness;

                    if (maxAllowedDarken >= MAX_DARKNESS) {
                        return Long.MAX_VALUE;
                    }

                    int currentDarken = getCurrentSkyDarken(currentTime, isRaining, isThundering);

                    if (currentDarken > maxAllowedDarken) {
                        return getNextSkyDarkenStopDuration(maxAllowedDarken+1, currentTime, isRaining, isThundering);
                    }

                    return getNextSkyDarkenStartDuration(maxAllowedDarken+1, currentTime, isRaining, isThundering);

                }
                case GE -> {
                    if (blockLight >= targetBrightness)
                        return Long.MAX_VALUE;

                    int maxAllowedDarken = skyLight - targetBrightness;

                    if (maxAllowedDarken >= MAX_DARKNESS) {
                        return Long.MAX_VALUE;
                    }

                    int currentDarken = getCurrentSkyDarken(currentTime, isRaining, isThundering);

                    if (currentDarken > maxAllowedDarken) {
                        return getNextSkyDarkenStopDuration(maxAllowedDarken+1, currentTime, isRaining, isThundering);
                    }

                    return getNextSkyDarkenStartDuration(maxAllowedDarken+1, currentTime, isRaining, isThundering);
                }
            }

            return Long.MAX_VALUE;
        }

        public long[] getStartArray(boolean isRaining, boolean isThundering) {
            if (isRaining && !isThundering) {
                return RAIN_SKY_DARKNESS_START;
            } else if (isThundering) {
                return RAIN_THUNDER_SKY_DARKNESS_START;
            } else {
                return NORMAL_SKY_DARKNESS_START;
            }
        }

        public long[] getEndArray(boolean isRaining, boolean isThundering) {
            if (isRaining && !isThundering) {
                return RAIN_SKY_DARKNESS_END;
            } else if (isThundering) {
                return RAIN_THUNDER_SKY_DARKNESS_END;
            } else {
                return NORMAL_SKY_DARKNESS_END;
            }
        }

        public int getCurrentSkyDarken(long currentTime, boolean isRaining, boolean isThundering) {
            long[] startTimes = getStartArray(isRaining, isThundering);
            long[] endTimes = getEndArray(isRaining, isThundering);

            int darkenOffset = MAX_DARKNESS - startTimes.length;

            assert startTimes.length == endTimes.length;

            long modTime = Math.floorMod(currentTime, 24000);

            for (int i=startTimes.length-1; i>=0; i--) {
                long darkStart = startTimes[i];
                long darkEnd = endTimes[i];

                if (modTime >= darkStart && modTime < darkEnd) {
                    return i + darkenOffset;
                }

                if (darkStart > darkEnd) {
                    if (modTime >= darkStart || modTime < darkEnd) {
                        return i + darkenOffset;
                    }
                }
            }
            return darkenOffset - 1;
        }

        public long getNextSkyDarkenStartDuration(int darken, long currentTime, boolean isRaining, boolean isThundering) {
            long[] startTimes = getStartArray(isRaining, isThundering);

            int darkenOffset = MAX_DARKNESS - startTimes.length;

            int index = darken - darkenOffset;

            if (index < 0) {
                return Long.MAX_VALUE;
            }

            long startTime = startTimes[index];

            long daysPassed = Math.floorDiv(currentTime, 24000);

            startTime += daysPassed * 24000;

            if (currentTime > startTime) {
                startTime += 24000;
            }

            return startTime - currentTime;
        }

        public long getNextSkyDarkenStopDuration(int darken, long currentTime, boolean isRaining, boolean isThundering) {
            long[] endTimes = getEndArray(isRaining, isThundering);

            int darkenOffset = MAX_DARKNESS - endTimes.length;

            int index = darken - darkenOffset;

            if (index < 0) {
                return Long.MAX_VALUE;
            }

            long endTime = endTimes[index];

            long daysPassed = Math.floorDiv(currentTime, 24000);

            endTime += daysPassed * 24000;

            if (currentTime > endTime) {
                endTime += 24000;
            }

            return endTime - currentTime;
        }
    }

    static final class TimeValue implements CalculateValue {
        private final List<Pair<Long, CalculateValue>> list;
        TimeValue(List<Pair<Long, CalculateValue>> list) {
            list.sort(Comparator.comparing(Pair::getFirst));
            this.list = list;
        }

        @Override
        public double calculateValue(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {
            if (this.list.isEmpty())
                return 0;

            long length = 24000;
            long modCurrentTime = Math.floorMod(currentTime, length);

            var currentPair = this.list.get(this.list.size() - 1);

            for (var pair : this.list) {
                if (pair.getFirst() <= modCurrentTime) {
                    currentPair = pair;
                } else {
                    break;
                }
            }

            return currentPair.getSecond().calculateValue(level, state, pos, currentTime, isRaining, isThundering);
        }

        @Override
        public boolean isAffectedByWeather(ServerLevel level, BlockState state, BlockPos pos) {
            return false;
        }

        @Override
        public long getNextOddsSwitchDuration(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {
            if (this.list.isEmpty())
                return Long.MAX_VALUE;

            long length = 24000;
            long modCurrentTime = Math.floorMod(currentTime, length);

            var currentPair = this.list.get(this.list.size() - 1);
            Pair<Long, CalculateValue> nextPair = null;

            for (var pair : this.list) {
                if (pair.getFirst() <= modCurrentTime) {
                    currentPair = pair;
                } else {
                    nextPair = pair;
                    break;
                }
            }

            if (nextPair == null) {
                nextPair = this.list.get(0);
            }

            long currentNextOddsSwitch = currentPair.getSecond().getNextOddsSwitchDuration(level, state, pos, currentTime, isRaining, isThundering);
            long nextOddsSwitch;

            if (nextPair.getFirst() == currentPair.getFirst()) {
                nextOddsSwitch = Long.MAX_VALUE;
            } else {
                nextOddsSwitch = nextPair.getFirst() - modCurrentTime;
                if (nextOddsSwitch < 0) {
                    nextOddsSwitch += 24000;
                }
            }


            return Math.min(currentNextOddsSwitch, nextOddsSwitch);
        }

        @Override
        public CalculateValue replicate() {
            List<Pair<Long, CalculateValue>> newList = new ArrayList<>();
            for (var pair : this.list) {
                newList.add(Pair.of(pair.getFirst(), pair.getSecond().replicate()));
            }
            return new TimeValue(newList);
        }
    }

    public enum FetchValue implements CalculateValue {
        GROWTH_SPEED {
            @Override
            public double calculateValue(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {
                #if MC_VER >= MC_1_21_1
                return ExpectPlatform.getGrowthSpeed(state, level, pos);
                #else
                return CropBlockInvoker.invokeGetGrowthSpeed(state.getBlock(), level, pos);
                #endif
            }
        },

        AVAILABLE_SPACE_FOR_GOURD {
            @Override
            public double calculateValue(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {
                return (Utils.isValidGourdPosition(Direction.NORTH, pos, level) ? 1 : 0)
                    + (Utils.isValidGourdPosition(Direction.EAST, pos, level) ? 1 : 0)
                    + (Utils.isValidGourdPosition(Direction.SOUTH, pos, level) ? 1 : 0)
                    + (Utils.isValidGourdPosition(Direction.WEST, pos, level) ? 1 : 0);

            }
        },

        RAW_BRIGHTNESS {
            @Override
            public double calculateValue(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {
                return level.getRawBrightness(pos, 0);
            }
        },

        RAW_BRIGHTNESS_ABOVE {
            @Override
            public double calculateValue(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {
                return level.getRawBrightness(pos.above(), 0);
            }
        },

        SUPER {
            @Override
            public double calculateValue(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {
                return 1;
            }
        };

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

        public static Optional<FetchValue> fromString(String variableName) {
            switch (variableName.toLowerCase()) {
                case "growth_speed" -> {
                    return Optional.of(GROWTH_SPEED);
                }
                case "available_space_for_gourd" -> {
                    return Optional.of(AVAILABLE_SPACE_FOR_GOURD);
                }
                case "raw_brightness" -> {
                    return Optional.of(RAW_BRIGHTNESS);
                }
                case "raw_brightness_above" -> {
                    return Optional.of(RAW_BRIGHTNESS_ABOVE);
                }
                case "super" -> {
                    return Optional.of(SUPER);
                }
            }
            return Optional.empty();
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

    public interface Condition {
        boolean isValid(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering);
        boolean isDynamic();
        boolean isAffectedByWeather(ServerLevel level, BlockState state, BlockPos pos);
        long getNextConditionSwitchDuration(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering);
    }

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
                                    case "update_none" -> simulateProperty.updateType = Optional.of(Block.UPDATE_NONE);
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
                        T mapValue = propertyInfo.get("update_neighbors");
                        if (mapValue != null) {
                            DataResult<Boolean> valueResult = ops.getBooleanValue(mapValue);
                            if (valueResult.error().isPresent()) {
                                return returnError(valueResult);
                            }
                            simulateProperty.updateNeighbors = valueResult.result();
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
