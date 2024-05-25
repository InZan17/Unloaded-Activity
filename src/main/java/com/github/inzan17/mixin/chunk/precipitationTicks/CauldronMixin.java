package com.github.inzan17.mixin.chunk.precipitationTicks;

import com.github.inzan17.UnloadedActivity;
import com.github.inzan17.Utils;
import net.minecraft.block.*;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

import static java.lang.Math.min;

@Mixin(CauldronBlock.class)
public abstract class CauldronMixin extends AbstractCauldronBlock {

    #if MC_VER >= MC_1_20_4
    public CauldronMixin(Settings settings, CauldronBehavior.CauldronBehaviorMap behaviorMap) {
        super(settings, behaviorMap);
    }
    #else
    public CauldronMixin(Settings settings, Map<Item, CauldronBehavior> behaviorMap) {
        super(settings, behaviorMap);
    }
    #endif

    @Shadow @Final private static float FILL_WITH_RAIN_CHANCE;
    @Shadow @Final private static float FILL_WITH_SNOW_CHANCE;

    @Override
    public boolean implementsSimulatePrecTicks() {
        return true;
    }


    public float getFillOdds(Biome.Precipitation precipitation) {
        if (precipitation == Biome.Precipitation.RAIN) {
            return FILL_WITH_RAIN_CHANCE;
        } else if (precipitation == Biome.Precipitation.SNOW) {
            return FILL_WITH_SNOW_CHANCE;
        } else {
            return 0.0F;
        }
    }

    @Override
    public boolean canSimulatePrecTicks(BlockState state, ServerWorld world, BlockPos pos, long timeInWeather, Biome.Precipitation precipitation) {
        if (!UnloadedActivity.instance.config.weatherFillCauldron) return false;
        if (timeInWeather == 0) return false;
        if (getFillOdds(precipitation) == 0.0F) return false;
        return true;
    }

    @Override
    public void simulatePrecTicks(BlockState state, ServerWorld world, BlockPos pos, long timeInWeather, long timePassed, Biome.Precipitation precipitation, double precipitationPickChance) {

        int maxCauldronLevel = LeveledCauldronBlock.MAX_LEVEL;

        double totalOdds = precipitationPickChance*getFillOdds(precipitation);

        int fill = Utils.getOccurrences(timeInWeather, totalOdds, maxCauldronLevel, world.random);

        if (fill == 0)
            return;

        if (precipitation == Biome.Precipitation.RAIN) {
            world.setBlockState(pos, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, fill));
        } else {
            world.setBlockState(pos, Blocks.POWDER_SNOW_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, fill));
        }

        world.emitGameEvent(null, GameEvent.BLOCK_CHANGE, pos);
    }
}
