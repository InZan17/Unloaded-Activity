package lol.zanspace.unloadedactivity.datapack.calculate_value;

import lol.zanspace.unloadedactivity.Utils;
import lol.zanspace.unloadedactivity.datapack.CalculateValue;
import lol.zanspace.unloadedactivity.mixin.CropBlockInvoker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

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
        public boolean isSuper() {
            return true;
        }

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
            case "super" -> {
                return Optional.of(SUPER);
            }
        }
        return Optional.empty();
    };
}