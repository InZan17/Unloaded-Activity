package com.github.inzan123.mixin;

import com.github.inzan123.SimulateRandomTicks;
import com.github.inzan123.UnloadedActivity;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;

import static java.lang.Math.pow;

@Mixin(OxidizableBlock.class)
public abstract class OxidizableBlockMixin extends Block implements Oxidizable {

    public OxidizableBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public double getOdds(ServerWorld world, BlockPos pos) {
        return 0.05688889f;
    }
    @Shadow
    public OxidationLevel getDegradationLevel() {
        return null;
    }
    @Override public boolean canSimulate() {return true;}
    public boolean shouldSimulate(BlockState state, ServerWorld world, BlockPos pos) {
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
    public void simulateTime(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        if (!shouldSimulate(state, world, pos))
            return;

        double randomPickChance = 1.0-pow(1.0 - 1.0 / 4096.0, randomTickSpeed);

        double tryDegradeOdds = getOdds(world, pos);

        BlockPos blockPos;
        float nearbyBlocks = 0;
        Iterator<BlockPos> iterator = BlockPos.iterateOutwards(pos, 4, 4, 4).iterator();
        while (iterator.hasNext() && (blockPos = iterator.next()).getManhattanDistance(pos) <= 4) {
            if (blockPos.equals(pos) || !((world.getBlockState(blockPos).getBlock()) instanceof Degradable)) continue;
            nearbyBlocks++;
        }
        float degradeOdds = 1 / (nearbyBlocks + 1);
        float degradeOdds2 = degradeOdds * degradeOdds * 0.75f;

        double totalOdds = randomPickChance * tryDegradeOdds * degradeOdds2;

        int currentAge = getCurrentAgeUA(state);

        int ageDifference = getMaxAgeUA()-currentAge;

        int ageAmount = getOccurrences(timePassed, totalOdds, ageDifference, random);

        if (ageAmount == 0) return;

        degrade(ageAmount, state, world, pos);
    }

    public void degrade(int steps, BlockState state, ServerWorld world, BlockPos pos) {
        this.getDegradationResult(state).ifPresent(state2 -> {
            world.setBlockState(pos, state2);
            int newSteps = steps - 1;
            if (newSteps != 0) {
                degrade(newSteps, state2, world, pos);
                // I am currently having a breakdown. Who decided copper blocks should be this complicated??? and Why??? WHY COULDNT IT JUST BE LIKE NORMAL BLOCKS????
            }
        });
    }
}
