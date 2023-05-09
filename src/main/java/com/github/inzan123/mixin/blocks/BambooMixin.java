package com.github.inzan123.mixin.blocks;

import com.github.inzan123.UnloadedActivity;
import net.minecraft.block.*;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static java.lang.Math.min;
import static net.minecraft.block.BambooBlock.*;

@Mixin(BambooBlock.class)
public abstract class BambooMixin extends Block implements Fertilizable {
    public BambooMixin(Settings settings) {
        super(settings);
    }

    @Override public boolean canSimulate() {return true;}
    @Override public double getOdds(ServerWorld world, BlockPos pos) {return 1d/3d;}

    public boolean shouldSimulate(BlockState state, ServerWorld world, BlockPos pos) {
        if (!UnloadedActivity.instance.config.growBamboo) return false;
        if (!world.isAir(pos.up())) return false;
        if (world.getBaseLightLevel(pos.up(), 0) < 9) return false;
        if (state.get(STAGE) == 1) return false;
        return true;
    }
    @Override public int getMaxHeightUA() {
        return 15;
    }

    @Shadow
    protected int countBambooBelow(BlockView world, BlockPos pos) {
        return 0;
    }

    public int countAirAbove(BlockView world, BlockPos pos, int maxCount) {
        int i;
        for (i = 0; i < maxCount && world.getBlockState(pos.up(i + 1)).isAir(); ++i) {
        }
        return i;
    }
    @Override
    public void simulateTime(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {
        if (shouldSimulate(state, world, pos)) {

            int height = countBambooBelow(world, pos);

            if (height < getMaxHeightUA()) {

                int heightDifference = getMaxHeightUA() - height;
                int maxGrowth = countAirAbove(world,pos, heightDifference);

                double randomPickChance = getRandomPickOdds(randomTickSpeed);
                double totalOdds = getOdds(world, pos) * randomPickChance;

                int growthAmount = getOccurrences(timePassed, totalOdds, maxGrowth, random);

                for(int i=1;i<growthAmount+1;i++) {

                    if (state == null)
                        return;

                    if (!shouldSimulate(state, world, pos))
                        return;

                    state = updateLeaves(state,world,pos,random,i);
                    pos = pos.up();
                }
            }
        }
        super.simulateTime(state, world, pos, random, timePassed, randomTickSpeed);
    }

    public BlockState updateLeaves(BlockState state, World world, BlockPos pos, Random random, int height) {
        BlockState blockState = world.getBlockState(pos.down());
        BlockPos blockPos = pos.down(2);
        BlockState blockState2 = world.getBlockState(blockPos);
        BambooLeaves bambooLeaves = BambooLeaves.NONE;
        if (height >= 1) {
            if (!blockState.isOf(Blocks.BAMBOO) || blockState.get(LEAVES) == BambooLeaves.NONE) {
                bambooLeaves = BambooLeaves.SMALL;
            } else if (blockState.isOf(Blocks.BAMBOO) && blockState.get(LEAVES) != BambooLeaves.NONE) {
                bambooLeaves = BambooLeaves.LARGE;
                if (blockState2.isOf(Blocks.BAMBOO)) {
                    world.setBlockState(pos.down(), blockState.with(LEAVES, BambooLeaves.SMALL), Block.NOTIFY_ALL);
                    world.setBlockState(blockPos, blockState2.with(LEAVES, BambooLeaves.NONE), Block.NOTIFY_ALL);
                }
            }
        }
        int age = state.get(AGE) == 1 || blockState2.isOf(Blocks.BAMBOO) ? 1 : 0;
        int stage = height >= 11 && random.nextFloat() < 0.25f || height == 15 ? 1 : 0;
        BlockState newBlockState = Blocks.BAMBOO.getDefaultState().with(AGE, age).with(LEAVES, bambooLeaves).with(STAGE, stage);
        world.setBlockState(pos.up(), newBlockState, Block.NOTIFY_ALL);
        return stage == 0 ? newBlockState : null;
    }
}