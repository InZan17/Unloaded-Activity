package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.Utils;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import static java.lang.Math.min;

@Mixin(GrowingPlantHeadBlock.class)
public abstract class GrowingPlantHeadMixin extends GrowingPlantBlock implements BonemealableBlock {

    protected GrowingPlantHeadMixin(Properties properties, Direction direction, VoxelShape voxelShape, boolean bl) {
        super(properties, direction, voxelShape, bl);
    }

    @Shadow @Final
    public static IntegerProperty AGE;
    @Shadow @Final public static int MAX_AGE = 25;
    @Shadow @Final private double growPerTickProbability;

    @Shadow protected BlockState getGrowIntoState(BlockState state, RandomSource random) {
        return null;
    }
    @Shadow protected boolean canGrowInto(BlockState state) {
        return true;
    }
    @Override
    public double getOdds(ServerLevel level, BlockPos pos) {
        return growPerTickProbability;
    }
    @Override
    public boolean implementsSimulateRandTicks() {return true;}
    @Override public boolean canSimulateRandTicks(BlockState state, ServerLevel level, BlockPos pos) {
        if (this.getCurrentAgeUA(state) >= this.getMaxAgeUA()) return false;
        if (!canGrowInto(level.getBlockState(pos.relative(this.growthDirection)))) return false;
        return true;
    }

    @Unique
    private int countValidSteps(BlockGetter blockGetter, BlockPos pos, Direction direction, int maxCount) {
        int i;
        for (i = 0; i < maxCount && canGrowInto(blockGetter.getBlockState(pos.relative(direction, i+1))); ++i) {
        }
        return i;
    }
    @Override public int getCurrentAgeUA(BlockState state) {
        return state.getValue(AGE);
    }

    @Override public int getMaxAgeUA() {
        return MAX_AGE;
    }
    @Override
    public void simulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, long timePassed, int randomTickSpeed) {

        int currentAge = getCurrentAgeUA(state);
        int maxAge = getMaxAgeUA();
        int ageDifference = maxAge - currentAge;
        ageDifference = min(ageDifference, min(ageDifference, countValidSteps(level, pos, this.growthDirection, ageDifference)));

        double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
        double totalOdds = getOdds(level, pos) * randomPickChance;

        int growthAmount = Utils.getOccurrences(timePassed, totalOdds, ageDifference, random);

        if (growthAmount == 0)
            return;

        BlockPos blockPos = pos.relative(this.growthDirection);
        BlockState newState = state;
        int i = 0;
        while (canGrowInto(level.getBlockState(blockPos)) && i < growthAmount) {
            newState = getGrowIntoState(newState, level.random);
            level.setBlockAndUpdate(blockPos, newState);
            blockPos = blockPos.relative(this.growthDirection);
            i++;
        }
    }
}
