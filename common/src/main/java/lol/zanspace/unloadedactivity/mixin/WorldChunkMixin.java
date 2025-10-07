package lol.zanspace.unloadedactivity.mixin;

import lol.zanspace.unloadedactivity.UnloadedActivity;
import net.minecraft.block.BlockState;
#if MC_VER >= MC_1_19_4
import net.minecraft.registry.Registry;
#else
import net.minecraft.util.registry.Registry;
#endif
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.chunk.BlendingData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin extends Chunk {

    @Shadow @Final World world;

    #if MC_VER >= MC_1_21_10
    public WorldChunkMixin(ChunkPos pos, UpgradeData upgradeData, HeightLimitView heightLimitView, PalettesFactory palettesFactory, long inhabitedTime, @Nullable ChunkSection[] sectionArray, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, heightLimitView, palettesFactory, inhabitedTime, sectionArray, blendingData);
    }
    #else
    public WorldChunkMixin(ChunkPos pos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<Biome> biomeRegistry, long inhabitedTime, @Nullable ChunkSection[] sectionArray, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, heightLimitView, biomeRegistry, inhabitedTime, sectionArray, blendingData);
    }
    #endif

    @Inject(
            at = @At(
                    value="INVOKE",
                    target="net/minecraft/world/chunk/ChunkSection.setBlockState (IIILnet/minecraft/block/BlockState;)Lnet/minecraft/block/BlockState;"
            ),
            method = "setBlockState",
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    #if MC_VER >= MC_1_21_5
    public void blockChanged(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir, int i, ChunkSection chunkSection, boolean bl, int j, int k, int l) {
    #else
    public void blockChanged(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> cir, int i, ChunkSection chunkSection, boolean bl, int j, int k, int l) {
    #endif
        if (world.isClient() || !UnloadedActivity.config.rememberBlockPositions)
            return;

        if (UnloadedActivity.config.debugLogs)
            UnloadedActivity.LOGGER.info("Placed "+state.getBlock().toString()+" at "+pos);

        boolean implementsSimulate = state.getBlock().implementsSimulateRandTicks();
        if (!implementsSimulate)
            return;

        if (UnloadedActivity.config.debugLogs)
            UnloadedActivity.LOGGER.info("Adding position to list "+pos.asLong());

        this.addSimulationBlock(pos.asLong());
    }

    @Inject(method = "<init>(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/ProtoChunk;Lnet/minecraft/world/chunk/WorldChunk$EntityLoader;)V", at = @At("RETURN"))
    private void initWorldChunk(ServerWorld world, ProtoChunk protoChunk, WorldChunk.EntityLoader entityLoader, CallbackInfo ci) {
        if (protoChunk.getLastTick() == 0) {
            this.setLastTick(world.getTimeOfDay());
        } else {
            this.setLastTick(protoChunk.getLastTick());
        }
        this.setSimulationVersion(protoChunk.getSimulationVersion());
        this.setSimulationBlocks(protoChunk.getSimulationBlocks());
    }
}
