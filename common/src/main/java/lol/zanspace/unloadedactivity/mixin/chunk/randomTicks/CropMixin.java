package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;


import lol.zanspace.unloadedactivity.ExpectPlatform;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import lol.zanspace.unloadedactivity.mixin.CropBlockInvoker;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CropBlock.class)
public abstract class CropMixin extends #if MC_VER >= MC_1_21_5 VegetationBlock #else BushBlock #endif {

    protected CropMixin(Properties properties) {
        super(properties);
    }

    @Shadow
    protected abstract int getAge(BlockState state);

    @Shadow
    public abstract int getMaxAge();

    @Shadow
    public abstract BlockState getStateForAge(int age);

    @Override
    public double getOdds(ServerLevel level, BlockPos pos) {

        #if MC_VER >= MC_1_21_1
        float f = ExpectPlatform.getGrowthSpeed(level.getBlockState(pos), level, pos);
        #else
        float f = CropBlockInvoker.getGrowthSpeed(this, level, pos);
        #endif
        return 1.0/(double)((int)(25.0F / f) + 1);
    }

    @Override
    public boolean implementsSimulateRandTicks() {return true;}

    @Override public boolean canSimulateRandTicks(BlockState state, ServerLevel level, BlockPos pos) {
        if (!UnloadedActivity.config.growCrops) return false;
        if (this.getCurrentAgeUA(state) >= this.getMaxAgeUA() || level.getRawBrightness(pos.above(), 0) < 9) return false;
        return true;
    }

    @Override public int getCurrentAgeUA(BlockState state) {
        return this.getAge(state);
    }

    @Override public int getMaxAgeUA() {
        return this.getMaxAge();
    }

    @Override
    public void simulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, long timePassed, int randomTickSpeed) {

        int currentAge = getCurrentAgeUA(state);
        int maxAge = getMaxAgeUA();
        int ageDifference = maxAge - currentAge;

        double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
        double totalOdds = getOdds(level, pos) * randomPickChance;

        int growthAmount = Utils.getOccurrences(timePassed, totalOdds, ageDifference, random);

        if (growthAmount == 0)
            return;

        state = this.getStateForAge(currentAge + growthAmount);
        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
    }
}
