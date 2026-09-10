package dev.moono.unloadedactivity.impl.number_fetchers;

import dev.moono.unloadedactivity.api.context.WeatherDependantContext;
import dev.moono.unloadedactivity.api.number_fetcher.WeatherDependantNumberFetcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

public class IsRainingAtValue implements WeatherDependantNumberFetcher {
    Vec3i offset;

    public IsRainingAtValue() {
        this.offset = Vec3i.ZERO;
    }

    public IsRainingAtValue(Vec3i offset) {
        this.offset = offset;
    }

    @Override
    public Number evaluate(WeatherDependantContext context) {
        LevelReader level = context.getLevel();
        BlockPos pos = context.getBlockPos().offset(this.offset);
        if (!context.isRaining()) {
            return 0;
        } else if (!level.canSeeSky(pos)) {
            return 0;
        } else if (level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY() > pos.getY()) {
            return 0;
        } else {
            Biome biome = level.getBiome(pos).value();

            #if MC_VER >= MC_1_21_3
            Biome.Precipitation precipitation = biome.getPrecipitationAt(pos, level.getSeaLevel());
            #elif MC_VER >= MC_1_19_4
            Biome.Precipitation precipitation = biome.getPrecipitationAt(pos);
            #else
            Biome.Precipitation precipitation = biome.getPrecipitation();
            #endif

            return precipitation == Biome.Precipitation.RAIN ? 1 : 0;
        }
    }
}
