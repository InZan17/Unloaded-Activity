package dev.moono.unloadedactivity.impl.number_fetchers.neapolitan;

import com.teamabnormals.neapolitan.common.block.BananaFrondBlock;
import dev.moono.unloadedactivity.api.context.FixedContext;
import dev.moono.unloadedactivity.api.number_fetcher.FixedNumberFetcher;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ShouldBeMoistValue implements FixedNumberFetcher {
    @Override
    public Number evaluate(FixedContext context) {
        BlockState state = context.getBlockState();
        if (!(state.getBlock() instanceof BananaFrondBlock)) {
            return 0;
        }

        boolean result = state.getValue(BlockStateProperties.FACING) == Direction.UP
            && BananaFrondBlock.canGrowOn(context.getLevel().getBlockState(context.getBlockPos().below()))
            && BananaFrondBlock.canRainAt(context.getServerLevel(), context.getBlockPos());
        return result ? 1 : 0;
    }
}
