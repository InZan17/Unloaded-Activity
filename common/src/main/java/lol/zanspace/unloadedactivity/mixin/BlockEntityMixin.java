package lol.zanspace.unloadedactivity.mixin;

import lol.zanspace.unloadedactivity.interfaces.BlockEntityTimeData;
import lol.zanspace.unloadedactivity.interfaces.SimulateBlockEntity;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
#if MC_VER > MC_1_20_4
import net.minecraft.registry.RegistryWrapper;
#endif
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
    #if MC_VER <= MC_1_20_4
    private void writeNbt(NbtCompound nbtCompound, CallbackInfo ci)
    #else
    private void writeNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci)
    #endif
    {
        NbtCompound blockData = new NbtCompound();

        blockData.putLong("last_tick", this.lastTick);

        nbtCompound.put("unloaded_activity", blockData);
    }
    @Inject(method = "readNbt", at = @At(value = "RETURN"))
    #if MC_VER <= MC_1_20_4
    private void readNbt(NbtCompound nbtCompound, CallbackInfo ci)
    #else
    private void readNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci)
    #endif
    {
        NbtCompound blockData = nbtCompound.getCompound("unloaded_activity")#if MC_VER >= MC_1_21_5 .orElse(new NbtCompound())#endif;

        boolean isEmpty = blockData.isEmpty();

        if (!isEmpty) {
            this.lastTick = blockData.getLong("last_tick"#if MC_VER >= MC_1_21_5 , 0#endif);
        }

        if (UnloadedActivity.config.convertCCAData && isEmpty) {
            NbtCompound cardinalData = nbtCompound.getCompound("cardinal_components")#if MC_VER >= MC_1_21_5 .orElse(new NbtCompound())#endif;

            if (!cardinalData.isEmpty()) {
                NbtCompound lastEntityTick = cardinalData.getCompound("unloadedactivity:last-blockentity-tick")#if MC_VER >= MC_1_21_5 .orElse(new NbtCompound())#endif;
                if (!lastEntityTick.isEmpty()) {
                    this.lastTick = lastEntityTick.getLong("last-tick"#if MC_VER >= MC_1_21_5 , 0#endif);
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