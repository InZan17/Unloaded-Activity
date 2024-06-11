package com.github.inzan17.mixin;

import com.github.inzan17.interfaces.BlockEntityTimeData;
import com.github.inzan17.interfaces.SimulateBlockEntity;
import com.github.inzan17.UnloadedActivity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockEntity.class, priority = 999)
public abstract class BlockEntityMixin implements SimulateBlockEntity, BlockEntityTimeData {
    long lastTick = 0;

    @Override
    public long getLastTick() {
        return this.lastTick;
    }

    @Override
    public void setLastTick(long tick) {
        this.lastTick = tick;
    }

    @Shadow
    public World getWorld() {
        return null;
    }

    @Shadow
    public boolean hasWorld() {
        return true;
    }

    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void writeNbt(NbtCompound nbtCompound, CallbackInfo ci) {
        NbtCompound blockData = new NbtCompound();

        blockData.putLong("last_tick", this.lastTick);

        nbtCompound.put("unloaded_activity", blockData);
    }

    @Inject(method = "readNbt", at = @At(value = "RETURN"))
    private void readNbt(NbtCompound nbtCompound, CallbackInfo ci) {
        NbtCompound blockData = nbtCompound.getCompound("unloaded_activity");

        boolean isEmpty = blockData.isEmpty();

        if (!isEmpty) {
            this.lastTick = blockData.getLong("last_tick");
        }

        if (UnloadedActivity.config.convertCCAData && isEmpty) {
            NbtCompound cardinalData = nbtCompound.getCompound("cardinal_components");

            if (!cardinalData.isEmpty()) {
                NbtCompound lastEntityTick = cardinalData.getCompound("unloadedactivity:last-blockentity-tick");
                if (!lastEntityTick.isEmpty()) {
                    this.lastTick = lastEntityTick.getLong("last-tick");
                }

                // This is so that cardinal components doesn't start sending warnings.
                cardinalData.remove("unloadedactivity:last-blockentity-tick");
            }
        }

        if (this.lastTick == 0 && this.hasWorld()) {
            this.lastTick = this.getWorld().getTimeOfDay();
        }
    }
}