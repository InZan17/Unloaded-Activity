package com.github.inzan123.mixin;

import com.github.inzan123.LongComponent;
import com.github.inzan123.TimeMachine;
import com.github.inzan123.UnloadedActivity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.github.inzan123.MyComponents.LASTCHUNKTICK;
import static java.lang.Long.max;


@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
	@Inject(at = @At("HEAD"), method = "tickChunk")
	private void tickChunk(WorldChunk chunk, int randomTickSpeed, CallbackInfo info) {

		ServerWorld world = (ServerWorld)(Object)this;

		LongComponent lastTick = chunk.getComponent(LASTCHUNKTICK);

		long currentTime = world.getTimeOfDay();

		if (lastTick.getValue() != 0) {

			long timeDifference = max(currentTime - lastTick.getValue(),0);

			int differenceThreshold = UnloadedActivity.instance.config.tickDifferenceThreshold;

			if (timeDifference > differenceThreshold)
				TimeMachine.simulateRandomTicks(timeDifference, world, chunk, randomTickSpeed);

		}

		lastTick.setValue(currentTime);
	}
}

