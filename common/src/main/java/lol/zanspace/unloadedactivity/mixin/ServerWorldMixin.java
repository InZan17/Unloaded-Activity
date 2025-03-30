package lol.zanspace.unloadedactivity.mixin;

#if MC_VER >= MC_1_21_5
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentStateType;
#endif

#if MC_VER >= MC_1_20_2
import net.minecraft.datafixer.DataFixTypes;
#endif
#if MC_VER >= MC_1_19_4
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
#else
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
#endif
import lol.zanspace.unloadedactivity.TimeMachine;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.WorldWeatherData;
import lol.zanspace.unloadedactivity.interfaces.WorldTimeData;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.*;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static java.lang.Long.max;
import static java.lang.Integer.max;


@Mixin(value = ServerWorld.class, priority = 1001)
public abstract class ServerWorldMixin extends World implements StructureWorldAccess, WorldTimeData {
	#if MC_VER >= MC_1_21_3
	protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
		super(properties, registryRef, registryManager, dimensionEntry, isClient, debugWorld, seed, maxChainedNeighborUpdates);
	}
	#elif MC_VER >= MC_1_19_4
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

		long lastTick = chunk.getLastTick();

		long currentTime = this.getTimeOfDay();

		if (lastTick != 0) {

			long timeDifference = max(currentTime - lastTick,0);

			int differenceThreshold = UnloadedActivity.config.tickDifferenceThreshold;

			if (timeDifference > differenceThreshold) {
				if (chunkIsKnown(chunk)) {
					if (knownUpdateCount < UnloadedActivity.config.maxKnownChunkUpdates*getMultiplier() || hasSlept) {
						++knownUpdateCount;
						msTime += TimeMachine.simulateChunk(timeDifference, this.toServerWorld(), chunk, randomTickSpeed);
					} else {
						return;
					}
				} else {
					if (updateCount < UnloadedActivity.config.maxChunkUpdates*getMultiplier() || hasSlept) {
						++updateCount;
						msTime += TimeMachine.simulateChunk(timeDifference, this.toServerWorld(), chunk, randomTickSpeed);
					} else {
						return;
					}
				}
			}
		}

		chunk.setLastTick(currentTime);

		if (!UnloadedActivity.config.rememberBlockPositions) {
			chunk.setSimulationVersion(0);
		}
	}

	private boolean chunkIsKnown(WorldChunk chunk) {
		return chunk.getSimulationVersion() == UnloadedActivity.chunkSimVer;
	}

	private int getMultiplier() {
		return UnloadedActivity.config.multiplyMaxChunkUpdatesPerPlayer ? max(1, this.getPlayers().size()) : 1;
	}

	@Inject(method = "tick", at = @At(value = "TAIL"))
	private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		if (UnloadedActivity.config.debugLogs && (updateCount+knownUpdateCount) != 0) {
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
		WorldWeatherData weatherInfo = this.getWeatherData();
		weatherInfo.updateValues(this);
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "net/minecraft/server/world/ServerWorld.wakeSleepingPlayers ()V"))
	private void wakeyWakey(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		if (UnloadedActivity.config.updateAllChunksWhenSleep)
			hasSlept = true;
	}

	@Shadow public abstract PersistentStateManager getPersistentStateManager();

	#if MC_VER >= MC_1_21_5
	private static PersistentStateType<WorldWeatherData> type = new PersistentStateType<>(
			"unloaded_activity",
			(ctx) -> new WorldWeatherData(),
			(ctx) -> {
				ServerWorld world = ctx.getWorldOrThrow();
				return NbtCompound.CODEC.xmap(
						nbt -> WorldWeatherData.fromNbt(nbt, world.getRegistryManager()),
						weatherData -> weatherData.writeNbt(new NbtCompound(), world.getRegistryManager())
				);
			},
			DataFixTypes.LEVEL
	);
	#elif MC_VER >= MC_1_20_2
	private static PersistentState.Type<WorldWeatherData> type = new PersistentState.Type<>(
			WorldWeatherData::new,
			WorldWeatherData::fromNbt,
			DataFixTypes.LEVEL
	);
	#endif

	@Override
	public WorldWeatherData getWeatherData() {
		return this.getPersistentStateManager().getOrCreate(
			#if MC_VER >= MC_1_20_2
			type
			#else
			tag -> WorldWeatherData.fromNbt(tag),
			() -> new WorldWeatherData(),
			#endif
			#if MC_VER < MC_1_21_5
			"unloaded_activity"
			#endif
		);
	}

	@Inject(at = @At("RETURN"), method = "<init>*")
	private void createState(CallbackInfo ci) {
		this.getWeatherData();
	}
}

