package com.github.inzan17.mixin;

import com.github.inzan17.SimulateBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements SimulateBlockEntity {
}