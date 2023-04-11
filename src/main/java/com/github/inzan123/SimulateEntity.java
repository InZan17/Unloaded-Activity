package com.github.inzan123;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface SimulateEntity {

    default boolean canSimulate(Entity entity) {
        return true;
    }
    default void simulateTime(Entity entity, long timeDifference)  {

    }
}
