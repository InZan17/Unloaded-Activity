package lol.zanspace.unloadedactivity.mixin.chunk.precipitationTicks;

import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(LiquidBlock.class)
public abstract class LiquidMixin extends Block implements BucketPickup {

    public LiquidMixin(Properties properties) {
        super(properties);
    }

    @Override
    public boolean implementsSimulatePrecTicks() {
        return true;
    }

    @Override
    public boolean canSimulatePrecTicks(BlockState state, ServerLevel level, BlockPos pos, long timeInWeather, Biome.Precipitation precipitation) {
        if (!UnloadedActivity.config.waterFreezing) return false;
        Biome biome = level.getBiome(pos.above()).value();
        if (!biome.shouldFreeze(level, pos)) return false;
        return true;
    }

    @Override
    public void simulatePrecTicks(BlockState state, ServerLevel level, BlockPos pos, long timeInWeather, long timePassed, Biome.Precipitation precipitation, double precipitationPickChance) {

        int makeIce = Utils.getOccurrencesBinomial(timePassed, precipitationPickChance, 1, level.random);

        if (makeIce == 0)
            return;

        level.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
    }
}