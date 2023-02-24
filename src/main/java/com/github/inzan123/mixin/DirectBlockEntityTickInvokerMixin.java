package com.github.inzan123.mixin;

import com.github.inzan123.TimeMachine;
import com.github.inzan123.LongComponent;
import com.github.inzan123.UnloadedActivity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.BlendingData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static com.github.inzan123.MyComponents.LASTBLOCKENTITYTICK;
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

        LongComponent lastTick = this.blockEntity.getComponent(LASTBLOCKENTITYTICK);

        long currentTime = world.getTimeOfDay();

        if (lastTick.getValue() != 0) {

            long timeDifference = max(currentTime - lastTick.getValue(),0);

            if (timeDifference > 20)
                TimeMachine.simulateBlockEntity(world, ((BlockEntity)this.blockEntity).getPos(), blockState, this.blockEntity, timeDifference);
        }
        lastTick.setValue(currentTime);
    }
}