package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.OccurrencesAndDuration;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import lol.zanspace.unloadedactivity.datapack.SimulationData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

#if MC_VER >= MC_1_19_4
import net.minecraft.world.level.block.BambooStalkBlock;
#else
import net.minecraft.world.level.block.BambooBlock;
#endif

@Mixin(BambooSaplingBlock.class)
public abstract class BambooSaplingMixin extends Block {

    public BambooSaplingMixin(Properties properties) {
        super(properties);
    }

    @Shadow protected void growBamboo(Level level, BlockPos pos) {}

    @Override
    public @Nullable Triple<BlockState, OccurrencesAndDuration, BlockPos> simulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName, RandomSource random, long timePassed, int randomTickSpeed, boolean calculateDuration) {
        if (propertyName.equals("@grow_bamboo")) {

            int maxHeight = simulateProperty.maxHeight.orElse(15);

            if (maxHeight <= 1 || !level.isEmptyBlock(pos.above()))
                return Triple.of(state, OccurrencesAndDuration.empty(), pos);

            double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
            double totalOdds = getOdds(level, state, pos, simulateProperty, propertyName) * randomPickChance;

            var result = Utils.getOccurrences(timePassed, totalOdds, 1, true, random);

            if (result.occurrences() != 0) {
                this.growBamboo(level, pos);
                pos = pos.above();
                state = level.getBlockState(pos);
            }

            return Triple.of(state, result, pos);
        }
        return super.simulateRandTicks(state, level, pos, simulateProperty, propertyName, random, timePassed, randomTickSpeed, calculateDuration);
    }
}
