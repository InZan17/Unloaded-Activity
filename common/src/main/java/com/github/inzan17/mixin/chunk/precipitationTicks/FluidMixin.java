package com.github.inzan17.mixin.chunk.precipitationTicks;

import com.github.inzan17.UnloadedActivity;
import com.github.inzan17.Utils;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;

import static java.lang.Math.min;

@Mixin(FluidBlock.class)
public abstract class FluidMixin extends Block implements FluidDrainable {

    public FluidMixin(Settings settings) {
        super(settings);
    }

    @Override
    public boolean implementsSimulatePrecTicks() {
        return true;
    }

    @Override
    public boolean canSimulatePrecTicks(BlockState state, ServerWorld world, BlockPos pos, long timeInWeather, Biome.Precipitation precipitation) {
        if (!UnloadedActivity.config.waterFreezing) return false;
        Biome biome = world.getBiome(pos.up()).value();
        if (!biome.canSetIce(world, pos)) return false;
        return true;
    }

    @Override
    public void simulatePrecTicks(BlockState state, ServerWorld world, BlockPos pos, long timeInWeather, long timePassed, Biome.Precipitation precipitation, double precipitationPickChance) {

        int makeIce = Utils.getOccurrences(timePassed, precipitationPickChance, 1, world.random);

        if (makeIce == 0)
            return;

        world.setBlockState(pos, Blocks.ICE.getDefaultState());
    }
}