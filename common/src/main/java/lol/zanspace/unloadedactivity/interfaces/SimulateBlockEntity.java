package lol.zanspace.unloadedactivity.interfaces;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public interface SimulateBlockEntity {

    default boolean canSimulate() {
        return false;
    }
    default void simulateTime(ServerWorld world, BlockPos pos, BlockState state, BlockEntity blockEntity, long timeDifference)  {}
}
