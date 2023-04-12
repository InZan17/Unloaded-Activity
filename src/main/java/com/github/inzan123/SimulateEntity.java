package com.github.inzan123;

import net.minecraft.entity.Entity;

public interface SimulateEntity {

    default boolean canSimulate(Entity entity) {
        return true;
    }
    default void simulateTime(Entity entity, long timeDifference)  {

    }
}
