package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.OccurrencesAndLeftover;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

#if MC_VER >= MC_1_19_4
import net.minecraft.world.level.block.BambooStalkBlock;

import java.util.Optional;
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
    public double getOdds(ServerLevel level, BlockState state, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        return 1d/3d;
    }
    @Override
    public boolean implementsSimulateRandTicks() {return true;}

    @Override public boolean canSimulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        if (!UnloadedActivity.config.growBamboo) return false;
        if (!level.isEmptyBlock(pos.above())) return false;
        if (level.getRawBrightness(pos.above(), 0) < 9) return false;
        return true;
    }
    @Override public int getMaxHeightUA() {
        return 15;
    }

    @Override
    public BlockState simulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName, RandomSource random, long timePassed, int randomTickSpeed, Optional<OccurrencesAndLeftover> returnLeftoverTicks) {

        double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
        double totalOdds = getOdds(level, state, pos, simulateProperty, propertyName) * randomPickChance;

        OccurrencesAndLeftover growthInfo = Utils.getOccurrencesAndLeftoverTicks(timePassed, totalOdds, 1, random);

        if (growthInfo.occurrences == 0)
            return state;

        this.growBamboo(level, pos);
        BlockPos newPos = pos.above();
        BlockState newState = level.getBlockState(newPos);
        if (newState.getBlock() instanceof #if MC_VER >= MC_1_19_4 BambooStalkBlock #else BambooBlock #endif bamboo) {
            if (growthInfo.leftover > 0 && bamboo.canSimulateRandTicks(newState, level, newPos, simulateProperty, propertyName)) {
                return bamboo.simulateRandTicks(newState, level, newPos, simulateProperty, propertyName, random, growthInfo.leftover, randomTickSpeed, returnLeftoverTicks);
            }
        }

        return state;
    }
}
