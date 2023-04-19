package com.github.inzan123.mixin;

import com.github.inzan123.SimulateRandomTicks;
import com.github.inzan123.UnloadedActivity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static java.lang.Math.*;

@Mixin(SaplingBlock.class)
public abstract class SaplingSimulateTimeMixin extends PlantBlock {

    public SaplingSimulateTimeMixin(Settings settings) {
        super(settings);
    }

    @Shadow @Final private SaplingGenerator generator;

    @Shadow
    public void generate(ServerWorld world, BlockPos pos, BlockState state, Random random) {
    }

    @Shadow @Final public static IntProperty STAGE;

    @Override
    public double getOdds(ServerWorld world, BlockPos pos) {
        return 0.14285714285; // 1/7
    }
    @Override public boolean canSimulate() {return true;}
    public boolean shouldSimulate(BlockState state, ServerWorld world, BlockPos pos) {
        if (!UnloadedActivity.instance.config.growSaplings) return false;
        if (world.getBaseLightLevel(pos, 0) < 9) return false;
        return true;
    }

    @Override public int getCurrentAgeUA(BlockState state) {
        return state.get(STAGE);
    }

    @Override public int getMaxAgeUA() {
        return 1;
    }

    @Override
    public void simulateTime(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        if (!shouldSimulate(state, world, pos))
            return;

        if (world.getLightLevel(LightType.BLOCK, pos.up()) < 9) { // If there isnt enough block lights we will do a calculation on how many ticks the tree could have spent in sunlight.
            long dayLength = 24000;
            long stopGrowTime = 13027; //stops growing at 12739 ticks when raining, 13027 when no rain
            long startGrowTime = 22974; //starts growing at 23267 ticks when raining, 22974 when no rain
            long offset = dayLength - startGrowTime; // we use this offset to pretend crops start growing at 0 ticks

            long growTimeWindow = stopGrowTime+offset;

            long currentTime = floorMod(min(floorMod(world.getTimeOfDay()+offset, dayLength)-offset, stopGrowTime), growTimeWindow);

            long previousTime = floorMod(min(floorMod(world.getTimeOfDay()-timePassed+offset, dayLength)-offset, stopGrowTime), growTimeWindow);

            long usefulTicks = growTimeWindow*(timePassed / dayLength);
            long restOfDayTicks = (currentTime-previousTime) % growTimeWindow;

            if (floorMod(timePassed, dayLength) > growTimeWindow)
                if (restOfDayTicks == 0)
                    restOfDayTicks = growTimeWindow;

            timePassed = restOfDayTicks + usefulTicks;
        }


        double randomPickChance = getRandomPickOdds(randomTickSpeed);
        double randomGrowChance = getOdds(world, pos);
        double totalOdds = randomPickChance * randomGrowChance;

        int currentAge = getCurrentAgeUA(state);

        int maxAge = getMaxAgeUA();

        int ageDifference = maxAge-currentAge;

        int growthAmount = getOccurrences(timePassed, totalOdds, ageDifference+1, random);

        if (growthAmount == 0) return;

        int newAge = currentAge + growthAmount;

        world.setBlockState(pos, state.with(STAGE,  min(newAge,maxAge)), 4);
        if (newAge > maxAge) {
            this.generate(world, pos, world.getBlockState(pos), random);
        }

    }
}