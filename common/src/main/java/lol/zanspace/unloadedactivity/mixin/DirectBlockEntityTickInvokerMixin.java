package lol.zanspace.unloadedactivity.mixin;

import lol.zanspace.unloadedactivity.TimeMachine;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static java.lang.Long.max;


@Mixin(targets = "net.minecraft.world.chunk.WorldChunk$DirectBlockEntityTickInvoker")
public abstract class DirectBlockEntityTickInvokerMixin<T extends BlockEntity> {
    @Shadow @Final private T blockEntity;
    @Shadow @Final WorldChunk worldChunk;

    @Inject(
            at = @At(
                    value="INVOKE",
                    target="Lnet/minecraft/block/entity/BlockEntityTicker;tick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/BlockEntity;)V"
            ),
            method = "tick",
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void blockEntityTick(CallbackInfo ci, BlockPos blockPos, Profiler profiler, BlockState blockState) {

        World world = this.blockEntity.getWorld();

        if (world.isClient())
            return;

        long lastTick = this.blockEntity.getLastTick();

        long currentTime = world.getTimeOfDay();

        if (lastTick != 0) {

            long timeDifference = max(currentTime - lastTick,0);

            int differenceThreshold = UnloadedActivity.config.tickDifferenceThreshold;

            if (timeDifference > differenceThreshold)
                TimeMachine.simulateBlockEntity(world, this.blockEntity.getPos(), blockState, this.blockEntity, timeDifference);
        }
        this.blockEntity.setLastTick(currentTime);
    }
}