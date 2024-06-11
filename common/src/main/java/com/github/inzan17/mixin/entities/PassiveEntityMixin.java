package com.github.inzan17.mixin.entities;

import com.github.inzan17.UnloadedActivity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import static java.lang.Math.max;
import static java.lang.Math.min;


@Mixin(PassiveEntity.class)
public abstract class PassiveEntityMixin extends PathAwareEntity {
    protected PassiveEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean canSimulate() {
        return true;
    }

    public boolean shouldSimulate(Entity entity) {
        if (!UnloadedActivity.config.ageEntities) return false;
        if (entity.isRemoved()) return false;
        if (!entity.isAlive()) return false;
        if (((PassiveEntity)entity).getBreedingAge() == 0) return false;
        return true;
    }

    @Override
    public void simulateTime(Entity entity, long timeDifference) {

        super.simulateTime(entity, timeDifference);

        if (!shouldSimulate(entity))
            return;

        PassiveEntity passiveEntity = (PassiveEntity)entity;
        int age = passiveEntity.getBreedingAge();
        if (age < 0) {
            passiveEntity.setBreedingAge((int)min(0,age+timeDifference));
        } else if (age > 0) {
            passiveEntity.setBreedingAge((int)max(0,age-timeDifference));
        }
    }
}
