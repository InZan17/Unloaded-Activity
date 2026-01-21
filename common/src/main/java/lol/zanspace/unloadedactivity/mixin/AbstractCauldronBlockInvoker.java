package lol.zanspace.unloadedactivity.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractCauldronBlock.class)
public interface AbstractCauldronBlockInvoker {
    @Invoker("canReceiveStalactiteDrip")
    public boolean canReceiveStalactiteDrip(Fluid fluid);

    @Invoker("receiveStalactiteDrip")
    public void receiveStalactiteDrip(BlockState state, Level level, BlockPos pos, Fluid fluid);
}
