package lol.zanspace.unloadedactivity.datapack.condition;

import lol.zanspace.unloadedactivity.datapack.Comparison;
import lol.zanspace.unloadedactivity.datapack.Condition;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

public class LocalBrightnessAboveCondition  implements Condition {
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

    public LocalBrightnessAboveCondition(Comparison comparison, int targetBrightness) {
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