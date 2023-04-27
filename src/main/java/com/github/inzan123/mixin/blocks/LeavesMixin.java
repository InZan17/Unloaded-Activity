package com.github.inzan123.mixin.blocks;

import com.github.inzan123.UnloadedActivity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LeavesBlock.class)
public abstract class LeavesMixin extends Block{
    public LeavesMixin(Settings settings) {
        super(settings);
    }

    @Shadow
    protected boolean shouldDecay(BlockState state) {
        return true;
    }

    @Override
    public double getOdds(ServerWorld world, BlockPos pos) {
        return 1;
    }
    @Override public boolean canSimulate() {return true;}
    public boolean shouldSimulate(BlockState state, ServerWorld world, BlockPos pos) {
        if (!UnloadedActivity.instance.config.decayLeaves) return false;
        return shouldDecay(state);
    }
    @Override
    public void simulateTime(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        if (shouldSimulate(state, world, pos)) {

            double randomPickChance = getRandomPickOdds(randomTickSpeed);
            double totalOdds = getOdds(world, pos) * randomPickChance;

            int decay = getOccurrences(timePassed, totalOdds, 1, random);

            if (decay != 0) {
                LeavesBlock.dropStacks(state, world, pos);
                world.removeBlock(pos, false);
                return;
            }
        }
        super.simulateTime(state, world, pos, random, timePassed, randomTickSpeed);
    }
}
