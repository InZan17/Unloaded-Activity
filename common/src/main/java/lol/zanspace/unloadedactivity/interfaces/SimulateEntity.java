package lol.zanspace.unloadedactivity.interfaces;

public interface SimulateEntity {

    default boolean canSimulate() {
        return false;
    }
    default void simulateTime(long timeDifference)  {

    }
}
