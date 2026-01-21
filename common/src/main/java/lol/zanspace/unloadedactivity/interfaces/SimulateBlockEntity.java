package lol.zanspace.unloadedactivity.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public interface SimulateBlockEntity {

    default boolean unloaded_activity$canSimulate() {
        return false;
    }
    default void unloaded_activity$simulateTime(ServerLevel level, BlockPos pos, BlockState state, long timeDifference)  {}
}
