package lol.zanspace.unloadedactivity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.nio.file.Path;

public class ExpectPlatform {
    @dev.architectury.injectables.annotations.ExpectPlatform
    public static Path getConfigDirectory() {
        throw new AssertionError();
    }
    #if MC_VER >= MC_1_21_1
    @dev.architectury.injectables.annotations.ExpectPlatform
    public static float getAvailableMoisture(BlockState blockState, BlockView world, BlockPos pos) {
        throw new AssertionError();
    }
    #endif
}
