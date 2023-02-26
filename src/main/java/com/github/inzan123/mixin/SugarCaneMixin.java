package com.github.inzan123.mixin;

import com.github.inzan123.SimulateRandomTicks;
import com.github.inzan123.UnloadedActivity;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static java.lang.Math.pow;

@Mixin(SugarCaneBlock.class)
public abstract class SugarCaneMixin extends Block implements SimulateRandomTicks {

    @Shadow @Final public static IntProperty AGE;

    public SugarCaneMixin(Settings settings) {
        super(settings);
    }

    @Override
    public double getOdds(ServerWorld world, BlockPos pos) {
        return 1;
    }

    @Override
    public boolean canSimulate(BlockState state, ServerWorld world, BlockPos pos) {
        if (!UnloadedActivity.instance.config.growSugarCane) return false;
        if (!world.isAir(pos.up())) return false;
        int height = 1;
        while (world.getBlockState(pos.down(height)).isOf(this)) {
            ++height;
        }
        return height < 3;
    }

    @Override
    public void simulateTime(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        int height = 1;
        while (world.getBlockState(pos.down(height)).isOf(this)) {
            ++height;
        }
        if (height >= 3) return;

        int age = state.get(AGE);
        int maxAge = 2*16; //16 per sugar cane block, top block doesn't grow.
        int remainingAge = maxAge - (age + (height-1)*16);

        double randomPickChance = 1.0-pow(1.0 - 1.0 / 4096.0, randomTickSpeed);

        double totalOdds = getOdds(world, pos) * randomPickChance;

        int growthAmount = getOccurrences(timePassed, totalOdds, remainingAge, random);

        if (growthAmount == 0) return;

        int growBlocks = growthAmount/16;
        int ageRemainder = growthAmount % 16;

        if (growBlocks != 0)
            world.setBlockState(pos, state.with(AGE, 0), Block.NO_REDRAW);

        for (int i=0;i<growBlocks;i++) {
            world.setBlockState(pos.up(i+1), this.getDefaultState());
            if (i+1<growBlocks)
                world.setBlockState(pos.up(i+1), this.getDefaultState().with(AGE, ageRemainder));
        }
    }
}