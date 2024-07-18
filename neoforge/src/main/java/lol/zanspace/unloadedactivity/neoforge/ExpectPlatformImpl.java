package lol.zanspace.unloadedactivity.neoforge;

#if MC_VER >= MC_1_21
import lol.zanspace.unloadedactivity.neoforge.mixin.CropBlockInvoker;
#endif

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ExpectPlatformImpl {
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
    #if MC_VER >= MC_1_21
    public static float getAvailableMoisture(BlockState blockState, BlockView world, BlockPos pos) {
        return CropBlockInvoker.getAvailableMoisture(blockState, world, pos);
    }
    #endif
}
