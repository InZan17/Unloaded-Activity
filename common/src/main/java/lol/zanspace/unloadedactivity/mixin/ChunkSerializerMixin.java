package lol.zanspace.unloadedactivity.mixin;

import lol.zanspace.unloadedactivity.UnloadedActivity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
#if MC_VER <= MC_1_21_1
import net.minecraft.world.ChunkSerializer;
#endif
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
#if MC_VER <= MC_1_19_4
import net.minecraft.world.chunk.ReadOnlyChunk;
#else
import net.minecraft.world.chunk.WrapperProtoChunk;
#endif
import net.minecraft.world.poi.PointOfInterestStorage;
#if MC_VER >= MC_1_21_1
import net.minecraft.world.storage.StorageKey;
#endif

#if MC_VER >= MC_1_21_3
import net.minecraft.world.chunk.SerializedChunk;
import net.minecraft.world.HeightLimitView;
import net.minecraft.registry.DynamicRegistryManager;
#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

#if MC_VER <= MC_1_21_1
@Mixin(value = ChunkSerializer.class, priority = 999)
public abstract class ChunkSerializerMixin {
    @Inject(method = "serialize", at = @At("RETURN"))
    private static void serialize(ServerWorld world, Chunk chunk, CallbackInfoReturnable<NbtCompound> cir) {
        NbtCompound nbtCompound = cir.getReturnValue();

        NbtCompound chunkData = new NbtCompound();

        chunkData.putLong("last_tick", chunk.getLastTick());
        chunkData.putLong("ver", chunk.getSimulationVersion());
        chunkData.putLongArray("sim_blocks", chunk.getSimulationBlocks());

        nbtCompound.put("unloaded_activity", chunkData);
    }

    @Inject(method = "deserialize", at = @At("RETURN"))
    #if MC_VER >= MC_1_21_1
    private static void deserialize(ServerWorld world, PointOfInterestStorage poiStorage, StorageKey key, ChunkPos chunkPos, NbtCompound nbtCompound, CallbackInfoReturnable<ProtoChunk> cir)
    #else
    private static void deserialize(ServerWorld world, PointOfInterestStorage poiStorage, ChunkPos chunkPos, NbtCompound nbtCompound, CallbackInfoReturnable<ProtoChunk> cir)
    #endif
    {
        ProtoChunk protoChunkTemp = cir.getReturnValue();

        #if MC_VER <= MC_1_19_4
        Chunk protoChunk = (protoChunkTemp instanceof ReadOnlyChunk readOnlyChunk) ? readOnlyChunk.getWrappedChunk() : protoChunkTemp;
        #else
        Chunk protoChunk = (protoChunkTemp instanceof WrapperProtoChunk wrappedChunk) ? wrappedChunk.getWrappedChunk() : protoChunkTemp;
        #endif

        NbtCompound chunkData = nbtCompound.getCompound("unloaded_activity");

        boolean isEmpty = chunkData.isEmpty();

        if (isEmpty) {
            protoChunk.setNeedsSaving(true);
        } else {
            protoChunk.setLastTick(chunkData.getLong("last_tick"));
            protoChunk.setSimulationVersion(chunkData.getLong("ver"));
            protoChunk.setSimulationBlocks(chunkData.getLongArray("sim_blocks"));
        }


        if (UnloadedActivity.config.convertCCAData && isEmpty) {
            NbtCompound cardinalData = nbtCompound.getCompound("cardinal_components");

            if (!cardinalData.isEmpty()) {
                NbtCompound lastChunkTick = cardinalData.getCompound("unloadedactivity:last-chunk-tick");
                if (!lastChunkTick.isEmpty()) {
                    protoChunk.setLastTick(lastChunkTick.getLong("last-tick"));
                }

                NbtCompound chunkSimVer = cardinalData.getCompound("unloadedactivity:chunk-sim-ver");
                if (!chunkSimVer.isEmpty()) {
                    protoChunk.setSimulationVersion(chunkSimVer.getLong("sim-ver"));
                }

                NbtCompound chunkSimBlocks = cardinalData.getCompound("unloadedactivity:chunk-sim-blocks");
                if (!chunkSimBlocks.isEmpty()) {
                    protoChunk.setSimulationBlocks(chunkSimBlocks.getLongArray("sim-blocks"));
                }

                // This is so that cardinal components doesn't start sending warnings.
                cardinalData.remove("unloadedactivity:last-chunk-tick");
                cardinalData.remove("unloadedactivity:chunk-sim-ver");
                cardinalData.remove("unloadedactivity:chunk-sim-blocks");
                cardinalData.remove("unloadedactivity:magik");
            }
        }

        if (protoChunk.getLastTick() == 0) {
            protoChunk.setNeedsSaving(true);
            protoChunk.setLastTick(world.getTimeOfDay());
        }
    }
}

