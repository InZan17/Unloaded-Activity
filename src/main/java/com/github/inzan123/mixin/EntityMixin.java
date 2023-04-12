package com.github.inzan123.mixin;

import com.github.inzan123.LongComponent;
import com.github.inzan123.TimeMachine;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.github.inzan123.MyComponents.LASTENTITYTICK;
import static java.lang.Long.max;

@Mixin(Entity.class)
public class EntityMixin{
    @Inject(at = @At("HEAD"), method = "tick")
    public void tickMovement(CallbackInfo ci) {
        Entity entity = (Entity)(Object)this;

        LongComponent lastTick = entity.getComponent(LASTENTITYTICK);

        World world = entity.world;

        long currentTime = world.getTimeOfDay();

        if (lastTick.getValue() != 0) {

            long timeDifference = max(currentTime - lastTick.getValue(),0);

            if (timeDifference > 20)
                TimeMachine.simulateEntity(entity, timeDifference);
        }
        lastTick.setValue(currentTime);
    }
}
