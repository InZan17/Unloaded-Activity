package com.github.inzan123;

import net.minecraft.nbt.NbtCompound;

public class LastTickComponent implements LongComponent {
    private long value = 0;

    @Override
    public long getValue() {
        return this.value;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.value = tag.getLong("last-tick");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putLong("last-tick", this.value);
    }

    @Override
    public void setValue(long lastTick) {
        this.value = lastTick;
    }
}
