package com.github.inzan123.mixin.chunk.precipitationTicks;

import com.github.inzan123.UnloadedActivity;
import com.github.inzan123.Utils;
import com.github.inzan123.mixin.CauldronBlockInvoker;
import net.minecraft.block.*;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.function.Predicate;

import static java.lang.Math.min;

@Mixin(LeveledCauldronBlock.class)
public abstract class LeveledCauldronMixin extends AbstractCauldronBlock {
    public LeveledCauldronMixin(Settings settings, CauldronBehavior.CauldronBehaviorMap behaviorMap) {
        super(settings, behaviorMap);
    }

    @Shadow @Final private Biome.Precipitation precipitation;

    @Override
    public boolean implementsSimulatePrecTicks() {
        return true;
    }


    public float getFillOdds(Biome.Precipitation precipitation) {
        if (precipitation == Biome.Precipitation.RAIN) {
            return CauldronBlockInvoker.getRainFIllChance();
        } else if (precipitation == Biome.Precipitation.SNOW) {
            return CauldronBlockInvoker.getSnowFIllChance();
        } else {
            return 0.0F;
        }
    }

    @Override
    public boolean canSimulatePrecTicks(BlockState state, ServerWorld world, BlockPos pos, long timeInWeather, Biome.Precipitation precipitation) {
        if (!UnloadedActivity.instance.config.weatherFillCauldron) return false;
        if (timeInWeather == 0) return false;
        if (getFillOdds(precipitation) == 0.0F) return false;
        if (precipitation != this.precipitation) return false;
        if (state.get(LeveledCauldronBlock.LEVEL) == LeveledCauldronBlock.MAX_LEVEL) return false;
        return true;
    }

    @Override
    public void simulatePrecTicks(BlockState state, ServerWorld world, BlockPos pos, long timeInWeather, long timePassed, Biome.Precipitation precipitation, double precipitationPickChance) {

        int maxCauldronLevel = LeveledCauldronBlock.MAX_LEVEL;
        int currentCauldronLevel = state.get(LeveledCauldronBlock.LEVEL);
        int levelDifference = maxCauldronLevel-currentCauldronLevel;

        double totalOdds = precipitationPickChance*getFillOdds(precipitation);

        int fill = Utils.getOccurrences(timeInWeather, totalOdds, levelDifference, world.random);

        if (fill == 0)
            return;

        BlockState newState = state.with(LeveledCauldronBlock.LEVEL, currentCauldronLevel+fill);
        world.setBlockState(pos, newState);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(newState));
    }
}
