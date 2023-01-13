package com.github.inzan123;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.nbt.NbtCompound;

public interface ChunkLongComponent extends ComponentV3 {
    long getValue();

    void setValue(long lastTick);

}

