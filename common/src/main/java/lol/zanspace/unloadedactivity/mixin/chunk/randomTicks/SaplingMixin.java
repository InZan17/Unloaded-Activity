package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
#if MC_VER >= MC_1_20_4
import net.minecraft.world.level.block.grower.TreeGrower;
#else
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
#endif
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static java.lang.Math.*;

@Mixin(SaplingBlock.class)
public abstract class SaplingMixin extends #if MC_VER >= MC_1_21_5 VegetationBlock #else BushBlock #endif {

    protected SaplingMixin(Properties properties) {
        super(properties);
    }

    #if MC_VER >= MC_1_20_4
    @Shadow @Final private TreeGrower treeGrower;
    #else
    @Shadow @Final private AbstractTreeGrower treeGrower;
    #endif

    @Shadow
    public void advanceTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
    }

    @Shadow @Final public static IntegerProperty STAGE;

    @Override
    public double getOdds(ServerLevel level, BlockPos pos) {
        return 0.14285714285; // 1/7
    }
    @Override
    public boolean implementsSimulateRandTicks() {return true;}
    @Override public boolean canSimulateRandTicks(BlockState state, ServerLevel level, BlockPos pos) {
        if (!UnloadedActivity.config.growSaplings) return false;
        if (level.getRawBrightness(pos, 0) < 9) return false;
        if (!state.is(this)) return false;
        return true;
    }

    @Override public int getCurrentAgeUA(BlockState state) {
        return state.getValue(STAGE);
    }

    @Override public int getMaxAgeUA() {
        return 1;
    }

    @Override
    public void simulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, long timePassed, int randomTickSpeed) {

        if (level.getBrightness(LightLayer.BLOCK, pos.above()) < 9) { // If there isnt enough block lights we will do a calculation on how many ticks the tree could have spent in sunlight.
            int stopGrowTime = 13027; //stops growing at 12739 ticks when raining, 13027 when no rain
            int startGrowTime = 22974; //starts growing at 23267 ticks when raining, 22974 when no rain

            timePassed = Utils.getTicksSinceTime(level.getDayTime(),timePassed,startGrowTime,stopGrowTime);
        }


        double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
        double randomGrowChance = getOdds(level, pos);
        double totalOdds = randomPickChance * randomGrowChance;

        int currentAge = getCurrentAgeUA(state);

        int maxAge = getMaxAgeUA();

        int ageDifference = maxAge - currentAge;

        int growthAmount = Utils.getOccurrences(timePassed, totalOdds, ageDifference + 1, random);

        if (growthAmount == 0)
            return;

        int newAge = currentAge + growthAmount;

        state = state.setValue(STAGE, min(newAge, maxAge));

        level.setBlock(pos, state, Block.UPDATE_INVISIBLE);
        if (newAge > maxAge) {
            this.advanceTree(level, pos, state, random);
        }
    }
}