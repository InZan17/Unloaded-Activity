package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SweetBerryBushBlock.class)
public abstract class SweetBerryBushMixin extends #if MC_VER >= MC_1_21_5 VegetationBlock #else BushBlock #endif {

    protected SweetBerryBushMixin(Properties properties) {
        super(properties);
    }

    @Shadow @Final public static IntegerProperty AGE;
    @Shadow @Final public static int MAX_AGE;

    @Override
    public double getOdds(ServerLevel level, BlockPos pos) {
        return 0.2;
    }
    @Override
    public boolean implementsSimulateRandTicks() {return true;}
    @Override public boolean canSimulateRandTicks(BlockState state, ServerLevel level, BlockPos pos) {
        if (state == null) return false;
        if (!UnloadedActivity.config.growSweetBerries) return false;
        if (getCurrentAgeUA(state) >= getMaxAgeUA() || level.getRawBrightness(pos.above(), 0) < 9) return false;
        return true;
    }

    @Override public int getCurrentAgeUA(BlockState state) {
        return state.getValue(AGE);
    }

    @Override public int getMaxAgeUA() {
        return MAX_AGE;
    }

    @Override
    public void simulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, long timePassed, int randomTickSpeed) {

        int age = getCurrentAgeUA(state);
        int ageDifference = getMaxAgeUA() - age;

        double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
        double totalOdds = getOdds(level, pos) * randomPickChance;

        int growthAmount = Utils.getOccurrences(timePassed, totalOdds, ageDifference, random);

        if (growthAmount == 0)
            return;

        state = state.setValue(AGE, age + growthAmount);
        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
    }
}
