package com.github.inzan17.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.CropBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CropBlock.class)
public interface CropBlockInvoker {
    @Invoker("getAvailableMoisture")
    public static float getAvailableMoisture(Block block, BlockView world, BlockPos pos) {
        throw new AssertionError();
    }
}
