package com.github.inzan123.mixin.chunk.randomTicks;


import com.github.inzan123.UnloadedActivity;
import com.github.inzan123.Utils;
import com.github.inzan123.mixin.CropBlockInvoker;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CropBlock.class)
public abstract class CropMixin extends PlantBlock {

    public CropMixin(Settings settings) {
        super(settings);
    }

    @Shadow
    protected abstract int getAge(BlockState state);

    @Shadow
    public abstract int getMaxAge();

    @Shadow
    public abstract BlockState withAge(int age);

    @Override
    public double getOdds(ServerWorld world, BlockPos pos) {
        float f = CropBlockInvoker.getAvailableMoisture(this, world, pos);
        return 1.0/(double)((int)(25.0F / f) + 1);
    }
    @Override
    public boolean implementsSimulateRandTicks() {return true;}
    @Override public boolean canSimulateRandTicks(BlockState state, ServerWorld world, BlockPos pos) {
        if (!UnloadedActivity.instance.config.growCrops) return false;
        if (this.getCurrentAgeUA(state) >= this.getMaxAgeUA() || world.getBaseLightLevel(pos.up(), 0) < 9) return false;
        return true;
    }

    @Override public int getCurrentAgeUA(BlockState state) {
        return this.getAge(state);
    }

    @Override public int getMaxAgeUA() {
        return this.getMaxAge();
    }

    @Override
    public void simulateRandTicks(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        int currentAge = getCurrentAgeUA(state);
        int maxAge = getMaxAgeUA();
        int ageDifference = maxAge - currentAge;

        double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
        double totalOdds = getOdds(world, pos) * randomPickChance;

        int growthAmount = Utils.getOccurrences(timePassed, totalOdds, ageDifference, random);

        if (growthAmount == 0)
            return;

        state = this.withAge(currentAge + growthAmount);
        world.setBlockState(pos, state, 2);
    }
}
