package com.github.inzan17;

import net.minecraft.nbt.NbtCompound;

public class ChunkSimVerComponent implements LongComponent {
    private long value = 0;

    @Override
    public long getValue() {
        return this.value;
    }

    @Override
    public void setValue(long ver) {
        this.value = ver;
    }
    @Override
    public void readFromNbt(NbtCompound tag) {
        this.value = tag.getLong("sim-ver");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putLong("sim-ver", this.value);
    }
}
