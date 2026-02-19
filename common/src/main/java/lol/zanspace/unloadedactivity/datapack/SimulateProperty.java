package lol.zanspace.unloadedactivity.datapack;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.interfaces.SimulateChunkBlocks;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryLoader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

#if MC_VER >= MC_1_21_11
import net.minecraft.resources.Identifier;
#else
import net.minecraft.resources.ResourceLocation;
#endif

import java.util.*;

import static lol.zanspace.unloadedactivity.interfaces.SimulateChunkBlocks.getProperty;


public class SimulateProperty {
    public String target;
    public SimulationType simulationType;
    public CalculateValue advanceProbability;

    public List<String> dependencies;
    public List<Condition> conditions;
    public Optional<Integer> maxValue;
    public Optional<Integer> maxHeight;
    public Optional<String> waterloggedProperty;
    public List<Direction> ignoreBuddingDirections;
    public Optional<String> buddingDirectionProperty;

    public Optional<Block> blockReplacement;

    public int updateType;
    public boolean updateNeighbors;
    public boolean resetOnHeightChange;
    public boolean keepUpdatingAfterMaxHeight;
    public boolean dropsResources;
    public int minWaterValue;

    public List<Block> buddingBlocks;

    public SimulateProperty(IncompleteSimulateProperty incomplete, String targetFallback) {
        // Required fields for all simulation types
        this.target = incomplete.target.orElse(targetFallback);

        if (incomplete.simulationType.isEmpty())
            throw new RuntimeException("simulation_type has not been set.");

        this.simulationType = incomplete.simulationType.get();

        if (incomplete.advanceProbability.isEmpty())
            throw new RuntimeException("advance_probability has not been set.");

        this.advanceProbability = incomplete.advanceProbability.get();

        // Simple transfer
        this.dependencies = incomplete.dependencies.stream().toList();
        this.conditions = incomplete.conditions.stream().toList();
        this.maxValue = incomplete.maxValue;
        this.maxHeight = incomplete.maxHeight;
        this.waterloggedProperty = incomplete.waterloggedProperty;
        this.ignoreBuddingDirections = incomplete.ignoreBuddingDirections.stream().toList();
        this.buddingDirectionProperty = incomplete.buddingDirectionProperty;

        // Convert types.
        this.blockReplacement = incomplete.blockReplacement.map(id -> {
            Optional<Block> maybeBlock = Registry.BLOCK.getOptional(id);
            if (maybeBlock.isEmpty()) {
                throw new RuntimeException(id + " is not a valid block.");
            }
            return maybeBlock.get();
        });

        // Default values for optional fields with defaults.
        this.updateType = incomplete.updateType.orElse(Block.UPDATE_ALL);
        this.updateNeighbors = incomplete.updateNeighbors.orElse(false);
        this.resetOnHeightChange = incomplete.resetOnHeightChange.orElse(true);
        this.keepUpdatingAfterMaxHeight = incomplete.keepUpdatingAfterMaxHeight.orElse(true);
        this.dropsResources = incomplete.dropsResources.orElse(true);
        this.minWaterValue = incomplete.minWaterValue.orElse(0);

        // Default value for required fields depending on simulationType.
        this.buddingBlocks = List.of();

        switch (this.simulationType) {
            case INT_PROPERTY -> {
            }
            case BUDDING -> {
                if (incomplete.buddingBlocks.isEmpty()) {
                    throw new RuntimeException("budding_blocks has not been set.");
                }

                if (incomplete.buddingBlocks.get().isEmpty()) {
                    throw new RuntimeException("budding_blocks must not be empty.");
                }

                ArrayList<Block> buddingBlocksList = new ArrayList<>();

                for (var buddingBlockId : incomplete.buddingBlocks.get()) {
                    Optional<Block> maybeBlock = Registry.BLOCK.getOptional(buddingBlockId);
                    if (maybeBlock.isEmpty()) {
                        throw new RuntimeException(buddingBlockId + " is not a valid block.");
                    }

                    Block block = maybeBlock.get();


                    if (this.buddingDirectionProperty.isPresent()) {
                        String buddingDirectionProperty = this.buddingDirectionProperty.get();
                        Optional<Property<?>> maybeProperty = getProperty(block.defaultBlockState(), buddingDirectionProperty);

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
                        Optional<Property<?>> maybeProperty = getProperty(block.defaultBlockState(), waterloggedProperty);

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

                    buddingBlocksList.add(block);
                }

                this.buddingBlocks = buddingBlocksList.stream().toList();
            }
            case ACTION -> {
            }
        }
    }

    public boolean isBudding() {
        return this.simulationType == SimulationType.BUDDING;
    }

    public boolean isBudding(String target) {
        return this.simulationType == SimulationType.BUDDING && this.target.equals(target);
    }

    public boolean isDecay() {
        return this.simulationType == SimulationType.DECAY;
    }

    public boolean isDecay(String target) {
        return this.simulationType == SimulationType.DECAY && this.target.equals(target);
    }

    public boolean isAction() {
        return this.simulationType == SimulationType.ACTION;
    }

    public boolean isAction(String target) {
        return this.simulationType == SimulationType.ACTION && this.target.equals(target);
    }
}
