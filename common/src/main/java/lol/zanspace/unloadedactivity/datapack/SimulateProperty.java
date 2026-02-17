package lol.zanspace.unloadedactivity.datapack;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryLoader;
import net.minecraft.world.level.block.Block;

#if MC_VER >= MC_1_21_11
import net.minecraft.resources.Identifier;
#else
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
#endif

import java.util.*;
import java.util.stream.Collectors;

import static lol.zanspace.unloadedactivity.datapack.SimulationData.returnError;

public class SimulateProperty {
    public Set<String> dependencies = new HashSet<>();
    public Optional<SimulationType> simulationType = Optional.empty();
    public Optional<String> target = Optional.empty();
    public Optional<Integer> maxHeight = Optional.empty();
    public Optional<Boolean> updateNeighbors = Optional.empty();
    public Optional<Boolean> resetOnHeightChange = Optional.empty();
    public Optional<Boolean> keepUpdatingAfterMaxHeight = Optional.empty();
    public Optional<Integer> updateType = Optional.empty();
    public Optional<CalculateValue> advanceProbability = Optional.empty();
    public Optional<Integer> maxValue = Optional.empty();
    public ArrayList<Condition> conditions = new ArrayList<>();
    public Optional<String> buddingDirectionProperty = Optional.empty();
    public Optional<Integer> minWaterValue = Optional.empty();
    public Optional<String> waterloggedProperty = Optional.empty();
    public ArrayList<Direction> ignoreBuddingDirections = new ArrayList<>();
    public Optional<ArrayList< #if MC_VER >= MC_1_21_11 Identifier #else ResourceLocation #endif >> buddingBlocks = Optional.empty();

    public void merge(SimulateProperty otherSimulateProperty) {
        this.simulationType = otherSimulateProperty.simulationType.or(() -> this.simulationType);
        this.target = otherSimulateProperty.target.or(() -> this.target);
        this.maxValue = otherSimulateProperty.maxValue.or(() -> this.maxValue);
        this.maxHeight = otherSimulateProperty.maxHeight.or(() -> this.maxHeight);
        this.dependencies.addAll(otherSimulateProperty.dependencies);
        this.updateType = otherSimulateProperty.updateType.or(() -> this.updateType);
        this.updateNeighbors = otherSimulateProperty.updateNeighbors.or(() -> this.updateNeighbors);
        this.resetOnHeightChange = otherSimulateProperty.resetOnHeightChange.or(() -> this.resetOnHeightChange);
        this.keepUpdatingAfterMaxHeight = otherSimulateProperty.keepUpdatingAfterMaxHeight.or(() -> this.keepUpdatingAfterMaxHeight);
        this.conditions.addAll(otherSimulateProperty.conditions);
        this.ignoreBuddingDirections.addAll(otherSimulateProperty.ignoreBuddingDirections);
        this.minWaterValue = otherSimulateProperty.minWaterValue.or(() -> this.minWaterValue);
        this.waterloggedProperty = otherSimulateProperty.waterloggedProperty.or(() -> this.waterloggedProperty);
        this.buddingDirectionProperty = otherSimulateProperty.buddingDirectionProperty.or(() -> this.buddingDirectionProperty);
        this.buddingBlocks = otherSimulateProperty.buddingBlocks.or(() -> this.buddingBlocks);

        if (otherSimulateProperty.advanceProbability.isPresent() && this.advanceProbability.isPresent()) {
            var oldProbability = this.advanceProbability.get();
            var newProbability = otherSimulateProperty.advanceProbability.get().replicate();

            newProbability.replaceSuper(oldProbability);

            this.advanceProbability = Optional.of(newProbability);
        } else {
            this.advanceProbability = otherSimulateProperty.advanceProbability.map(CalculateValue::replicate).or(() -> this.advanceProbability);
        }
    }

    public void finalize(String targetFallback) {
        if (this.target.isEmpty()) {
            this.target = Optional.of(targetFallback);
        }
    }

