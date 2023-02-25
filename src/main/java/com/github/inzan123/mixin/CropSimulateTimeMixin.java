package com.github.inzan123.mixin;


import com.github.inzan123.SimulateRandomTicks;
import com.github.inzan123.UnloadedActivity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static java.lang.Math.pow;

@Mixin(CropBlock.class)
public abstract class CropSimulateTimeMixin extends PlantBlock implements SimulateRandomTicks {

    public CropSimulateTimeMixin(Settings settings) {
        super(settings);
    }

    @Shadow
    protected static float getAvailableMoisture(Block block, BlockView world, BlockPos pos) {
        return 0;
    }

    @Shadow
    protected abstract int getAge(BlockState state);

    @Shadow
    public abstract int getMaxAge();

    @Shadow
    public abstract BlockState withAge(int age);

    @Override
    public double getOdds(ServerWorld world, BlockPos pos) {
        float f = getAvailableMoisture(this, world, pos);
        return 1.0/(double)((int)(25.0F / f) + 1);
    }

    @Override
    public boolean canSimulate(BlockState state, ServerWorld world, BlockPos pos) {
        if (!UnloadedActivity.instance.config.growCrops) return false;
        if (this.getAge(state) >= this.getMaxAge() || world.getBaseLightLevel(pos.up(), 0) < 9) return false;
        return true;
    }

    @Override
    public void simulateTime(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        int currentAge = this.getAge(state);
        int maxAge = this.getMaxAge();
        int ageDifference = maxAge - currentAge;

        double randomPickChance = 1.0-pow(1.0 - 1.0 / 4096.0, randomTickSpeed);

        double totalOdds = getOdds(world, pos) * randomPickChance;

        int growthAmount = getOccurrences(timePassed, totalOdds, ageDifference, random);

        if (growthAmount == 0) return;

        world.setBlockState(pos, this.withAge(currentAge + growthAmount), 2);
    }
}
