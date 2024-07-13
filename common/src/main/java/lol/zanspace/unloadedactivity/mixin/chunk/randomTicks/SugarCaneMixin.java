package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SugarCaneBlock.class)
public abstract class SugarCaneMixin extends Block {

    @Shadow @Final public static IntProperty AGE;

    public SugarCaneMixin(Settings settings) {
        super(settings);
    }

    @Override
    public double getOdds(ServerWorld world, BlockPos pos) {
        return 1;
    }
    @Override
    public boolean implementsSimulateRandTicks() {return true;}

    @Override public boolean canSimulateRandTicks(BlockState state, ServerWorld world, BlockPos pos) {
        if (!UnloadedActivity.config.growSugarCane) return false;
        if (!world.isAir(pos.up())) return false;
        return true;
    }
    @Override public int getCurrentAgeUA(BlockState state) {
        return state.get(AGE);
    }
    @Override public int getMaxAgeUA() {
        return 15;
    }
    @Override public int getMaxHeightUA() {return 2;}

    public int countAirAbove(BlockView world, BlockPos pos, int maxCount) {
        int i;
        for (i = 0; i < maxCount && world.getBlockState(pos.up(i + 1)).isAir(); ++i) {
        }
        return i;
    }
    @Override
    public void simulateRandTicks(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        int height = 0;
        while (world.getBlockState(pos.down(height+1)).isOf(this)) {
            ++height;
        }

        if (height >= getMaxHeightUA())
            return;

        int age = getCurrentAgeUA(state);
        int maxAge = getMaxAgeUA()+1; // add one for when growing

        int heightDifference = getMaxHeightUA()-height-1;

        int maxGrowth = countAirAbove(world, pos, heightDifference);
        int remainingAge = maxAge - age + maxGrowth*maxAge;

        double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
        double totalOdds = getOdds(world, pos) * randomPickChance;

        int growthAmount = Utils.getOccurrences(timePassed, totalOdds, remainingAge, random);


        if (growthAmount == 0)
            return;

        growthAmount += age;

        int growBlocks = growthAmount/16;
        int ageRemainder = growthAmount % 16;

        if (growBlocks != 0) {
            state = state.with(AGE, 0);
        } else {
            state = state.with(AGE, ageRemainder);
        }
        world.setBlockState(pos, state, Block.NO_REDRAW);

        for (int i=0;i<growBlocks;i++) {

            BlockPos placementPos = pos.up(i+1);

            if (!world.getBlockState(placementPos).isAir()) {
                return;
            }

            world.setBlockState(placementPos, this.getDefaultState());

            if (i+1==growBlocks)
                world.setBlockState(placementPos, this.getDefaultState().with(AGE, ageRemainder));
        }
    }
}