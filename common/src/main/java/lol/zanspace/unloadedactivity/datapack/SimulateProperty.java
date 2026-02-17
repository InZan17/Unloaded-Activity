package lol.zanspace.unloadedactivity.datapack;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import net.minecraft.world.level.block.Block;

import java.util.*;

import static lol.zanspace.unloadedactivity.datapack.SimulationData.returnError;

public class SimulateProperty {
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

    public void merge(SimulateProperty otherSimulateProperty) {
        this.propertyType = otherSimulateProperty.propertyType.or(() -> this.propertyType);
        this.maxValue = otherSimulateProperty.maxValue.or(() -> this.maxValue);
        this.maxHeight = otherSimulateProperty.maxHeight.or(() -> this.maxHeight);
        this.dependencies.addAll(otherSimulateProperty.dependencies);
        this.updateType = otherSimulateProperty.updateType.or(() -> this.updateType);
        this.updateNeighbors = otherSimulateProperty.updateNeighbors.or(() -> this.updateNeighbors);
        this.resetOnHeightChange = otherSimulateProperty.resetOnHeightChange.or(() -> this.resetOnHeightChange);
        this.keepUpdatingAfterMaxHeight = otherSimulateProperty.keepUpdatingAfterMaxHeight.or(() -> this.keepUpdatingAfterMaxHeight);
        this.conditions.addAll(otherSimulateProperty.conditions);

        if (otherSimulateProperty.advanceProbability.isPresent() && this.advanceProbability.isPresent()) {
            var oldProbability = this.advanceProbability.get();
            var newProbability = otherSimulateProperty.advanceProbability.get().replicate();

            newProbability.replaceSuper(oldProbability);

            this.advanceProbability = Optional.of(newProbability);
        } else {
            this.advanceProbability = otherSimulateProperty.advanceProbability.map(CalculateValue::replicate).or(() -> this.advanceProbability);
        }
    }

    public <T> void parseAndApplyProbability(DynamicOps<T> ops, T input) {
        CalculateValue calculateValue = CalculateValue.parse(ops, input);
        this.advanceProbability = Optional.of(calculateValue);
    }

    public <T> void parseAndApplyCondition(DynamicOps<T> ops, T input) {
        Condition condition = Condition.parse(ops, input).getOrThrow(true, e->{});
        this.conditions.add(condition);
    }

    static public <T> DataResult<SimulateProperty> parse(DynamicOps<T> ops, T input) {

        SimulateProperty simulateProperty = new SimulateProperty();

        var propertyInfoResult = ops.getMap(input);

        if (propertyInfoResult.error().isPresent()) {
            return returnError(propertyInfoResult);
        }

        MapLike<T> propertyInfo = propertyInfoResult.result().get();

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

        return DataResult.success(simulateProperty);
    }
}
