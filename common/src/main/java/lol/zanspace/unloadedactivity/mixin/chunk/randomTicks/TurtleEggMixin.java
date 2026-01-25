package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.OccurrencesAndLeftover;
import lol.zanspace.unloadedactivity.UnloadedActivity;
#if MC_VER >= MC_1_21_3
import net.minecraft.world.entity.EntitySpawnReason;
#endif
import lol.zanspace.unloadedactivity.datapack.SimulationData;
import net.minecraft.world.entity.EntityType;
#if MC_VER >= MC_1_21_11
import net.minecraft.world.entity.animal.turtle.Turtle;
#else
import net.minecraft.world.entity.animal.Turtle;
#endif
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import lol.zanspace.unloadedactivity.Utils;

import java.util.Optional;

import static java.lang.Math.floorMod;
import static java.lang.Math.min;

@Mixin(TurtleEggBlock.class)
public abstract class TurtleEggMixin extends Block {
    //21061
    //21905

    public TurtleEggMixin(Properties properties) {
        super(properties);
    }

    @Shadow @Final
    public static IntegerProperty HATCH;
    @Shadow @Final
    public static IntegerProperty EGGS;

    @Shadow
    public static boolean onSand(BlockGetter blockGetter, BlockPos pos) {
        return false;
    }

    @Override public int getCurrentAgeUA(BlockState state) {
        return state.getValue(HATCH);
    }

    @Override public int getMaxAgeUA() {
        return 2;
    }

    @Override
    public double getOdds(ServerLevel level, BlockState state, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        return 1d/500d;
    }
    @Override
    public boolean implementsSimulateRandTicks() {return true;}

    @Override public boolean canSimulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        if (!onSand(level, pos)) return false;
        if (!UnloadedActivity.config.hatchTurtleEggs) return false;
        return true;
    }

    @Override
    public BlockState simulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName, RandomSource random, long timePassed, int randomTickSpeed, Optional<OccurrencesAndLeftover> returnLeftoverTicks) {

        //at a certain point in the day the odds of hatching become 100% instead of 1/500
        int quickHatchStart = 21061;
        int quickHatchEnd = 21905;
        int dayLength = 24000;

        double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
        double hatchChance = getOdds(level, state, pos, simulateProperty, propertyName);
        double totalOdds = randomPickChance * hatchChance;

        int currentAge = getCurrentAgeUA(state);
        int maxAge = getMaxAgeUA();
        int ageDifference = maxAge - currentAge;

        int growthAmount = 0;
        long leftover = 0;

        if (!UnloadedActivity.config.accurateTurtleAgeAfterHatch) {
            long quickTicks = Utils.getTicksSinceTime(level.getDayTime(), timePassed, quickHatchStart, quickHatchEnd);
            long slowTicks = timePassed-quickTicks;

            growthAmount = Utils.getOccurrences(quickTicks, randomPickChance, ageDifference+1, random);

            if (ageDifference-growthAmount >= 0)
                growthAmount += Utils.getOccurrences(slowTicks, totalOdds, ageDifference-growthAmount+1, random);

            leftover = (long)(random.nextFloat()*(float)(timePassed/(ageDifference+1)));
        } else {
            long originTime = level.getDayTime()-timePassed;
            while(timePassed > 0 && ageDifference-growthAmount >= 0) {
                long localTime = originTime % dayLength;
                if (localTime < quickHatchStart || localTime >= quickHatchEnd) {
                    long remaining = min(floorMod(quickHatchStart-localTime, dayLength), timePassed);
                    timePassed-=remaining;
                    originTime+=remaining;
                    OccurrencesAndLeftover oal = Utils.getOccurrencesAndLeftoverTicks(remaining, totalOdds, ageDifference-growthAmount+1, random);
                    growthAmount += oal.occurrences;
                    leftover = oal.leftover;
                } else {
                    long remaining = min(floorMod(quickHatchEnd-localTime, dayLength), timePassed);
                    timePassed-=remaining;
                    originTime+=remaining;
                    OccurrencesAndLeftover oal = Utils.getOccurrencesAndLeftoverTicks(remaining, randomPickChance, ageDifference-growthAmount+1, random);
                    growthAmount += oal.occurrences;
                    leftover = oal.leftover;
                }
            }
            leftover += timePassed;
        }


        if (growthAmount == 0)
            return state;

        int newAge = currentAge + growthAmount;

        state = state.setValue(HATCH, min(newAge, maxAge));

        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        if (newAge > maxAge) {
            level.removeBlock(pos, false);
            for(int i = 0; i < state.getValue(EGGS); i++) {
                #if MC_VER >= MC_1_21_3
                Turtle turtle = EntityType.TURTLE.create(level, EntitySpawnReason.BREEDING);
                #else
                Turtle turtle = EntityType.TURTLE.create(level);
                #endif

                if (turtle == null)
                    return null;

                turtle.setAge((int) min(-24000+leftover,0)-1); //we do -1 so that it can grow up by itself and drop a scute
                turtle.setHomePos(pos);
                #if MC_VER >= MC_1_21_5
                turtle.snapTo(pos.getX() + 0.3 + i * 0.2, pos.getY(), pos.getZ() + 0.3, 0, 0);
                #else
                turtle.moveTo(pos.getX() + 0.3 + i * 0.2, pos.getY(), pos.getZ() + 0.3, 0, 0);
                #endif
                level.addFreshEntity(turtle);

            }
            return null;
        }
        return state;
    }
}
