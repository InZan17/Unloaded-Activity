package com.github.inzan123.mixin;

import com.github.inzan123.SimulateTimePassing;
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

@Mixin(CocoaBlock.class)
public class CocoaSimulateTimeMixin implements SimulateTimePassing {

    @Shadow @Final
    public static int MAX_AGE;
    @Shadow @Final
    public static IntProperty AGE;

    @Override
    public double getGrowthOdds(ServerWorld world, BlockPos pos) {
        return 0.2; //1/5
    }
    @Override
    public void simulateTime(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        if (!UnloadedActivity.CONFIG.growCocoa()) return;

        int currentAge = state.get(AGE);
        int ageDifference = MAX_AGE - currentAge;
        if (ageDifference <= 0) return;

        double randomPickChance = 1.0-pow(1.0 - 1.0 / 4096.0, randomTickSpeed);

        double totalOdds = getGrowthOdds(world, pos) * randomPickChance;

        int growthAmount = getOccurrences(timePassed, totalOdds, ageDifference, random);

        if (growthAmount == 0) return;

        world.setBlockState(pos, (BlockState)state.with(AGE, currentAge + growthAmount), Block.NOTIFY_LISTENERS);
    }
}
