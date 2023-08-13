package com.github.inzan123.mixin;

import com.github.inzan123.LongArrayComponent;
import com.github.inzan123.LongComponent;
import com.github.inzan123.TimeMachine;
import com.github.inzan123.UnloadedActivity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static com.github.inzan123.MyComponents.*;
import static java.lang.Integer.min;
import static java.lang.Long.max;
import static java.lang.Integer.max;


@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements StructureWorldAccess {
	protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
		super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
	}
	public int updateCount = 0;
	public boolean hasSlept = false;
	public int msTime = 0;

	@Shadow public ServerWorld toServerWorld() {return null;}

	@Inject(at = @At("HEAD"), method = "tickChunk")
	private void tickChunk(WorldChunk chunk, int randomTickSpeed, CallbackInfo info) {

		if (this.isClient())
			return;

		LongComponent lastTick = chunk.getComponent(LASTCHUNKTICK);

		long currentTime = this.getTimeOfDay();

		if (lastTick.getValue() != 0) {

			long timeDifference = max(currentTime - lastTick.getValue(),0);

			int differenceThreshold = UnloadedActivity.instance.config.tickDifferenceThreshold;

			if (timeDifference > differenceThreshold) {
				if (updateCount < UnloadedActivity.instance.config.maxChunkUpdates*getMultiplier() || hasSlept) {
					++updateCount;
					msTime += TimeMachine.simulateRandomTicks(timeDifference, this.toServerWorld(), chunk, randomTickSpeed);
				} else {
					return;
				}
			}
		}

		lastTick.setValue(currentTime);
		if (!UnloadedActivity.instance.config.rememberBlockPositions) {
			LongComponent chunkSimVer = chunk.getComponent(CHUNKSIMVER);
			chunkSimVer.setValue(0);
		}
	}

	private int getMultiplier() {
		return UnloadedActivity.instance.config.multiplyMaxChunkUpdatesPerPlayer ? max(1, this.getPlayers().size()) : 1;
	}

	@Inject(method = "tick", at = @At(value = "TAIL"))
	private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		if (UnloadedActivity.instance.config.debugLogs && updateCount != 0) {
			int averageMs = (int)((float) msTime / updateCount + 0.5);
			UnloadedActivity.LOGGER.info("Average chunk update time for "+updateCount+" chunks: "+averageMs+"ms");
			UnloadedActivity.LOGGER.info("Total chunk update time for "+updateCount+" chunks: "+msTime+"ms");
		}
		msTime = 0;
		updateCount = 0;
		hasSlept = false;
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "net/minecraft/server/world/ServerWorld.wakeSleepingPlayers ()V"))
	private void wakeyWakey(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		if (UnloadedActivity.instance.config.updateAllChunksWhenSleep)
			hasSlept = true;
	}
}

