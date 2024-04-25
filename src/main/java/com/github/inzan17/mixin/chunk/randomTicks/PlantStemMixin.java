package com.github.inzan17.mixin.chunk.randomTicks;

import com.github.inzan17.UnloadedActivity;
import com.github.inzan17.Utils;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static java.lang.Math.min;

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
    @Override
    public boolean implementsSimulateRandTicks() {return true;}
    @Override public boolean canSimulateRandTicks(BlockState state, ServerWorld world, BlockPos pos) {
        if (!UnloadedActivity.instance.config.growPlantStems) return false;
        if (this.getCurrentAgeUA(state) >= this.getMaxAgeUA()) return false;
        if (!chooseStemState(world.getBlockState(pos.offset(this.growthDirection)))) return false;
        return true;
    }

    public int countValidSteps(BlockView world, BlockPos pos, Direction direction, int maxCount) {
        int i;
        for (i = 0; i < maxCount && chooseStemState(world.getBlockState(pos.offset(direction, i+1))); ++i) {
        }
        return i;
    }
    @Override public int getCurrentAgeUA(BlockState state) {
        return state.get(AGE);
    }

    @Override public int getMaxAgeUA() {
        return MAX_AGE;
    }
    @Override
    public void simulateRandTicks(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        int currentAge = getCurrentAgeUA(state);
        int maxAge = getMaxAgeUA();
        int ageDifference = maxAge - currentAge;
        ageDifference = min(ageDifference, min(ageDifference, countValidSteps(world, pos, this.growthDirection, ageDifference)));

        double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
        double totalOdds = getOdds(world, pos) * randomPickChance;

        int growthAmount = Utils.getOccurrences(timePassed, totalOdds, ageDifference, random);

        if (growthAmount == 0)
            return;

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
