package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.datapack.SimulationData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

#if MC_VER >= MC_1_21_1
import lol.zanspace.unloadedactivity.ExpectPlatform;
#else
import lol.zanspace.unloadedactivity.mixin.CropBlockInvoker;
#endif

@Mixin(BeetrootBlock.class)
public abstract class BeetrootsMixin extends CropBlock {

    protected BeetrootsMixin(Properties properties) {
        super(properties);
    }

    @Override
    public double getOdds(ServerLevel level, BlockState state, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        #if MC_VER >= MC_1_21_1
        float f = ExpectPlatform.getGrowthSpeed(level.getBlockState(pos), level, pos);
        #else
        float f = CropBlockInvoker.invokeGetGrowthSpeed(this, level, pos);
        #endif
        return (1.0/(double)((int)(25.0F / f) + 1))/3;
    }
}
