package com.github.inzan17.mixin.chunk.randomTicks;

import com.github.inzan17.UnloadedActivity;
import com.github.inzan17.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
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

    @Override
    public boolean implementsSimulateRandTicks() {return true;}
    @Override public boolean canSimulateRandTicks(BlockState state, ServerWorld world, BlockPos pos) {
        if (!UnloadedActivity.instance.config.decayLeaves) return false;
        return shouldDecay(state);
    }
    @Override
    public void simulateRandTicks(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
        double totalOdds = getOdds(world, pos) * randomPickChance;

        int decay = Utils.getOccurrences(timePassed, totalOdds, 1, random);

        if (decay == 0)
            return;

        LeavesBlock.dropStacks(state, world, pos);
        world.removeBlock(pos, false);
    }
}
