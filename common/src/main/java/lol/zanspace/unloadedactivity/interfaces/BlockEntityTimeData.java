package lol.zanspace.unloadedactivity.interfaces;

public interface BlockEntityTimeData {
    default long getLastTick() {return 0;};

    default void setLastTick(long tick) {};
}
