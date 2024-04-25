package com.github.inzan17.mixin.chunk.randomTicks;

import net.minecraft.block.CropBlock;
import net.minecraft.block.TorchflowerBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

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
