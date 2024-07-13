package lol.zanspace.unloadedactivity.interfaces;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface SimulateBlockEntity {

    default boolean canSimulate() {
        return false;
    }
    default void simulateTime(World world, BlockPos pos, BlockState state, BlockEntity blockEntity, long timeDifference)  {}
}
