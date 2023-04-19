package com.github.inzan123;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface SimulateBlockEntity {

    default boolean canSimulate() {
        return false;
    }
    default void simulateTime(World world, BlockPos pos, BlockState blockState, BlockEntity blockEntity, long timeDifference)  {

    }
}
