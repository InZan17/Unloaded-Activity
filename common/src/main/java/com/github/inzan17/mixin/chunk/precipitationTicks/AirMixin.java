package com.github.inzan17.mixin.chunk.precipitationTicks;

import com.github.inzan17.UnloadedActivity;
import com.github.inzan17.Utils;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;

import static java.lang.Math.min;

@Mixin(AirBlock.class)
public abstract class AirMixin extends Block {
    public AirMixin(Settings settings) {
        super(settings);
    }

    @Override
    public boolean implementsSimulatePrecTicks() {
        return true;
    }

    @Override
    public boolean canSimulatePrecTicks(BlockState state, ServerWorld world, BlockPos pos, long timeInWeather, Biome.Precipitation precipitation) {
        if (!UnloadedActivity.config.accumulateSnow) return false;
        if (timeInWeather == 0) return false;
        int maxSnowHeight = #if MC_VER >= MC_1_19_4
            min(world.getGameRules().getInt(GameRules.SNOW_ACCUMULATION_HEIGHT), SnowBlock.MAX_LAYERS)
        #else
                1
        #endif;
        if (maxSnowHeight <= 0) return false;
        Biome biome = world.getBiome(pos).value();
        if (!biome.canSetSnow(world, pos)) return false;
        return true;
    }

    @Override
    public void simulatePrecTicks(BlockState state, ServerWorld world, BlockPos pos, long timeInWeather, long timePassed, Biome.Precipitation precipitation, double precipitationPickChance) {

        int maxSnowHeight = #if MC_VER >= MC_1_19_4
            min(world.getGameRules().getInt(GameRules.SNOW_ACCUMULATION_HEIGHT), SnowBlock.MAX_LAYERS)
        #else
            1
        #endif;

        int layers = Utils.getOccurrences(timeInWeather, precipitationPickChance, min(maxSnowHeight, SnowBlock.MAX_LAYERS), world.random);

        if (layers == 0)
            return;

        world.setBlockState(pos, Blocks.SNOW.getDefaultState().with(SnowBlock.LAYERS, layers));
    }
}
