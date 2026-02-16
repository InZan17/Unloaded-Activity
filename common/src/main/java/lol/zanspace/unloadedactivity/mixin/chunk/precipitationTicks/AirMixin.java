package lol.zanspace.unloadedactivity.mixin.chunk.precipitationTicks;

import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

#if MC_VER >= MC_1_21_11
import net.minecraft.world.level.gamerules.GameRules;
#else
import net.minecraft.world.level.GameRules;
#endif

import static java.lang.Math.min;

@Mixin(AirBlock.class)
public abstract class AirMixin extends Block {

    public AirMixin(Properties properties) {
        super(properties);
    }

    @Unique
    private int getMaxSnowHeight(ServerLevel level) {
        return
        #if MC_VER >= MC_1_21_11
            min(level.getGameRules().get(GameRules.MAX_SNOW_ACCUMULATION_HEIGHT), SnowLayerBlock.MAX_HEIGHT)
        #elif MC_VER >= MC_1_19_4
            min(level.getGameRules().getInt(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT), SnowLayerBlock.MAX_HEIGHT)
        #else
            1
        #endif;
    }

    @Override
    public boolean implementsSimulatePrecTicks() {
        return true;
    }

    @Override
    public boolean canSimulatePrecTicks(BlockState state, ServerLevel level, BlockPos pos, long timeInWeather, Biome.Precipitation precipitation) {
        if (!UnloadedActivity.config.accumulateSnow) return false;
        if (timeInWeather == 0) return false;
        int maxSnowHeight = getMaxSnowHeight(level);
        if (maxSnowHeight <= 0) return false;
        Biome biome = level.getBiome(pos).value();
        if (!biome.shouldSnow(level, pos)) return false;
        return true;
    }

    @Override
    public void simulatePrecTicks(BlockState state, ServerLevel level, BlockPos pos, long timeInWeather, long timePassed, Biome.Precipitation precipitation, double precipitationPickChance) {

        int maxSnowHeight = getMaxSnowHeight(level);

        int layers = Utils.getOccurrencesBinomial(timeInWeather, precipitationPickChance, min(maxSnowHeight, SnowLayerBlock.MAX_HEIGHT), level.random);

        if (layers == 0)
            return;

        level.setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers));
    }
}
