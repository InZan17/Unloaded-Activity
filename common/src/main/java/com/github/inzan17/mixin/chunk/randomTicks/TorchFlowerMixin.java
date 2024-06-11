package com.github.inzan17.mixin.chunk.randomTicks;

import org.spongepowered.asm.mixin.Mixin;

#if MC_VER >= MC_1_19_4
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
        float f = getAvailableMoisture(this, world, pos);
        return (1.0/(double)((int)(25.0F / f) + 1))/3;
    }
}
#else
// IDK how to make it not complain, so we just do an empty mixin to the air block.
import net.minecraft.block.AirBlock;
@Mixin(AirBlock.class)
public class TorchFlowerMixin {}
#endif