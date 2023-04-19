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

        if (!shouldSimulate(state, world, pos))
            return;

        int currentAge = getCurrentAgeUA(state);
        int ageDifference = getMaxAgeUA() - currentAge;

        double randomPickChance = 1.0-pow(1.0 - 1.0 / 4096.0, randomTickSpeed);

        double totalOdds = getOdds(world, pos) * randomPickChance;

        int growthAmount = getOccurrences(timePassed, totalOdds, ageDifference, random);

        if (growthAmount == 0) return;

        world.setBlockState(pos, (BlockState)state.with(AGE, currentAge + growthAmount), Block.NOTIFY_LISTENERS);
    }
}