#else
@Mixin(value = SerializedChunk.class, priority = 999)
public abstract class ChunkSerializerMixin {

    public long lastTick = 0;
    public long ver = 0;
    public long[] simBlocks = {};
    public boolean needsSaving = false;

    @Inject(method = "serialize", at = @At("RETURN"))
    public void serialize(CallbackInfoReturnable<NbtCompound> cir) {
        NbtCompound nbtCompound = cir.getReturnValue();

        NbtCompound chunkData = new NbtCompound();

        chunkData.putLong("last_tick", lastTick);
        chunkData.putLong("ver", ver);
        chunkData.putLongArray("sim_blocks", simBlocks);

        nbtCompound.put("unloaded_activity", chunkData);
    }

    @Inject(method = "convert", at = @At("RETURN"))
    public void convert(ServerWorld world, PointOfInterestStorage poiStorage, StorageKey key, ChunkPos expectedPos, CallbackInfoReturnable<ProtoChunk> cir) {
        ProtoChunk protoChunkTemp = cir.getReturnValue();

        Chunk chunk = (protoChunkTemp instanceof WrapperProtoChunk wrappedChunk) ? wrappedChunk.getWrappedChunk() : protoChunkTemp;

        chunk.setLastTick(lastTick);
        chunk.setSimulationVersion(ver);
        chunk.setSimulationBlocks(simBlocks);
        if (lastTick == 0) {
            chunk.markNeedsSaving();
            chunk.setLastTick(world.getTimeOfDay());
        } else if (needsSaving) {
            chunk.markNeedsSaving();
        }
    }

    @Inject(method = "fromChunk", at = @At("RETURN"))
    private static void fromChunk(ServerWorld world, Chunk chunk, CallbackInfoReturnable<SerializedChunk> cir) {
        ChunkSerializerMixin serializedChunk = (ChunkSerializerMixin) (Object) cir.getReturnValue();
        if (serializedChunk != null) {
            serializedChunk.lastTick = chunk.getLastTick();
            serializedChunk.ver = chunk.getSimulationVersion();
            serializedChunk.simBlocks = chunk.getSimulationBlocks().stream().mapToLong(l -> l).toArray();;
        }
    }

    @Inject(method = "fromNbt", at = @At("RETURN"))
    private static void fromNbt(HeightLimitView world, DynamicRegistryManager registryManager, NbtCompound nbtCompound, CallbackInfoReturnable<SerializedChunk> cir) {
        ChunkSerializerMixin serializedChunk = (ChunkSerializerMixin)(Object)cir.getReturnValue();

        if (serializedChunk == null) {
            return;
        }

        NbtCompound chunkData = nbtCompound.getCompound("unloaded_activity");

        boolean isEmpty = chunkData.isEmpty();

        if (isEmpty) {
            serializedChunk.needsSaving = true;
        } else {
            serializedChunk.lastTick = chunkData.getLong("last_tick");
            serializedChunk.ver = chunkData.getLong("ver");
            serializedChunk.simBlocks = chunkData.getLongArray("sim_blocks");
        }


        if (UnloadedActivity.config.convertCCAData && isEmpty) {
            NbtCompound cardinalData = nbtCompound.getCompound("cardinal_components");

            if (!cardinalData.isEmpty()) {
                NbtCompound lastChunkTick = cardinalData.getCompound("unloadedactivity:last-chunk-tick");
                if (!lastChunkTick.isEmpty()) {
                    serializedChunk.lastTick = lastChunkTick.getLong("last-tick");
                }

                NbtCompound chunkSimVer = cardinalData.getCompound("unloadedactivity:chunk-sim-ver");
                if (!chunkSimVer.isEmpty()) {
                    serializedChunk.ver = chunkSimVer.getLong("sim-ver");
                }

                NbtCompound chunkSimBlocks = cardinalData.getCompound("unloadedactivity:chunk-sim-blocks");
                if (!chunkSimBlocks.isEmpty()) {
                    serializedChunk.simBlocks = chunkSimBlocks.getLongArray("sim-blocks");
                }

                // This is so that cardinal components doesn't start sending warnings.
                cardinalData.remove("unloadedactivity:last-chunk-tick");
                cardinalData.remove("unloadedactivity:chunk-sim-ver");
                cardinalData.remove("unloadedactivity:chunk-sim-blocks");
                cardinalData.remove("unloadedactivity:magik");
            }
        }
    }
}
#endif