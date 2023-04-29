package com.github.inzan123.mixin.blocks;

import com.github.inzan123.UnloadedActivity;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractPlantStemBlock.class)
public abstract class PlantStemMixin extends AbstractPlantPartBlock implements Fertilizable {

    protected PlantStemMixin(Settings settings, Direction growthDirection, VoxelShape outlineShape, boolean tickWater) {
        super(settings, growthDirection, outlineShape, tickWater);
    }

    @Shadow @Final
    public static IntProperty AGE;
    @Shadow @Final public static int MAX_AGE = 25;
    @Shadow @Final private double growthChance;

    @Shadow protected BlockState age(BlockState state, Random random) {
        return null;
    }
    @Shadow protected boolean chooseStemState(BlockState state) {
        return true;
    }
    @Override
    public double getOdds(ServerWorld world, BlockPos pos) {
        return growthChance;
    }
    @Override public boolean canSimulate() {return true;}
    @Override public int getCurrentAgeUA(BlockState state) {
        return state.get(AGE);
    }

    @Override public int getMaxAgeUA() {
        return MAX_AGE;
    }
    public boolean shouldSimulate(BlockState state, ServerWorld world, BlockPos pos) {
        if (!UnloadedActivity.instance.config.decayLeaves) return false;
        return true;
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
                BlockPos blockPos = pos.offset(this.growthDirection);
                BlockState newState = state;
                int i = 0;
                while (chooseStemState(world.getBlockState(blockPos)) && i < growthAmount) {
                    newState = age(newState, world.random);
                    world.setBlockState(blockPos, newState);
                    blockPos = blockPos.offset(this.growthDirection);
                    i++;
                }
            }
        }
        super.simulateTime(state, world, pos, random, timePassed, randomTickSpeed);
    }
}
