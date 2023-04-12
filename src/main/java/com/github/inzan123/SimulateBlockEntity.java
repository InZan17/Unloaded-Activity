package com.github.inzan123;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface SimulateBlockEntity {

    default <T extends BlockEntity> boolean canSimulate(World world, BlockPos pos, BlockState blockState, T blockEntity) {
        return true;
    }
    default <T extends BlockEntity> void simulateTime(World world, BlockPos pos, BlockState blockState, T blockEntity, long timeDifference)  {

    }
}
