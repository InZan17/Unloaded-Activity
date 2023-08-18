package com.github.inzan123.mixin.blocks;

import com.github.inzan123.LongComponent;
import com.github.inzan123.UnloadedActivity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TurtleEggBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static com.github.inzan123.MyComponents.LASTENTITYTICK;
import static java.lang.Math.floorMod;
import static java.lang.Math.min;

@Mixin(TurtleEggBlock.class)
public abstract class TurtleEggMixin extends Block {
    //21061
    //21905
    public TurtleEggMixin(Settings settings) {
        super(settings);
    }
    @Shadow @Final
    public static IntProperty HATCH;
    @Shadow @Final
    public static IntProperty EGGS;

    @Shadow
    public static boolean isSandBelow(BlockView world, BlockPos pos) {
        return false;
    }

    @Override public int getCurrentAgeUA(BlockState state) {
        return state.get(HATCH);
    }

    @Override public int getMaxAgeUA() {
        return 2;
    }

    @Override
    public double getOdds(ServerWorld world, BlockPos pos) {
        return 0.002; //1/500
    }
    @Override
    public boolean implementsSimulate() {return true;}

    @Override public boolean canSimulate(BlockState state, ServerWorld world, BlockPos pos) {
        if (!isSandBelow(world, pos)) return false;
        if (!UnloadedActivity.instance.config.hatchTurtleEggs) return false;
        return true;
    }

    @Override
    public void simulateTime(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        //at a certain point in the day the odds of hatching become 100% instead of 1/500
        int quickHatchStart = 21061;
        int quickHatchEnd = 21905;
        int dayLength = 24000;
        long quickTicks = getTicksSinceTime(world.getTimeOfDay(), timePassed, quickHatchStart, quickHatchEnd);
        long slowTicks = timePassed-quickTicks;

        double randomPickChance = getRandomPickOdds(randomTickSpeed);
        double hatchChance = getOdds(world, pos);
        double totalOdds = randomPickChance * hatchChance;

        int currentAge = getCurrentAgeUA(state);
        int maxAge = getMaxAgeUA();
        int ageDifference = maxAge - currentAge;

        int growthAmount = 0;
        long leftover = 0;

        if (false) { //replace with a "Very Accurate Simulations" options later pls
            growthAmount = getOccurrences(quickTicks, randomPickChance, ageDifference+1, random);

            if (ageDifference-growthAmount >= 0)
                growthAmount += getOccurrences(slowTicks, totalOdds, ageDifference-growthAmount+1, random);
        } else {
            long originTime = world.getTimeOfDay()-timePassed;
            while(timePassed > 0 && ageDifference-growthAmount >= 0) {
                UnloadedActivity.LOGGER.info(""+timePassed);
                long localTime = originTime % dayLength;
                UnloadedActivity.LOGGER.info(""+localTime);
                if (localTime < quickHatchStart || localTime >= quickHatchEnd) {
                    long remaining = min(floorMod(quickHatchStart-localTime, dayLength), timePassed);
                    timePassed-=remaining;
                    originTime+=remaining;
                    OccurrencesAndLeftover oal = getOccurrencesAndLeftoverTicksFast(remaining, hatchChance, randomTickSpeed, ageDifference-growthAmount+1, random);
                    growthAmount += oal.occurrences;
                    leftover = oal.leftover;
                } else {
                    long remaining = min(floorMod(quickHatchEnd-localTime, dayLength), timePassed);
                    timePassed-=remaining;
                    originTime+=remaining;
                    OccurrencesAndLeftover oal = getOccurrencesAndLeftoverTicksFast(remaining, 1.0, randomTickSpeed, ageDifference-growthAmount+1, random);
                    growthAmount += oal.occurrences;
                    leftover = oal.leftover;
                }
            }
            leftover += timePassed;
        }


        if (growthAmount == 0)
            return;

        int newAge = currentAge + growthAmount;

        state = state.with(HATCH, min(newAge, maxAge));

        world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
        if (newAge > maxAge) {
            world.removeBlock(pos, false);
            for(int i = 0; i < state.get(EGGS); i++) {
                TurtleEntity turtle = EntityType.TURTLE.create(world);

                if (turtle == null)
                    return;

                LongComponent lastTick = turtle.getComponent(LASTENTITYTICK);
                lastTick.setValue(world.getTimeOfDay());

                turtle.setBreedingAge((int) min(-24000+leftover,0));
                turtle.setHomePos(pos);
                turtle.refreshPositionAndAngles(pos.getX() + 0.3 + i * 0.2, pos.getY(), pos.getZ() + 0.3, 0, 0);
                world.spawnEntity(turtle);

            }
        }
    }
}
