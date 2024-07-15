package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.SaplingBlock;
#if MC_VER >= MC_1_20_4
import net.minecraft.block.SaplingGenerator;
#else
import net.minecraft.block.sapling.SaplingGenerator;
#endif
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static java.lang.Math.*;

@Mixin(SaplingBlock.class)
public abstract class SaplingMixin extends PlantBlock {

    public SaplingMixin(Settings settings) {
        super(settings);
    }

    @Shadow @Final private SaplingGenerator generator;

    @Shadow
    public void generate(ServerWorld world, BlockPos pos, BlockState state, Random random) {
    }

    @Shadow
    public abstract boolean canGrow(World world, Random random, BlockPos pos, BlockState state);

    @Shadow @Final public static IntProperty STAGE;

    @Override
    public double getOdds(ServerWorld world, BlockPos pos) {
        return 0.14285714285; // 1/7
    }
    @Override
    public boolean implementsSimulateRandTicks() {return true;}
    @Override public boolean canSimulateRandTicks(BlockState state, ServerWorld world, BlockPos pos) {
        if (!UnloadedActivity.config.growSaplings) return false;
        if (world.getBaseLightLevel(pos, 0) < 9) return false;
        if (!state.isOf(this)) return false;
        return true;
    }

    @Override public int getCurrentAgeUA(BlockState state) {
        return state.get(STAGE);
    }

    @Override public int getMaxAgeUA() {
        return 1;
    }

    @Override
    public void simulateRandTicks(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        if (!this.canGrow(world, random, pos, state)) {
            return;
        }

        if (world.getLightLevel(LightType.BLOCK, pos.up()) < 9) { // If there isnt enough block lights we will do a calculation on how many ticks the tree could have spent in sunlight.
            int stopGrowTime = 13027; //stops growing at 12739 ticks when raining, 13027 when no rain
            int startGrowTime = 22974; //starts growing at 23267 ticks when raining, 22974 when no rain

            timePassed = Utils.getTicksSinceTime(world.getTimeOfDay(),timePassed,startGrowTime,stopGrowTime);
        }


        double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
        double randomGrowChance = getOdds(world, pos);
        double totalOdds = randomPickChance * randomGrowChance;

        int currentAge = getCurrentAgeUA(state);

        int maxAge = getMaxAgeUA();

        int ageDifference = maxAge - currentAge;

        int growthAmount = Utils.getOccurrences(timePassed, totalOdds, ageDifference + 1, random);

        if (growthAmount == 0)
            return;

        int newAge = currentAge + growthAmount;

        state = state.with(STAGE, min(newAge, maxAge));

        world.setBlockState(pos, state, 4);
        if (newAge > maxAge) {
            this.generate(world, pos, state, random);
        }
    }
}