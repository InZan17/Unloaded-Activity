package com.github.inzan123.mixin;

import com.github.inzan123.LongComponent;
import com.github.inzan123.SimulateEntity;
import com.github.inzan123.TimeMachine;
import com.github.inzan123.UnloadedActivity;
import dev.onyxstudios.cca.api.v3.component.ComponentAccess;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.util.Nameable;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.github.inzan123.MyComponents.LASTENTITYTICK;
import static java.lang.Long.max;

@Mixin(Entity.class)
public abstract class EntityMixin implements Nameable, EntityLike, CommandOutput, SimulateEntity, ComponentAccess {

    @Shadow public World world;
    @Inject(at = @At("HEAD"), method = "tick")
    public void tickMovement(CallbackInfo ci) {

        World world = this.world;

        if (world.isClient())
            return;

        LongComponent lastTick = this.getComponent(LASTENTITYTICK);

        long currentTime = world.getTimeOfDay();

        if (lastTick.getValue() != 0) {

            long timeDifference = max(currentTime - lastTick.getValue(),0);

            int differenceThreshold = UnloadedActivity.instance.config.tickDifferenceThreshold;

            if (timeDifference > differenceThreshold)
                TimeMachine.simulateEntity((Entity)(Object)this, timeDifference);
        }
        lastTick.setValue(currentTime);
    }
}
