package lol.zanspace.unloadedactivity.mixin;

import lol.zanspace.unloadedactivity.interfaces.SimulateChunkBlocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin implements SimulateChunkBlocks {
}