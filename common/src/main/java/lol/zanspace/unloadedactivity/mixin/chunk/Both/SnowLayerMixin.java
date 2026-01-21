package lol.zanspace.unloadedactivity.mixin.chunk.Both;

import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

#if MC_VER >= MC_1_21_11
import net.minecraft.world.level.gamerules.GameRules;
#else
import net.minecraft.world.level.GameRules;
#endif

@Mixin(SnowLayerBlock.class)
public abstract class SnowLayerMixin extends Block {

    public SnowLayerMixin(Properties properties) {
        super(properties);
    }

    @Override
    public boolean implementsSimulatePrecTicks() {
        return true;
    }
    @Override
    public boolean implementsSimulateRandTicks() {
        return true;
    }

    @Override
    public double getOdds(ServerLevel level, BlockPos pos) {
        return 1;
    }

    @Override
    public boolean canSimulateRandTicks(BlockState state, ServerLevel level, BlockPos pos) {
        if (!UnloadedActivity.config.meltSnow) return false;
        if (level.getBrightness(LightLayer.BLOCK, pos) <= 11) return false;
        return true;
    }

    @Override
    public void simulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, long timePassed, int randomTickSpeed) {

        double pickOdds = Utils.getRandomPickOdds(randomTickSpeed)*this.getOdds(level,pos);;

        if (Utils.getOccurrences(timePassed, pickOdds, 1, random) != 0) {
            dropResources(state, level, pos);
            level.removeBlock(pos, false);
        }

    }

    @Override
    public boolean canSimulatePrecTicks(BlockState state, ServerLevel level, BlockPos pos, long timeInWeather, Biome.Precipitation precipitation) {
        if (!UnloadedActivity.config.accumulateSnow) return false;
        if (timeInWeather == 0) return false;
        int maxSnowHeight = #if MC_VER >= MC_1_21_11
            Math.min(level.getGameRules().get(GameRules.MAX_SNOW_ACCUMULATION_HEIGHT), SnowLayerBlock.MAX_HEIGHT)
        #elif MC_VER >= MC_1_19_4
            Math.min(level.getGameRules().getInt(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT), SnowLayerBlock.MAX_HEIGHT)
        #else
            1
        #endif;
        if (maxSnowHeight <= state.getValue(SnowLayerBlock.LAYERS)) return false;
        Biome biome = level.getBiome(pos).value();
        if (!biome.shouldSnow(level, pos)) return false;
        return true;
    }

    @Override
    public void simulatePrecTicks(BlockState state, ServerLevel level, BlockPos pos, long timeInWeather, long timePassed, Biome.Precipitation precipitation, double precipitationPickChance) {

        int maxSnowHeight =
        #if MC_VER >= MC_1_21_11
            Math.min(level.getGameRules().get(GameRules.MAX_SNOW_ACCUMULATION_HEIGHT), SnowLayerBlock.MAX_HEIGHT)
        #elif MC_VER >= MC_1_19_4
            Math.min(level.getGameRules().getInt(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT), SnowLayerBlock.MAX_HEIGHT)
        #else
            1
        #endif;

        int currentSnowHeight = state.getValue(SnowLayerBlock.LAYERS);

        int heightDifference = maxSnowHeight-currentSnowHeight;

        int newLayers = Utils.getOccurrences(timeInWeather, precipitationPickChance, heightDifference, level.random);

        if (newLayers == 0)
            return;

        level.setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, currentSnowHeight+newLayers));
    }
}
