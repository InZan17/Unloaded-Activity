package com.github.inzan17.mixin;

import com.github.inzan17.LongComponent;
import com.github.inzan17.TimeMachine;
import com.github.inzan17.UnloadedActivity;
import com.github.inzan17.WeatherInfoInterface;
#if MC_VER >= MC_1_19_4
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
#else
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
#endif
import net.minecraft.server.world.ServerWorld;
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

import static com.github.inzan17.MyComponents.*;
import static java.lang.Long.max;
import static java.lang.Integer.max;


@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements StructureWorldAccess {
	#if MC_VER >= MC_1_19_4
	protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
		super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
	}
    #else
	protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimension, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
		super(properties, registryRef, dimension, profiler, isClient, debugWorld, seed, maxChainedNeighborUpdates);
	}
	#endif
	public int updateCount = 0;
	public int knownUpdateCount = 0;
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
				if (chunkIsKnown(chunk)) {
					if (knownUpdateCount < UnloadedActivity.instance.config.maxKnownChunkUpdates*getMultiplier() || hasSlept) {
						++knownUpdateCount;
						msTime += TimeMachine.simulateChunk(timeDifference, this.toServerWorld(), chunk, randomTickSpeed);
					} else {
						return;
					}
				} else {
					if (updateCount < UnloadedActivity.instance.config.maxChunkUpdates*getMultiplier() || hasSlept) {
						++updateCount;
						msTime += TimeMachine.simulateChunk(timeDifference, this.toServerWorld(), chunk, randomTickSpeed);
					} else {
						return;
					}
				}
			}
		}

		lastTick.setValue(currentTime);
		if (!UnloadedActivity.instance.config.rememberBlockPositions) {
			LongComponent chunkSimVer = chunk.getComponent(CHUNKSIMVER);
			chunkSimVer.setValue(0);
		}
	}

	private boolean chunkIsKnown(WorldChunk chunk) {
		LongComponent chunkSimVer = chunk.getComponent(CHUNKSIMVER);
		return chunkSimVer.getValue() == UnloadedActivity.chunkSimVer;
	}

	private int getMultiplier() {
		return UnloadedActivity.instance.config.multiplyMaxChunkUpdatesPerPlayer ? max(1, this.getPlayers().size()) : 1;
	}

	@Inject(method = "tick", at = @At(value = "TAIL"))
	private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		if (UnloadedActivity.instance.config.debugLogs && (updateCount+knownUpdateCount) != 0) {
			int averageMs = (int)((float) msTime / (updateCount+knownUpdateCount) + 0.5);
			UnloadedActivity.LOGGER.info("Average chunk update time for "+updateCount+" chunks and  "+knownUpdateCount+" known chunks: "+averageMs+"ms");
			UnloadedActivity.LOGGER.info("Total chunk update time for "+updateCount+" chunks and  "+knownUpdateCount+" known chunks: "+msTime+"ms");
		}
		msTime = 0;
		updateCount = 0;
		knownUpdateCount = 0;
		hasSlept = false;
	}

	@Inject(method = "tick", at = @At(value = "TAIL", target = "net/minecraft/server/world/ServerWorld.tickTime ()V"))
	private void finishTickTime(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		WeatherInfoInterface weatherInfo = this.getComponent(WORLDWEATHERINFO);
		weatherInfo.updateValues(this);
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "net/minecraft/server/world/ServerWorld.wakeSleepingPlayers ()V"))
	private void wakeyWakey(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		if (UnloadedActivity.instance.config.updateAllChunksWhenSleep)
			hasSlept = true;
	}
}

