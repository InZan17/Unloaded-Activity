package com.github.inzan123.mixin;


import com.github.inzan123.SimulateRandomTicks;
import com.github.inzan123.UnloadedActivity;
import net.minecraft.block.*;
import net.minecraft.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

import static java.lang.Math.min;
import static java.lang.Math.pow;

@Mixin(StemBlock.class)
public abstract class StemSimulateTimeMixin extends PlantBlock implements SimulateRandomTicks {

    public StemSimulateTimeMixin(Settings settings, GourdBlock gourdBlock) {
        super(settings);
        this.gourdBlock = gourdBlock;
    }

    @Shadow
    public static IntProperty AGE;

    @Shadow
    private final GourdBlock gourdBlock;

    protected int getAge(BlockState state) {
        return state.get(AGE);
    }

    public int getMaxAge() {
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

    @Override public boolean canSimulate(BlockState state, ServerWorld world, BlockPos pos) {
        if (!UnloadedActivity.instance.config.growStems) return false;
        if (world.getBaseLightLevel(pos, 0) < 9) return false;
        return true;
    }

    @Override
    public void simulateTime(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        int currentAge = this.getAge(state);
        int maxAge = this.getMaxAge();
        int ageDifference = maxAge-currentAge;

        if (ageDifference >= 0) { //if age difference is 0 then it will calculate pumpkin/melon growth instead

            double randomPickChance = 1.0-pow(1.0 - 1.0 / 4096.0, randomTickSpeed); //chance to get picked by random ticks (this will unfortunately not take into account crops being picked twice on high random tick speeds)

            double randomGrowChance = getOdds(world, pos); //chance to grow for every pick

            double totalOdds = randomPickChance * randomGrowChance;

            int growthAmount = getOccurrences(timePassed, totalOdds, ageDifference+1, random);

            if (growthAmount == 0) return;

            state = state.with(AGE, min(currentAge+growthAmount, maxAge));
            world.setBlockState(pos, state, 2);

            if (currentAge+growthAmount <= maxAge) return; //if it doesn't surpass the max age of crop, don't try to grow fruit

            double chanceForFreeSpace = 0.25*NumOfValidPositions(pos,world);

            if (chanceForFreeSpace == 0) return;

            int growsFruit = getOccurrences(timePassed, chanceForFreeSpace, 1, random);

            if (growsFruit == 0) return;

            List<Direction> directions = Direction.Type.HORIZONTAL.getShuffled(random);
            for (int i = 0; i < directions.size(); i++) {

                Direction direction = directions.get(i);

                if (!isValidPosition(direction, pos, world)) continue;

                BlockPos blockPos = pos.offset(direction);
                world.setBlockState(blockPos, this.gourdBlock.getDefaultState());
                world.setBlockState(pos, this.gourdBlock.getAttachedStem().getDefaultState().with(HorizontalFacingBlock.FACING, direction));
                return;
            }
        }
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
