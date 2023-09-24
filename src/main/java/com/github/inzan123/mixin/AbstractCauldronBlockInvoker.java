package com.github.inzan123.mixin;

import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractCauldronBlock.class)
public interface AbstractCauldronBlockInvoker {
    @Invoker("canBeFilledByDripstone")
    public boolean canBeFilledByDripstone(Fluid fluid);

    @Invoker("fillFromDripstone")
    public void fillFromDripstone(BlockState state, World world, BlockPos pos, Fluid fluid);
}
