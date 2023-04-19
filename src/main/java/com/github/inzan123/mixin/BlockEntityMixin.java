package com.github.inzan123.mixin;

import com.github.inzan123.SimulateBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements SimulateBlockEntity {
}