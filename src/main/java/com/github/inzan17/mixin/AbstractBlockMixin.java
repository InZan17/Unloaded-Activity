package com.github.inzan17.mixin;

import com.github.inzan17.SimulateChunkBlocks;
import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin implements SimulateChunkBlocks {
}