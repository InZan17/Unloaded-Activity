package com.github.inzan17;

public interface BlockEntityTimeData {
    default long getLastTick() {return 0;};

    default void setLastTick(long tick) {};
}
