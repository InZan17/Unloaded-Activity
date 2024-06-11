package com.github.inzan17.interfaces;

import java.util.ArrayList;

public interface ChunkTimeData {
    default long getLastTick() {return 0;};

    default void setLastTick(long tick) {};

    default long getSimulationVersion() {return 0;};

    default void setSimulationVersion(long ver) {};

    default ArrayList<Long> getSimulationBlocks() {return new ArrayList<>();};

    default void setSimulationBlocks(ArrayList<Long> positions) {};
    default void setSimulationBlocks(long[] positions) {
        ArrayList<Long> positionsList = new ArrayList<>();

        for (long value : positions) {
            positionsList.add(value);
        }

        this.setSimulationBlocks(positionsList);
    };

    default void addSimulationBlock(long blockPos) {};

    default void removeSimulationBlock(long blockPos) {};
}
