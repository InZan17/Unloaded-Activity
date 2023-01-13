package com.github.inzan123;

import net.minecraft.nbt.NbtCompound;

public class ChunkLastTickComponent implements ChunkLongComponent {
    private long value = 0;

    @Override
    public long getValue() {
        return this.value;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.value = tag.getLong("value");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putLong("value", this.value);
    }

    @Override
    public void setValue(long lastTick) {
        this.value = lastTick;
    }
}
