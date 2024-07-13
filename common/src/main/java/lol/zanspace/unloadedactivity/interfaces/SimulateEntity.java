package lol.zanspace.unloadedactivity.interfaces;

import net.minecraft.entity.Entity;

public interface SimulateEntity {

    default boolean canSimulate() {
        return false;
    }
    default void simulateTime(Entity entity, long timeDifference)  {

    }
}
