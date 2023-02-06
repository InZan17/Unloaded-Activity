package com.github.inzan123.mixin;

import com.github.inzan123.ChunkLongComponent;
import com.github.inzan123.SimulateTimePassing;
import com.github.inzan123.TimeMachine;
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

import static com.github.inzan123.MyComponents.LASTTICK;
import static java.lang.Long.max;


@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
	@Inject(at = @At("HEAD"), method = "tickChunk")
	private void tickChunk(WorldChunk chunk, int randomTickSpeed, CallbackInfo info) {

		ServerWorld world = (ServerWorld)(Object)this;

		ChunkLongComponent lastTick = chunk.getComponent(LASTTICK);

		long currentTime = world.getTimeOfDay();

		if (lastTick.getValue() != 0) { //either new chunk or hasn't been loaded since mod was installed (until now)

			long timeDifference = max(currentTime - lastTick.getValue(),0);

			if (timeDifference > 20)
				TimeMachine.simulateRandomTicks(timeDifference, world, chunk, randomTickSpeed);

		}

		lastTick.setValue(currentTime);
	}
}

