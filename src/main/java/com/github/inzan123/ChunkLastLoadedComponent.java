package com.github.inzan123;

import net.minecraft.nbt.NbtCompound;

public class ChunkLastLoadedComponent implements ChunkLongComponent {
    private long value = 0;

    @Override
    public long getValue() {
        return this.value;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.value = tag.getLong("last-loaded");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putLong("last-loaded", this.value);
    }

    @Override
    public void setValue(long value) {
        this.value = value;
    }
}