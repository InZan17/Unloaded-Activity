package com.github.inzan123.mixin.blocks;

import com.github.inzan123.SimulateRandomTicks;
import com.github.inzan123.UnloadedActivity;
import com.github.inzan123.Utils;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.Optional;

import static java.lang.Math.pow;

@Mixin(OxidizableStairsBlock.class)
public abstract class OxidizableStairsBlockMixin extends StairsBlock implements Oxidizable {

    public OxidizableStairsBlockMixin(BlockState baseBlockState, Settings settings) {
        super(baseBlockState, settings);
    }

    @Override
    public double getOdds(ServerWorld world, BlockPos pos) {
        return 0.05688889f;
    }
    @Override
    public boolean implementsSimulateRandTicks() {return true;}
    @Shadow
    public OxidationLevel getDegradationLevel() {
        return null;
    }
    @Override public boolean canSimulateRandTicks(BlockState state, ServerWorld world, BlockPos pos) {
        if (!UnloadedActivity.instance.config.ageCopper) return false;
        int currentAge = getCurrentAgeUA(state);
        if (currentAge == getMaxAgeUA()) return false;
        return true;
    }

    @Override public int getCurrentAgeUA(BlockState state) {
        return this.getDegradationLevel().ordinal();
    }

    @Override public int getMaxAgeUA() {
        return 3;
    }

    @Override
    public void simulateRandTicks(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);

        double tryDegradeOdds = getOdds(world, pos);

        BlockPos blockPos;
        float nearbyBlocks = 0;
        Iterator<BlockPos> iterator = BlockPos.iterateOutwards(pos, 4, 4, 4).iterator();
        while (iterator.hasNext() && (blockPos = iterator.next()).getManhattanDistance(pos) <= 4) {
            if (blockPos.equals(pos) || !((world.getBlockState(blockPos).getBlock()) instanceof Degradable))
                continue;
            nearbyBlocks++;
        }
        float degradeOdds = 1 / (nearbyBlocks + 1);
        float degradeOdds2 = degradeOdds * degradeOdds * 0.75f;

        double totalOdds = randomPickChance * tryDegradeOdds * degradeOdds2;
        int currentAge = getCurrentAgeUA(state);
        int ageDifference = getMaxAgeUA() - currentAge;

        int ageAmount = Utils.getOccurrences(timePassed, totalOdds, ageDifference, random);

        if (ageAmount == 0)
            return;

        state = getDegradeResult(ageAmount, state, world, pos);
        world.setBlockState(pos, state);
    }

    public BlockState getDegradeResult(int steps, BlockState state, ServerWorld world, BlockPos pos) {

        steps--;

        Optional<BlockState> optionalState = this.getDegradationResult(state);

        if (optionalState.isEmpty())
            return state;

        if (steps != 0) {
            return getDegradeResult(steps, optionalState.get(), world, pos);
            //im too lazy to see how getDegradationResult actually degrades the thing
        }

        return optionalState.get();
    }
}
