package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;


import lol.zanspace.unloadedactivity.OccurrencesAndDuration;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import lol.zanspace.unloadedactivity.datapack.SimulationData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(IceBlock.class)
public abstract class IceMixin extends HalfTransparentBlock {

    protected IceMixin(Properties properties) {
        super(properties);
    }

    /*

    @Override
    public double getOdds(ServerLevel level, BlockState state, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        return 1;
    }

    @Shadow protected void melt(BlockState state, Level level, BlockPos pos) {}

    @Override
    public boolean implementsSimulateRandTicks() {return true;}

    @Override public boolean canSimulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        if (!UnloadedActivity.config.meltIce) return false;
        #if MC_VER >= MC_1_21_3
        int opacity = state.getLightBlock();
        #else
        int opacity = state.getLightBlock(level, pos);
        #endif
        if (level.getBrightness(LightLayer.BLOCK, pos) <= 11 - opacity) return false;
        return true;
    }

    @Override
    public Triple<BlockState, OccurrencesAndDuration, BlockPos> simulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName, RandomSource random, long timePassed, int randomTickSpeed, boolean calculateDuration) {

        double pickOdds = Utils.getRandomPickOdds(randomTickSpeed) * this.getOdds(level, state, pos, simulateProperty, propertyName);

        if (Utils.getOccurrences(timePassed, pickOdds, 1, random) != 0) {
            this.melt(state, level, pos);
            return null;
        }

        return state;
    }

     */
}
