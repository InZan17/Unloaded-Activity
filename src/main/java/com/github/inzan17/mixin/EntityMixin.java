package com.github.inzan17.mixin;

import com.github.inzan17.SimulateEntity;
import com.github.inzan17.TimeMachine;
import com.github.inzan17.UnloadedActivity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.util.Nameable;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static java.lang.Long.max;


@Mixin(value = Entity.class, priority = 999)
public abstract class EntityMixin implements Nameable, EntityLike, CommandOutput, SimulateEntity {

    private long lastTick = 0;

    @Shadow public World world;
    @Inject(at = @At("HEAD"), method = "tick")
    public void tickMovement(CallbackInfo ci) {

        World world = this.world;

        if (world.isClient())
            return;

        long currentTime = world.getTimeOfDay();

        if (this.lastTick != 0) {

            long timeDifference = max(currentTime - this.lastTick,0);

            int differenceThreshold = UnloadedActivity.instance.config.tickDifferenceThreshold;

            if (timeDifference > differenceThreshold)
                TimeMachine.simulateEntity((Entity)(Object)this, timeDifference);
        }
        this.lastTick = currentTime;
    }

    @Inject( at = @At("RETURN"), method = "<init>")
    private void init(EntityType type, World world, CallbackInfo ci) {
        this.lastTick = world.getTimeOfDay();
    }

    @Inject( at = @At("RETURN"), method = "writeNbt")
    private void writeNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        NbtCompound returnedNbt = cir.getReturnValue();

        NbtCompound entityData = new NbtCompound();

        entityData.putLong("last_tick", this.lastTick);

        returnedNbt.put("unloaded_activity", entityData);
    }

    @Inject(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V", shift = At.Shift.AFTER))
    private void readNbt(NbtCompound nbtCompound, CallbackInfo ci) {
        NbtCompound entityData = nbtCompound.getCompound("unloaded_activity");

        boolean isEmpty = entityData.isEmpty();

        if (!isEmpty) {
            this.lastTick = entityData.getLong("last_tick");
        }

        if (UnloadedActivity.instance.config.convertCCAData && isEmpty) {
            NbtCompound cardinalData = nbtCompound.getCompound("cardinal_components");

            if (!cardinalData.isEmpty()) {
                NbtCompound lastEntityTick = cardinalData.getCompound("unloadedactivity:last-entity-tick");
                if (!lastEntityTick.isEmpty()) {
                    this.lastTick = lastEntityTick.getLong("last-tick");
                }

                // This is so that cardinal components doesn't start sending warnings.
                cardinalData.remove("unloadedactivity:last-entity-tick");
            }
        }

        if (this.lastTick == 0) {
            this.lastTick = world.getTimeOfDay();
        }
    }
}
