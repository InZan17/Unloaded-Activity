package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.ExpectPlatform;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BeetrootsBlock.class)
public abstract class BeetrootsMixin extends CropBlock{
    public BeetrootsMixin(Settings settings) {
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
