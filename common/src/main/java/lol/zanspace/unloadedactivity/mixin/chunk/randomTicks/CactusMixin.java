package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.OccurrencesAndLeftover;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import lol.zanspace.unloadedactivity.datapack.SimulationData;
import net.minecraft.world.level.block.Block;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(CactusBlock.class)
public abstract class CactusMixin extends net.minecraft.world.level.block.Block {

    public CactusMixin(Properties properties) {
        super(properties);
    }

    @Shadow @Final public static IntegerProperty AGE;
    @Shadow @Final public static int MAX_AGE;

    @Override
    public double getOdds(ServerLevel level, BlockState state, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        return 1;
    }
    @Override
    public boolean implementsSimulateRandTicks() {return true;}

    @Override public boolean canSimulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        if (!UnloadedActivity.config.growCactus) return false;
        if (!level.isEmptyBlock(pos.above())) return false;
        return true;
    }
    @Override public int getCurrentAgeUA(net.minecraft.world.level.block.state.BlockState state) {
        return state.getValue(AGE);
    }
    @Override public int getMaxAgeUA() {
        return MAX_AGE;
    }
    @Override public int getMaxHeightUA() {return 2;}

    @Unique
    private int countAirAbove(BlockGetter blockGetter, BlockPos pos, int maxCount) {
        int i;
        for (i = 0; i < maxCount && blockGetter.getBlockState(pos.above(i + 1)).isAir(); ++i) {
        }
        return i;
    }
    @Override
    public BlockState simulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName, RandomSource random, long timePassed, int randomTickSpeed, Optional<OccurrencesAndLeftover> returnLeftoverTicks) {

        int height = 0;
        while (level.getBlockState(pos.below(height+1)).is(this)) {
            ++height;
        }

        if (height >= getMaxHeightUA())
            return state;

        int age = getCurrentAgeUA(state);
        int maxAge = getMaxAgeUA()+1; // add one for when growing

        int heightDifference = getMaxHeightUA()-height-1;

        int maxGrowth = countAirAbove(level, pos, heightDifference);
        int remainingAge = maxAge - age + maxGrowth*maxAge;

        double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
        double totalOdds = getOdds(level, state, pos, simulateProperty, propertyName) * randomPickChance;

        int growthAmount = Utils.getOccurrences(timePassed, totalOdds, remainingAge, random);

        if (growthAmount == 0)
            return state;

        growthAmount += age;

        int growBlocks = growthAmount/16;
        int ageRemainder = growthAmount % 16;

        for (int i=0;i<growBlocks;i++) {

            BlockPos newPos = pos.above(1);

            if (!level.getBlockState(newPos).isAir()) {
                return null;
            }

            BlockState newState = this.defaultBlockState();

            if (i+1==growBlocks)
                newState = newState.setValue(AGE, ageRemainder);

            level.setBlockAndUpdate(newPos, newState);

            //For some reason this doesn't work??
            //world.updateNeighbor(newState, newPos, this, pos, false);
            //I really want to figure out why tho.

            #if MC_VER == MC_1_19_2
            level.scheduleTick(newPos, newState.getBlock(), 1);
            #else
            level.scheduleTick(newPos, newState.getBlock(), 1);
            #endif
            pos = newPos;
        }

        if (growBlocks != 0) {
            state = state.setValue(AGE, 0);
        } else {
            state = state.setValue(AGE, ageRemainder);
        }
        level.setBlock(pos, state, Block.UPDATE_INVISIBLE);
        return state;
    }
}