    public void throwIfInvalid() {
        if (this.target.isEmpty()) {
            throw new RuntimeException("target has not been set.");
        }

        if (this.simulationType.isEmpty()) {
            throw new RuntimeException("simulation_type has not been set.");
        }

        if (this.advanceProbability.isEmpty()) {
            throw new RuntimeException("advance_probability has not been set.");
        }

        SimulationType simulationType = this.simulationType.get();

        switch (simulationType) {
            case INT_PROPERTY -> {
            }
            case BUDDING -> {

                if (this.buddingDirectionProperty.isEmpty()) {
                    throw new RuntimeException("budding_direction_property has not been set.");
                }

                if (this.buddingBlocks.isEmpty()) {
                    throw new RuntimeException("budding_blocks has not been set.");
                }

                if (this.buddingBlocks.get().isEmpty()) {
                    throw new RuntimeException("budding_blocks must not be empty.");
                }

                for (var buddingBlockId : this.buddingBlocks.get()) {
                    Optional<Block> maybeBlock = Registry.BLOCK.getOptional(buddingBlockId);
                    if (maybeBlock.isEmpty()) {
                        throw new RuntimeException(buddingBlockId + " is not a valid block.");
                    }

                    Block block = maybeBlock.get();

                    String buddingDirectionProperty = this.buddingDirectionProperty.get();

                    {
                        Optional<Property<?>> maybeProperty = block.getProperty(block.defaultBlockState(), buddingDirectionProperty);

                        if (maybeProperty.isEmpty()) {
                            throw new RuntimeException(buddingDirectionProperty + " is not a valid direction property on " + block + ". It doesn't exist.");
                        }

                        Property<?> property = maybeProperty.get();

                        if (property instanceof DirectionProperty directionProperty) {
                            List<Direction> availableDirections = Arrays.stream(Direction.values()).filter(direction -> !this.ignoreBuddingDirections.contains(direction)).toList();
                            List<Direction> possibleDirections = directionProperty.getPossibleValues().stream().toList();
                            for (Direction direction : availableDirections) {
                                if (!possibleDirections.contains(direction)) {
                                    throw new RuntimeException(block + " direction property " + buddingDirectionProperty + " doesn't support the direction " + direction + ". Consider adding it to ignore_budding_directions.");
                                }
                            }
                        } else {
                            throw new RuntimeException(buddingDirectionProperty + " is not a valid direction property on " + block + ". It holds a different type.");
                        }
                    }

                    if (this.waterloggedProperty.isPresent()) {
                        String waterloggedProperty = this.waterloggedProperty.get();
                        Optional<Property<?>> maybeProperty = block.getProperty(block.defaultBlockState(), waterloggedProperty);

                        if (maybeProperty.isEmpty()) {
                            throw new RuntimeException(waterloggedProperty + " is not a valid boolean property on " + block + ". It doesn't exist.");
                        }

                        Property<?> property = maybeProperty.get();

                        if (property instanceof BooleanProperty) {
                            // yay
                        } else {
                            throw new RuntimeException(waterloggedProperty + " is not a valid boolean property on " + block + ". It holds a different type.");
                        }
                    }
                }
            }
            case ACTION -> {
            }
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
            T mapValue = propertyInfo.get("simulation_type");
            if (mapValue != null) {
                DataResult<String> valueResult = ops.getStringValue(mapValue);
                if (valueResult.error().isPresent()) {
                    return returnError(valueResult);
                }

                String simulationTypeString = valueResult.result().get();
                Optional<SimulationType> maybeSimulationType = SimulationType.fromString(simulationTypeString);

                if (maybeSimulationType.isEmpty()) {
                    return returnError(simulationTypeString + " is not a valid simulation type.");
                }

                simulateProperty.simulationType = maybeSimulationType;
            }
        }

        {
            T mapValue = propertyInfo.get("target");
            if (mapValue != null) {
                DataResult<String> valueResult = ops.getStringValue(mapValue);
                if (valueResult.error().isPresent()) {
                    return returnError(valueResult);
                }
                simulateProperty.target = valueResult.result();
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
            T mapValue = propertyInfo.get("ignore_budding_directions");
            if (mapValue != null) {
                var listResult = ops.getStream(mapValue);
                if (listResult.error().isPresent()) {
                    return returnError(listResult);
                }

                for (T ignoredDirection : listResult.result().get().toList()) {
                    var result = Direction.CODEC.decode(ops, ignoredDirection);
                    if (result.result().isEmpty()) {
                        returnError(result);
                    }

                    simulateProperty.ignoreBuddingDirections.add(result.result().get().getFirst());
                }
            }
        }

        {
            T mapValue = propertyInfo.get("min_water_value");
            if (mapValue != null) {
                DataResult<Number> valueResult = ops.getNumberValue(mapValue);
                if (valueResult.error().isPresent()) {
                    return returnError(valueResult);
                }
                simulateProperty.minWaterValue = valueResult.result().map(Number::intValue);
            }
        }

        {
            T mapValue = propertyInfo.get("waterlogged_property");
            if (mapValue != null) {
                DataResult<String> valueResult = ops.getStringValue(mapValue);
                if (valueResult.error().isPresent()) {
                    return returnError(valueResult);
                }
                simulateProperty.waterloggedProperty = valueResult.result();
            }
        }

        {
            T mapValue = propertyInfo.get("budding_direction_property");
            if (mapValue != null) {
                DataResult<String> valueResult = ops.getStringValue(mapValue);
                if (valueResult.error().isPresent()) {
                    return returnError(valueResult);
                }
                simulateProperty.buddingDirectionProperty = valueResult.result();
            }
        }

        {
            T mapValue = propertyInfo.get("budding_blocks");
            if (mapValue != null) {
                var listResult = ops.getStream(mapValue);
                if (listResult.error().isPresent()) {
                    return returnError(listResult);
                }

                ArrayList< ResourceLocation > buddingBlocks = new ArrayList<>();

                for (T buddingBlockId : listResult.result().get().toList()) {
                    var result = ResourceLocation.CODEC.decode(ops, buddingBlockId);
                    if (result.result().isEmpty()) {
                        returnError(result);
                    }

                    buddingBlocks.add(result.result().get().getFirst());
                }

                simulateProperty.buddingBlocks = Optional.of(buddingBlocks);
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
