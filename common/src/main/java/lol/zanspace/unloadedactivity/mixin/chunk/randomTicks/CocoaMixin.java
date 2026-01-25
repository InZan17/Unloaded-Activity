package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.OccurrencesAndLeftover;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import lol.zanspace.unloadedactivity.datapack.SimulationData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(CocoaBlock.class)
public abstract class CocoaMixin extends HorizontalDirectionalBlock implements BonemealableBlock {

    protected CocoaMixin(Properties properties) {
        super(properties);
    }

    @Shadow @Final
    public static int MAX_AGE;
    @Shadow @Final
    public static IntegerProperty AGE;

    @Override
    public double getOdds(ServerLevel level, BlockState state, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        return 1d/5d;
    }
    @Override
    public boolean implementsSimulateRandTicks() {return true;}
    @Override public boolean canSimulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        if (state == null) return false;
        if (!UnloadedActivity.config.growCocoa) return false;
        return getCurrentAgeUA(state) < getMaxAgeUA();
    }
    @Override public int getCurrentAgeUA(BlockState state) {
        return state.getValue(AGE);
    }

    @Override public int getMaxAgeUA() {
        return MAX_AGE;
    }

    @Override
    public BlockState simulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName, RandomSource random, long timePassed, int randomTickSpeed, Optional<OccurrencesAndLeftover> returnLeftoverTicks) {

        int currentAge = getCurrentAgeUA(state);
        int maxAge = getMaxAgeUA();
        int ageDifference = maxAge - currentAge;

        double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
        double totalOdds = getOdds(level, state, pos, simulateProperty, propertyName) * randomPickChance;

        int growthAmount = Utils.getOccurrences(timePassed, totalOdds, ageDifference, random);

        if (growthAmount == 0)
            return state;

        state = state.setValue(AGE, currentAge + growthAmount);
        level.setBlock(pos, state, Block.UPDATE_CLIENTS);

        return state;
    }
}
