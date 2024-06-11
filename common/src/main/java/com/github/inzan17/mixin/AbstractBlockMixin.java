package com.github.inzan17.mixin;

import com.github.inzan17.interfaces.SimulateChunkBlocks;
import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin implements SimulateChunkBlocks {
}