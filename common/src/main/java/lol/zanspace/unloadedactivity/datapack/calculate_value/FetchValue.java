package lol.zanspace.unloadedactivity.datapack.calculate_value;

import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import lol.zanspace.unloadedactivity.datapack.CalculateValue;
import lol.zanspace.unloadedactivity.mixin.CropBlockInvoker;
import lol.zanspace.unloadedactivity.mixin.GameRulesAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Objects;
import java.util.Optional;

import static lol.zanspace.unloadedactivity.interfaces.SimulateChunkBlocks.getProperty;

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

    BLOCK_BRIGHTNESS {
        @Override
        public double calculateValue(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {
            return level.getBrightness(LightLayer.BLOCK, pos);
        }
    },

    BLOCK_BRIGHTNESS_ABOVE {
        @Override
        public double calculateValue(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {
            return level.getBrightness(LightLayer.BLOCK, pos.above());
        }
    },

    IS_SAND_BELOW {
        @Override
        public double calculateValue(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {
            return level.getBlockState(pos.below()).is(BlockTags.SAND) ? 1 : 0;
        }
    },

    SHOULD_SNOW {
        @Override
        public double calculateValue(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {
            Biome biome = level.getBiome(pos).value();
            return biome.shouldSnow(level, pos) ? 1 : 0;
        }
    },

    MAX_SNOW_HEIGHT {
        @Override
        public double calculateValue(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {
            int maxSnowHeight = #if MC_VER >= MC_1_21_11
                level.getGameRules().get(GameRules.MAX_SNOW_ACCUMULATION_HEIGHT)
            #elif MC_VER >= MC_1_19_4
                level.getGameRules().getInt(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT)
            #else
                1
            #endif;

            return Math.min(maxSnowHeight, SnowLayerBlock.MAX_HEIGHT);
        }
    },

    SUPER {
        @Override
        public boolean isSuper() {
            return true;
        }

        @Override
        public double calculateValue(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {
            return 1;
        }
    };

    @Override
    public boolean canBeAffectedByWeather() {
        return false;
    }

    @Override
    public boolean canBeAffectedByTime() {
        return false;
    }

    @Override
    public long getNextValueSwitchDuration(ServerLevel level, BlockState state, BlockPos pos, long currentTime, boolean isRaining, boolean isThundering) {
        return Long.MAX_VALUE;
    }

    @Override
    public CalculateValue replicate() {
        return this;
    }

    @Override
    public void replaceSuper(CalculateValue superValue) {}

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
            case "block_brightness" -> {
                return Optional.of(BLOCK_BRIGHTNESS);
            }
            case "block_brightness_above" -> {
                return Optional.of(BLOCK_BRIGHTNESS_ABOVE);
            }
            case "is_sand_below" -> {
                return Optional.of(IS_SAND_BELOW);
            }
            case "should_snow" -> {
                return Optional.of(SHOULD_SNOW);
            }
            case "max_snow_height" -> {
                return Optional.of(MAX_SNOW_HEIGHT);
            }
            case "super" -> {
                return Optional.of(SUPER);
            }
        }

        return Optional.empty();
    };
}