package com.github.inzan123;

import dev.onyxstudios.cca.api.v3.block.BlockComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.block.BlockComponentInitializer;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.TransientComponent;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public final class MyComponents implements ChunkComponentInitializer, BlockComponentInitializer, EntityComponentInitializer {
    public static final ComponentKey<LongComponent> LASTCHUNKTICK =
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("unloadedactivity","last-chunk-tick"), LongComponent.class);
    public static final ComponentKey<LongComponent> LASTBLOCKENTITYTICK =
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("unloadedactivity","last-blockentity-tick"), LongComponent.class);
    public static final ComponentKey<LongComponent> LASTENTITYTICK =
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("unloadedactivity","last-entity-tick"), LongComponent.class);
    public static final ComponentKey<TransientComponent> MAGIK =
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("unloadedactivity","magik"), TransientComponent.class);
    @Override
    public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry) {
        registry.register(LASTCHUNKTICK, it -> new LastTickComponent());
        registry.register(MAGIK, it -> new EmptyComponent());
    }

    @Override
    public void registerBlockComponentFactories(BlockComponentFactoryRegistry registry) {
        registry.registerFor(BlockEntity.class,LASTBLOCKENTITYTICK, it -> new LastTickComponent());
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(Entity.class,LASTENTITYTICK, it -> new LastTickComponent());
    }
}

