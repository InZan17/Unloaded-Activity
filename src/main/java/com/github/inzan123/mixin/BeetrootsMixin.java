package com.github.inzan123.mixin;

import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BeetrootsBlock.class)
public class BeetrootsMixin extends CropBlock{
    public BeetrootsMixin(Settings settings) {
        super(settings);
    }

    @Override
    public double getOdds(ServerWorld world, BlockPos pos) {
        float f = getAvailableMoisture(this, world, pos);
        return (1.0/(double)((int)(25.0F / f) + 1))/3;
    }
}
