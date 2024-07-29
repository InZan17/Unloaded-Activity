package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.ExpectPlatform;
import org.spongepowered.asm.mixin.Mixin;

#if MC_VER >= MC_1_19_4
import lol.zanspace.unloadedactivity.mixin.CropBlockInvoker;
import net.minecraft.block.CropBlock;
import net.minecraft.block.TorchflowerBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(TorchflowerBlock.class)
public class TorchFlowerMixin extends CropBlock {
    public TorchFlowerMixin(Settings settings) {
        super(settings);
    }

    @Override
    public double getOdds(ServerWorld world, BlockPos pos) {
        #if MC_VER >= MC_1_21
        float f = ExpectPlatform.getAvailableMoisture(world.getBlockState(pos), world, pos);
        #else
        float f = CropBlockInvoker.getAvailableMoisture(this, world, pos);
        #endif
        return (1.0/(double)((int)(25.0F / f) + 1))/3;
    }
}
#else
// IDK how to make it not complain, so we just do an empty mixin to the air block.
import net.minecraft.block.AirBlock;
@Mixin(AirBlock.class)
public class TorchFlowerMixin {}
#endif