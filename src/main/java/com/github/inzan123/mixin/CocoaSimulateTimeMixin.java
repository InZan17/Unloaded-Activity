package com.github.inzan123.mixin;

import com.github.inzan123.SimulateRandomTicks;
import com.github.inzan123.UnloadedActivity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static java.lang.Math.pow;

@Mixin(CocoaBlock.class)
public abstract class CocoaSimulateTimeMixin extends HorizontalFacingBlock {

    @Shadow @Final
    public static int MAX_AGE;
    @Shadow @Final
    public static IntProperty AGE;

    protected CocoaSimulateTimeMixin(Settings settings) {
        super(settings);
    }

    @Override
    public double getOdds(ServerWorld world, BlockPos pos) {
        return 0.2; //1/5
    }
    @Override public boolean canSimulate() {return true;}
    public boolean shouldSimulate(BlockState state, ServerWorld world, BlockPos pos) {
        if (state == null) return false;
        if (!UnloadedActivity.instance.config.growCocoa) return false;
        return getCurrentAgeUA(state) < getMaxAgeUA();
    }
    @Override public int getCurrentAgeUA(BlockState state) {
        return state.get(AGE);
    }

    @Override public int getMaxAgeUA() {
        return MAX_AGE;
    }

    @Override
    public void simulateTime(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        if (shouldSimulate(state, world, pos)) {

            int currentAge = getCurrentAgeUA(state);
            int maxAge = getMaxAgeUA();
            int ageDifference = maxAge - currentAge;

            double randomPickChance = getRandomPickOdds(randomTickSpeed);
            double totalOdds = getOdds(world, pos) * randomPickChance;

            int growthAmount = getOccurrences(timePassed, totalOdds, ageDifference, random);

            if (growthAmount != 0) {
                state = state.with(AGE, currentAge + growthAmount);
                world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
            }
        }

        super.simulateTime(state, world, pos, random, timePassed, randomTickSpeed);
    }
}
