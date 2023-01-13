package com.github.inzan123;

import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import net.minecraft.util.Identifier;

public final class MyComponents implements ChunkComponentInitializer {
    public static final ComponentKey<ChunkLongComponent> MAGIK =
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("unloadedactivity","magik"), ChunkLongComponent.class);

    @Override
    public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry) {
        // Add the component to every World instance
        registry.register(MAGIK, it -> new ChunkLastTickComponent());
    }
}
