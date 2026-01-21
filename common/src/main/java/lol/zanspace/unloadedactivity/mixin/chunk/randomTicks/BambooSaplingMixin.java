package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.OccurrencesAndLeftover;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
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
    public double getOdds(ServerLevel level, BlockPos pos) {
        return 1d/3d;
    }
    @Override
    public boolean implementsSimulateRandTicks() {return true;}

    @Override public boolean canSimulateRandTicks(BlockState state, ServerLevel level, BlockPos pos) {
        if (!UnloadedActivity.config.growBamboo) return false;
        if (!level.isEmptyBlock(pos.above())) return false;
        if (level.getRawBrightness(pos.above(), 0) < 9) return false;
        return true;
    }
    @Override public int getMaxHeightUA() {
        return 15;
    }

    @Override
    public void simulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, long timePassed, int randomTickSpeed) {

        double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
        double totalOdds = getOdds(level, pos) * randomPickChance;

        OccurrencesAndLeftover growthInfo = Utils.getOccurrencesAndLeftoverTicks(timePassed, totalOdds, 1, random);

        if (growthInfo.occurrences == 0)
            return;

        this.growBamboo(level, pos);
        BlockPos newPos = pos.above();
        BlockState newState = level.getBlockState(newPos);
        if (newState.getBlock() instanceof #if MC_VER >= MC_1_19_4 BambooStalkBlock #else BambooBlock #endif bamboo) {
            if (bamboo.canSimulateRandTicks(newState, level, newPos) && growthInfo.leftover > 0) {
                bamboo.simulateRandTicks(newState, level, newPos, random, growthInfo.leftover, randomTickSpeed);
            }

        }
    }
}
