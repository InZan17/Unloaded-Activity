package com.github.inzan123.mixin;

import com.github.inzan123.ChunkLongComponent;
import com.github.inzan123.SimulateTimePassing;
import com.github.inzan123.UnloadedActivity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;

import static com.github.inzan123.MyComponents.MAGIK;
import static java.lang.Long.max;


@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
	@Inject(at = @At("HEAD"), method = "tickChunk")
	private void tickChunk(WorldChunk chunk, int randomTickSpeed, CallbackInfo info) {

		ServerWorld world = (ServerWorld)(Object)this;

		ChunkLongComponent lastTick = chunk.getComponent(MAGIK);

		long currentTime = world.getTimeOfDay();

		if (lastTick.getValue() != 0) { //either new chunk or hasn't been loaded since mod was installed (until now)

			long timeDifference = max(currentTime - lastTick.getValue(),0);

			if (timeDifference > 20) {

				long now = 0;
				if (UnloadedActivity.CONFIG.debugLogs()) now = Instant.now().toEpochMilli();

				int minY = world.getBottomY();
				int maxY = world.getTopY();

				if (UnloadedActivity.CONFIG.randomizeXZBlockPicks()) {
					ArrayList<BlockPos> blockPosArray = new ArrayList<BlockPos>();

					for (int z=0; z<16;z++) {
						for (int x=0; x<16;x++) {
							blockPosArray.add(new BlockPos(x,0,z));
						}
					}

					Collections.shuffle(blockPosArray);

					for (int y=minY; y<maxY;y++) {
						for (int i=0; i<blockPosArray.size(); i++) {
							BlockPos position = new BlockPos(blockPosArray.get(i).getX(),y,blockPosArray.get(i).getZ());
							simulateTime(position, chunk, world, timeDifference, randomTickSpeed);
						}
					}
				} else {
					for (int z=0; z<16;z++) {
						for (int x=0; x<16;x++) {
							for (int y=minY; y<maxY;y++) {
								BlockPos position = new BlockPos(x,y,z);
								simulateTime(position, chunk, world, timeDifference, randomTickSpeed);
							}
						}
					}
				}

				if (UnloadedActivity.CONFIG.debugLogs()) UnloadedActivity.LOGGER.info("Milliseconds to loop through chunk: " + (Instant.now().toEpochMilli() - now));
			}
		}

		lastTick.setValue(currentTime);
	}

	private void simulateTime(BlockPos position, WorldChunk chunk, ServerWorld world, long timeDifference, int randomTickSpeed) {
		BlockState state = chunk.getBlockState(position);
		Block block = state.getBlock();
		if (block instanceof SimulateTimePassing) {
			SimulateTimePassing tickSimulator = (SimulateTimePassing) block;
			ChunkPos chunkPos = chunk.getPos();
			BlockPos notChunkBlockPos = position.add(new BlockPos(chunkPos.x*16,0,chunkPos.z*16));
			tickSimulator.simulateTime(state, world, notChunkBlockPos, world.random, timeDifference, randomTickSpeed);
		}
	}

}

