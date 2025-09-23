package lol.zanspace.unloadedactivity.mixin;

import lol.zanspace.unloadedactivity.interfaces.SimulateEntity;
import lol.zanspace.unloadedactivity.TimeMachine;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandOutput;
#if MC_VER > MC_1_21_5
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
#endif
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

            int differenceThreshold = UnloadedActivity.config.tickDifferenceThreshold;

            if (timeDifference > differenceThreshold)
                TimeMachine.simulateEntity((Entity)(Object)this, timeDifference);
        }
        this.lastTick = currentTime;
    }

    @Inject( at = @At("RETURN"), method = "<init>")
    private void init(EntityType type, World world, CallbackInfo ci) {
        this.lastTick = world.getTimeOfDay();
    }

    #if MC_VER <= MC_1_21_5
    @Inject( at = @At("RETURN"), method = "writeNbt")
    private void writeNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        NbtCompound returnedNbt = cir.getReturnValue();

        NbtCompound entityData = new NbtCompound();

        entityData.putLong("last_tick", this.lastTick);

        returnedNbt.put("unloaded_activity", entityData);
    }
    #else
    @Inject(method = "writeData", at = @At("RETURN"))
    private void writeNbt(WriteView nbt, CallbackInfo ci) {
        NbtCompound entityData = new NbtCompound();

        entityData.putLong("last_tick", this.lastTick);

        nbt.put("unloaded_activity", NbtCompound.CODEC, entityData);
    }
    #endif

    #if MC_VER <= MC_1_21_5
    @Inject(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V", shift = At.Shift.AFTER))
    private void readNbt(NbtCompound nbtCompound, CallbackInfo ci) {
    #else
    @Inject(method = "readData", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readCustomData(Lnet/minecraft/storage/ReadView;)V", shift = At.Shift.AFTER))
    private void readNbt(ReadView nbtCompound, CallbackInfo ci) {
    #endif
        #if MC_VER <= MC_1_21_5
        NbtCompound entityData = nbtCompound.getCompound("unloaded_activity")#if MC_VER >= MC_1_21_5 .orElse(new NbtCompound())#endif;
        #else
        NbtCompound entityData = nbtCompound.read("unloaded_activity", NbtCompound.CODEC).orElse(new NbtCompound());
        #endif

        boolean isEmpty = entityData.isEmpty();

        if (!isEmpty) {
            this.lastTick = entityData.getLong("last_tick"#if MC_VER >= MC_1_21_5 , 0#endif);
        }

        if (UnloadedActivity.config.convertCCAData && isEmpty) {
            #if MC_VER <= MC_1_21_5
            NbtCompound cardinalData = nbtCompound.getCompound("cardinal_components")#if MC_VER >= MC_1_21_5 .orElse(new NbtCompound())#endif;
            #else
            NbtCompound cardinalData = nbtCompound.read("cardinal_components", NbtCompound.CODEC).orElse(new NbtCompound());
            #endif

            if (!cardinalData.isEmpty()) {
                NbtCompound lastEntityTick = cardinalData.getCompound("unloadedactivity:last-entity-tick")#if MC_VER >= MC_1_21_5 .orElse(new NbtCompound())#endif;
                if (!lastEntityTick.isEmpty()) {
                    this.lastTick = lastEntityTick.getLong("last-tick"#if MC_VER >= MC_1_21_5 , 0#endif);
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
