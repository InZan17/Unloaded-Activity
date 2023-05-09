package com.github.inzan123.mixin;

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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static com.github.inzan123.MyComponents.LASTCHUNKTICK;
import static java.lang.Long.max;


@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements StructureWorldAccess {
	protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
		super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
	}

	public HashMap<ChunkPos, Long> updateList = new HashMap<>();
	public int currentRandomTickSpeed = 0;
	public int updateCount = 0;

	@Inject(at = @At("HEAD"), method = "tickChunk")
	private void tickChunk(WorldChunk chunk, int randomTickSpeed, CallbackInfo info) {

		LongComponent lastTick = chunk.getComponent(LASTCHUNKTICK);

		long currentTime = this.getTimeOfDay();

		if (lastTick.getValue() != 0) {

			long timeDifference = max(currentTime - lastTick.getValue(),0);

			int differenceThreshold = UnloadedActivity.instance.config.tickDifferenceThreshold;

			if (timeDifference > differenceThreshold) {
				if (updateCount < UnloadedActivity.instance.config.maxChunkUpdates) {
					currentRandomTickSpeed = randomTickSpeed;
					updateList.put(
							chunk.getPos(),
							updateList.getOrDefault(chunk.getPos(),0l)+timeDifference
					);
				} else {
					++updateCount;
					TimeMachine.simulateRandomTicks(timeDifference, (ServerWorld)(Object)this, chunk, currentRandomTickSpeed);
					updateList.remove(chunk.getPos());
					lastTick.setValue(currentTime);
				}
				return;
			}
		}

		lastTick.setValue(currentTime);
	}

	@Inject(method = "tick", at = @At(value = "TAIL"))
	private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {

		Iterator<HashMap.Entry<ChunkPos, Long>> iterator = updateList.entrySet().iterator();
		if (iterator.hasNext()) {
			ServerWorld world = (ServerWorld)(Object)this;
			while (iterator.hasNext() && updateCount < UnloadedActivity.instance.config.maxChunkUpdates) {

				HashMap.Entry<ChunkPos, Long> entry = iterator.next();

				ChunkPos pos = entry.getKey();

				WorldChunk chunk = this.getChunk(pos.x,pos.z);

				TimeMachine.simulateRandomTicks(entry.getValue(), world, chunk, currentRandomTickSpeed);

				LongComponent lastTick = chunk.getComponent(LASTCHUNKTICK);

				long currentTime = this.getTimeOfDay();
				lastTick.setValue(currentTime);

				iterator.remove();
				++updateCount;
			}
		}
		updateCount = 0;
	}
}

