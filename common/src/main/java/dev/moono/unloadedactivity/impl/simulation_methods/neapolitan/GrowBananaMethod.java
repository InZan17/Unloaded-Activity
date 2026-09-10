package dev.moono.unloadedactivity.impl.simulation_methods.neapolitan;

import com.teamabnormals.neapolitan.common.block.BananaFrondBlock;
import com.teamabnormals.neapolitan.core.registry.NeapolitanBlocks;
import dev.moono.unloadedactivity.DeferredBlockPlacer;
import dev.moono.unloadedactivity.GameUtils;
import dev.moono.unloadedactivity.api.OccurrencesAndTimings;
import dev.moono.unloadedactivity.api.SimulationConfig;
import dev.moono.unloadedactivity.api.simulation_method.SeparableSimulationMethod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class GrowBananaMethod extends SeparableSimulationMethod {

    #if MC_VER < MC_1_21_1
    public final Supplier<Block> BANANA_FROND;
    public final Supplier<Block> SMALL_BANANA_FROND;
    public final Supplier<Block> LARGE_BANANA_FROND;
    #endif

    @SuppressWarnings("unchecked")
    public GrowBananaMethod(SimulationConfig simulationConfig, Block block, boolean hasDependants) {
        super(simulationConfig, block, hasDependants);
        if (!(block instanceof BananaFrondBlock)) {
            throw new RuntimeException("The block " + block + " cannot have this simulation method.");
        }
        #if MC_VER < MC_1_21_1
        try {
            BANANA_FROND = (Supplier<Block>)NeapolitanBlocks.class.getField("BANANA_FROND").get(null);
            SMALL_BANANA_FROND = (Supplier<Block>)NeapolitanBlocks.class.getField("SMALL_BANANA_FROND").get(null);
            LARGE_BANANA_FROND = (Supplier<Block>)NeapolitanBlocks.class.getField("LARGE_BANANA_FROND").get(null);
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
        #endif
    }

    @Override
    public int getMaxUpdateCount(BlockState state, ServerLevel level, BlockPos pos) {
        return 1;
    }

    @Override
    public DeferredBlockPlacer getNewBlockStates(BlockState state, ServerLevel level, BlockPos pos, OccurrencesAndTimings occurrencesAndTimings) {
        RandomSource rand = GameUtils.getRand(level);
        #if MC_VER >= MC_1_21_1
        int extra = switch (state.getValue(BananaFrondBlock.SIZE)) {
            case 1 -> rand.nextInt(2);
            case 2 -> 1 + rand.nextInt(2);
            case 3 -> 1 + rand.nextInt(3);
            default -> 0;
        };
        #else
        Block frond = state.getBlock();
        int extra = 0;
		if (frond == this.SMALL_BANANA_FROND.get())
			extra = rand.nextInt(2);
		if (frond == this.BANANA_FROND.get())
			extra = 1 + rand.nextInt(2);
		if (frond == this.LARGE_BANANA_FROND.get())
			extra = 1 + rand.nextInt(3);
        #endif
        int size = 3 + extra;
        BananaFrondBlock.attemptGrowBanana(size, level, GameUtils.getRand(level), pos);
        return null;
    }

    @Override
    public boolean isDependable() {
        return false;
    }
}
