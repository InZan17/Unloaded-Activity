package lol.zanspace.unloadedactivity.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;

#if MC_VER >= MC_1_21_1
import net.minecraft.block.Block;
import net.minecraft.block.CropBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CropBlock.class)
public interface CropBlockInvoker {
    @Invoker("getAvailableMoisture")
    public static float getAvailableMoisture(Block block, BlockView world, BlockPos pos) {
        throw new AssertionError();
    }
}
#else
// An empty mixin to the air block.
import net.minecraft.block.AirBlock;
@Mixin(AirBlock.class)
public class CropBlockInvoker {}
#endif
