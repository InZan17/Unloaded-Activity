package com.github.inzan123;

import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.TransientComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public final class MyComponents implements ChunkComponentInitializer {
    public static final ComponentKey<ChunkLongComponent> LASTTICK =
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("unloadedactivity","last-tick"), ChunkLongComponent.class);
    public static final ComponentKey<ChunkLongComponent> LASTLOADED =
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("unloadedactivity","last-loaded"), ChunkLongComponent.class);
    public static final ComponentKey<TransientComponent> MAGIK =
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("unloadedactivity","magik"), TransientComponent.class);
    @Override
    public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry) {
        registry.register(LASTTICK, it -> new ChunkLastTickComponent());
        registry.register(LASTLOADED, it -> new ChunkLastLoadedComponent());
        registry.register(MAGIK, it -> new EmptyComponent());

    }
}

