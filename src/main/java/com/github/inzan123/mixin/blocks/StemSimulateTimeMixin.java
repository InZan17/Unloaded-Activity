package com.github.inzan123.mixin.blocks;


import com.github.inzan123.SimulateRandomTicks;
import com.github.inzan123.UnloadedActivity;
import net.minecraft.block.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

import static java.lang.Math.min;
import static java.lang.Math.pow;

@Mixin(StemBlock.class)
public abstract class StemSimulateTimeMixin extends PlantBlock {

    public StemSimulateTimeMixin(Settings settings, GourdBlock gourdBlock) {
        super(settings);
        this.gourdBlock = gourdBlock;
    }

    @Shadow
    public static IntProperty AGE;

    @Shadow
    private final GourdBlock gourdBlock;

    @Override public int getCurrentAgeUA(BlockState state) {
        return state.get(AGE);
    }

    @Override public int getMaxAgeUA() {
        return 7;
    }

    public float getAvailableMoisture(Block block, BlockView world, BlockPos pos) {
        float f = 1.0F;
        BlockPos blockPos = pos.down();

        for(int i = -1; i <= 1; ++i) {
            for(int j = -1; j <= 1; ++j) {
                float g = 0.0F;
                BlockState blockState = world.getBlockState(blockPos.add(i, 0, j));
                if (blockState.isOf(Blocks.FARMLAND)) {
                    g = 1.0F;
                    if (blockState.get(FarmlandBlock.MOISTURE) > 0) {
                        g = 3.0F;
                    }
                }

                if (i != 0 || j != 0) {
                    g /= 4.0F;
                }

                f += g;
            }
        }

        BlockPos blockPos2 = pos.north();
        BlockPos blockPos3 = pos.south();
        BlockPos blockPos4 = pos.west();
        BlockPos blockPos5 = pos.east();
        boolean bl = world.getBlockState(blockPos4).isOf(block) || world.getBlockState(blockPos5).isOf(block);
        boolean bl2 = world.getBlockState(blockPos2).isOf(block) || world.getBlockState(blockPos3).isOf(block);
        if (bl && bl2) {
            f /= 2.0F;
        } else {
            boolean bl3 = world.getBlockState(blockPos4.north()).isOf(block) || world.getBlockState(blockPos5.north()).isOf(block) || world.getBlockState(blockPos5.south()).isOf(block) || world.getBlockState(blockPos4.south()).isOf(block);
            if (bl3) {
                f /= 2.0F;
            }
        }

        return f;
    }

    @Override
    public double getOdds(ServerWorld world, BlockPos pos) {
        float f = getAvailableMoisture(this, world, pos);
        return 1.0/(double)((int)(25.0F / f) + 1);
    }
    @Override public boolean canSimulate() {return true;}
    public boolean shouldSimulate(BlockState state, ServerWorld world, BlockPos pos) {
        if (!UnloadedActivity.instance.config.growStems) return false;
        if (world.getBaseLightLevel(pos, 0) < 9) return false;
        return true;
    }

    @Override
    public void simulateTime(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        if (shouldSimulate(state, world, pos)) {

            int currentAge = this.getCurrentAgeUA(state);
            int maxAge = this.getMaxAgeUA();
            int ageDifference = maxAge - currentAge;

            //We dont check ageDifference since if difference is 0 then it still needs to calculate pumpkin/melon growth

            double randomPickChance = getRandomPickOdds(randomTickSpeed);

            double randomGrowChance = getOdds(world, pos); //chance to grow for every pick

            double totalOdds = randomPickChance * randomGrowChance;

            int growthAmount = getOccurrences(timePassed, totalOdds, ageDifference + 1, random);

            if (growthAmount != 0) {
                state = state.with(AGE, min(currentAge + growthAmount, maxAge));
                world.setBlockState(pos, state, 2);
            }

            if (currentAge + growthAmount > maxAge) { // it surpasses the max age of crop, try to grow fruit

                double chanceForFreeSpace = 0.25 * NumOfValidPositions(pos, world);
                int growsFruit = getOccurrences(timePassed, chanceForFreeSpace, 1, random);

                if (growsFruit != 0) {
                    List<Direction> directions = Direction.Type.HORIZONTAL.getShuffled(random);

                    for (int i = 0; i < directions.size(); i++) {

                        Direction direction = directions.get(i);

                        if (!isValidPosition(direction, pos, world)) continue;

                        BlockPos blockPos = pos.offset(direction);
                        world.setBlockState(blockPos, this.gourdBlock.getDefaultState());

                        state = this.gourdBlock.getAttachedStem().getDefaultState().with(HorizontalFacingBlock.FACING, direction);
                        world.setBlockState(pos, state);
                        break;
                    }
                }
            }
        }
        super.simulateTime(state, world, pos, random, timePassed, randomTickSpeed);
    }

    public int NumOfValidPositions(BlockPos pos, ServerWorld world) {
        return (isValidPosition(Direction.NORTH, pos, world) ? 1 : 0)
             + (isValidPosition(Direction.EAST, pos, world) ? 1 : 0)
             + (isValidPosition(Direction.SOUTH, pos, world) ? 1 : 0)
             + (isValidPosition(Direction.WEST, pos, world) ? 1 : 0);
    }

    public boolean isValidPosition(Direction direction, BlockPos pos, ServerWorld world) {
        BlockPos blockPos = pos.offset(direction);
        BlockState blockState = world.getBlockState(blockPos.down());
        return world.getBlockState(blockPos).isAir() && (blockState.isOf(Blocks.FARMLAND) || blockState.isIn(BlockTags.DIRT));
    }
}
