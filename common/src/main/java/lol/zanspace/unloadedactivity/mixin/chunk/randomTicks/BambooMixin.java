package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.OccurrencesAndDuration;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import lol.zanspace.unloadedactivity.datapack.SimulationData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

#if MC_VER >= MC_1_19_4
import net.minecraft.world.level.block.BambooStalkBlock;
#else
import net.minecraft.world.level.block.BambooBlock;
#endif

#if MC_VER >= MC_1_19_4
@Mixin(BambooStalkBlock.class)
#else
@Mixin(BambooBlock.class)
#endif
public abstract class BambooMixin extends Block implements BonemealableBlock {

    public BambooMixin(Properties properties) {
        super(properties);
    }

    /*

    @Shadow @Final public static IntegerProperty STAGE;

    @Override public double getOdds(ServerLevel level, BlockState state, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {return 1d/3d;}

    @Override
    public boolean implementsSimulateRandTicks() {return true;}

    @Override
    public boolean canSimulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        if (!UnloadedActivity.config.growBamboo) return false;
        if (!level.isEmptyBlock(pos.above())) return false;
        if (level.getRawBrightness(pos.above(), 0) < 9) return false;
        if (state.getValue(STAGE) == 1) return false;
        return true;
    }
    @Override public int getMaxHeightUA() {
        return 15;
    }

    @Shadow
    protected int getHeightBelowUpToMax(BlockGetter blockGetter, BlockPos pos) {
        return 0;
    }

    @Shadow protected abstract int getHeightAboveUpToMax(BlockGetter blockGetter, BlockPos pos);

    @Unique
    private int countAirAboveUpToMax(BlockGetter blockGetter, BlockPos pos, int maxCount) {
        int i;
        for (i = 0; i < maxCount && blockGetter.getBlockState(pos.above(i + 1)).isAir(); ++i) {
        }
        return i;
    }
    @Override
    public @Nullable Triple<BlockState, OccurrencesAndDuration, BlockPos> simulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName, RandomSource random, long timePassed, int randomTickSpeed, boolean calculateDuration) {

        int height = getHeightBelowUpToMax(level, pos);

        if (height >= getMaxHeightUA())
            return null;

        int heightDifference = getMaxHeightUA() - height;
        int maxGrowth = this.countAirAboveUpToMax(level,pos, heightDifference);

        double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
        double totalOdds = getOdds(level, state, pos, simulateProperty, propertyName) * randomPickChance;

        var result = Utils.getOccurrences(timePassed, totalOdds, maxGrowth, calculateDuration, random);


        for(int i=0;i<result.occurrences();i++) {
            this.performBonemeal(level, random, pos, state);

            if (i == result.occurrences() - 1)
                return null;

            int grew = this.getHeightAboveUpToMax(level, pos);

            if (grew == 0)
                return null;

            pos = pos.above(grew);
            state = level.getBlockState(pos);

            if (!this.canSimulateRandTicks(state, level, pos, simulateProperty, propertyName))
                return null;
        }
        return null;
    }

     */
}