package com.github.inzan123.mixin;

import com.github.inzan123.ChunkSimBlocksComponent;
import com.github.inzan123.LongArrayComponent;
import com.github.inzan123.UnloadedActivity;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.BlendingData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;

import static com.github.inzan123.MyComponents.CHUNKSIMBLOCKS;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin extends Chunk {

    @Shadow @Final World world;

    public WorldChunkMixin(ChunkPos pos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<Biome> biomeRegistry, long inhabitedTime, @Nullable ChunkSection[] sectionArray, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, heightLimitView, biomeRegistry, inhabitedTime, sectionArray, blendingData);
    }

    @Inject(
            at = @At(
                    value="INVOKE",
                    target="net/minecraft/world/chunk/ChunkSection.setBlockState (IIILnet/minecraft/block/BlockState;)Lnet/minecraft/block/BlockState;"
            ),
            method = "setBlockState",
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void blockChanged(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> cir, int i, ChunkSection chunkSection, boolean bl, int j, int k, int l) {
        if (world.isClient() || !UnloadedActivity.instance.config.rememberBlockPositions)
            return;

        if (UnloadedActivity.instance.config.debugLogs)
            UnloadedActivity.LOGGER.info("Placed "+state.getBlock().toString()+" at "+pos);

        boolean implementsSimulate = state.getBlock().implementsSimulate();
        if (!implementsSimulate)
            return;

        if (UnloadedActivity.instance.config.debugLogs)
            UnloadedActivity.LOGGER.info("Adding position to list "+pos.asLong());

        LongArrayComponent chunkSimBlocks = this.getComponent(CHUNKSIMBLOCKS);
        chunkSimBlocks.addValue(pos.asLong());
    }
}
