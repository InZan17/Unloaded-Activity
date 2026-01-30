package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.OccurrencesAndDuration;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import lol.zanspace.unloadedactivity.datapack.SimulationData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(LeavesBlock.class)
public abstract class LeavesMixin extends Block{

    public LeavesMixin(Properties properties) {
        super(properties);
    }

    /*

    @Shadow
    protected boolean decaying(BlockState state) {
        return true;
    }

    @Override
    public double getOdds(ServerLevel level, BlockState state, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        return 1;
    }

    @Override
    public boolean implementsSimulateRandTicks() {return true;}
    @Override public boolean canSimulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        if (!UnloadedActivity.config.decayLeaves) return false;
        return decaying(state);
    }
    @Override
    public Triple<BlockState, OccurrencesAndDuration, BlockPos> simulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName, RandomSource random, long timePassed, int randomTickSpeed, boolean calculateDuration) {

        double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
        double totalOdds = getOdds(level, state, pos, simulateProperty, propertyName) * randomPickChance;

        int decay = Utils.getOccurrences(timePassed, totalOdds, 1, random);

        if (decay == 0)
            return state;

        dropResources(state, level, pos);
        level.removeBlock(pos, false);

        return null;
    }

     */
}
