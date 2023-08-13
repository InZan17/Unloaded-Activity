package com.github.inzan123.mixin.blocks;

import com.github.inzan123.UnloadedActivity;
import net.minecraft.block.*;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BuddingAmethystBlock.class)
public abstract class BuddingAmethystMixin extends AmethystBlock {

    public BuddingAmethystMixin(Settings settings) {
        super(settings);
    }

    @Shadow @Final static int GROW_CHANCE;

    @Shadow @Final private static Direction[] DIRECTIONS;

    @Shadow @Final public static boolean canGrowIn(BlockState state) {return true;}
    @Override
    public boolean implementsSimulate() {return true;}

    @Override
    public double getOdds(ServerWorld world, BlockPos pos) {
        return 1.0/(GROW_CHANCE*DIRECTIONS.length);
    }
    @Override public boolean canSimulate(BlockState state, ServerWorld world, BlockPos pos) {
        if (!UnloadedActivity.instance.config.growAmethyst) return false;
        return true;
    }
    @Override
    public void simulateTime(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        double randomPickChance = getRandomPickOdds(randomTickSpeed);
        double totalOdds = getOdds(world, pos) * randomPickChance;

        for(int i=0;i<DIRECTIONS.length;i++) {

            Direction direction = DIRECTIONS[i];
            BlockPos blockPos = pos.offset(direction);
            BlockState blockState = world.getBlockState(blockPos);

            int currentAge;

            if (canGrowIn(blockState)) {
                currentAge = 0;
            } else if (blockState.isOf(Blocks.SMALL_AMETHYST_BUD) && blockState.get(AmethystClusterBlock.FACING) == direction) {
                currentAge = 1;
            } else if (blockState.isOf(Blocks.MEDIUM_AMETHYST_BUD) && blockState.get(AmethystClusterBlock.FACING) == direction) {
                currentAge = 2;
            } else if (blockState.isOf(Blocks.LARGE_AMETHYST_BUD) && blockState.get(AmethystClusterBlock.FACING) == direction) {
                currentAge = 3;
            } else {
                continue;
            }

            int ageDifference = 4 - currentAge;

            currentAge += getOccurrences(timePassed, totalOdds, ageDifference, random);

            Block block;

            switch (currentAge) {
                case 1:
                    block = Blocks.SMALL_AMETHYST_BUD;
                    break;
                case 2:
                    block = Blocks.MEDIUM_AMETHYST_BUD;
                    break;
                case 3:
                    block = Blocks.LARGE_AMETHYST_BUD;
                    break;
                case 4:
                    block = Blocks.AMETHYST_CLUSTER;
                    break;
                default:
                    continue;
            }
            BlockState blockState2 = block.getDefaultState()
                .with(AmethystClusterBlock.FACING, direction)
                .with(AmethystClusterBlock.WATERLOGGED, blockState.getFluidState().getFluid() == Fluids.WATER);

            world.setBlockState(blockPos, blockState2);
        }
    }
}
