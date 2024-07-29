package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static java.lang.Math.min;
import static net.minecraft.block.BambooBlock.*;

@Mixin(BambooBlock.class)
public abstract class BambooMixin extends Block implements Fertilizable {
    public BambooMixin(Settings settings) {
        super(settings);
    }
    @Override public double getOdds(ServerWorld world, BlockPos pos) {return 1d/3d;}
    @Override
    public boolean implementsSimulateRandTicks() {return true;}
    @Override
    public boolean canSimulateRandTicks(BlockState state, ServerWorld world, BlockPos pos) {
        if (!UnloadedActivity.config.growBamboo) return false;
        if (!world.isAir(pos.up())) return false;
        if (world.getBaseLightLevel(pos.up(), 0) < 9) return false;
        if (state.get(STAGE) == 1) return false;
        return true;
    }
    @Override public int getMaxHeightUA() {
        return 15;
    }

    @Shadow
    protected int countBambooBelow(BlockView world, BlockPos pos) {
        return 0;
    }

    @Shadow protected abstract int countBambooAbove(BlockView world, BlockPos pos);

    public int countAirAbove(BlockView world, BlockPos pos, int maxCount) {
        int i;
        for (i = 0; i < maxCount && world.getBlockState(pos.up(i + 1)).isAir(); ++i) {
        }
        return i;
    }
    @Override
    public void simulateRandTicks(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        int height = countBambooBelow(world, pos);

        if (height >= getMaxHeightUA())
            return;

        int heightDifference = getMaxHeightUA() - height;
        int maxGrowth = this.countAirAbove(world,pos, heightDifference);

        double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
        double totalOdds = getOdds(world, pos) * randomPickChance;

        int growthAmount = Utils.getOccurrences(timePassed, totalOdds, maxGrowth, random);

        BlockState currentState = state;
        BlockPos currentPos = pos;

        for(int i=0;i<growthAmount;i++) {
            this.grow(world, random, currentPos, currentState);

            if (i == growthAmount - 1)
                return;

            int grew = this.countBambooAbove(world, currentPos);

            if (grew == 0)
                return;

            currentPos = currentPos.up(grew);
            currentState = world.getBlockState(currentPos);

            if (!this.canSimulateRandTicks(currentState, world, currentPos))
                return;
        }
    }
}