package com.github.inzan123.mixin.blocks;

import com.github.inzan123.UnloadedActivity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CactusBlock.class)
public abstract class CactusMixin extends Block {

    @Shadow @Final public static IntProperty AGE;
    @Shadow @Final public static int MAX_AGE;

    public CactusMixin(Settings settings) {
        super(settings);
    }

    @Override
    public double getOdds(ServerWorld world, BlockPos pos) {
        return 1;
    }

    @Override public boolean canSimulate() {return true;}

    public boolean shouldSimulate(BlockState state, ServerWorld world, BlockPos pos) {
        if (!UnloadedActivity.instance.config.growSugarCane) return false;
        if (!world.isAir(pos.up())) return false;
        return true;
    }
    @Override public int getCurrentAgeUA(BlockState state) {
        return state.get(AGE);
    }
    @Override public int getMaxAgeUA() {
        return MAX_AGE;
    }
    @Override public int getMaxHeightUA() {return 2;}
    @Override
    public void simulateTime(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        if (shouldSimulate(state, world, pos)) {
            int height = 0;
            while (world.getBlockState(pos.down(height)).isOf(this)) {
                ++height;
            }

            if (height < getMaxHeightUA()+1) {

                int age = getCurrentAgeUA(state);
                int maxAge = getMaxAgeUA()+1; // add one for when growing
                int maxAgeTotal = getMaxHeightUA()*maxAge;
                int remainingAge = maxAgeTotal - (age + (height-1)*maxAge);

                double randomPickChance = getRandomPickOdds(randomTickSpeed);
                double totalOdds = getOdds(world, pos) * randomPickChance;

                int growthAmount = getOccurrences(timePassed, totalOdds, remainingAge, random);

                if (growthAmount != 0) {

                    int growBlocks = growthAmount/16;
                    int ageRemainder = growthAmount % 16;

                    for (int i=0;i<growBlocks;i++) {

                        BlockPos newPos = pos.up(i+1);
                        BlockState newState = this.getDefaultState();

                        if (i+1<growBlocks)
                            newState = newState.with(AGE, ageRemainder);

                        world.setBlockState(newPos, newState);
                        world.updateNeighbor(state, newPos, this, pos, false);
                    }

                    if (growBlocks != 0) {
                        state = state.with(AGE, 0);
                        world.setBlockState(pos, state, Block.NO_REDRAW);
                    }
                }
            }
        }
        super.simulateTime(state, world, pos, random, timePassed, randomTickSpeed);
    }
}