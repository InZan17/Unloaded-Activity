package lol.zanspace.unloadedactivity.neoforge.mixin;

import org.spongepowered.asm.mixin.Mixin;

#if MC_VER >= MC_1_21
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CropBlock.class)
public interface CropBlockInvoker {
    @Invoker("getGrowthSpeed")
    public static float getAvailableMoisture(BlockState blockState, BlockView world, BlockPos pos) {
        throw new AssertionError();
    }
}
#else
// An empty mixin to the air block.
import net.minecraft.block.AirBlock;
@Mixin(AirBlock.class)
public class CropBlockInvoker {}
#endif
