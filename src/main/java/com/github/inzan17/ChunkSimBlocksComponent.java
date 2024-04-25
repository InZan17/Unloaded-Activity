package com.github.inzan17;

import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;

public class ChunkSimBlocksComponent implements LongArrayComponent {
    private ArrayList<Long> positions = new ArrayList<>();

    @Override
    public ArrayList<Long> getValue() {
        return positions;
    }

    @Override
    public void setValue(ArrayList<Long> positions) {
        this.positions = positions;
    }

    @Override
    public void addValue(long blockPos) {

        if (positions.contains(blockPos))
            return;

        positions.add(blockPos);
    }

    @Override
    public void removeValue(long blockPos) {

        int blockPosIndex = positions.indexOf(blockPos);

        if (blockPosIndex < 0)
            return;

        positions.remove(blockPosIndex);
    }
    @Override
    public void readFromNbt(NbtCompound tag) {
        long[] longArray = tag.getLongArray("sim-blocks");
        ArrayList<Long> longArrayList = new ArrayList<>();

        for (long value : longArray) {
            longArrayList.add(value);
        }

        this.positions = longArrayList;
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putLongArray("sim-blocks", this.positions);
    }
}
