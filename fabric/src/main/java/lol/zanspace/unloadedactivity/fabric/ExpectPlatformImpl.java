package lol.zanspace.unloadedactivity.fabric;

#if MC_VER >= MC_1_21_1
import lol.zanspace.unloadedactivity.fabric.mixin.CropBlockInvoker;
#endif
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.nio.file.Path;

public class ExpectPlatformImpl {
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
    #if MC_VER >= MC_1_21_1
    public static float getAvailableMoisture(BlockState blockState, BlockView world, BlockPos pos) {
        return CropBlockInvoker.getAvailableMoisture(blockState.getBlock(), world, pos);
    }
    #endif
}
