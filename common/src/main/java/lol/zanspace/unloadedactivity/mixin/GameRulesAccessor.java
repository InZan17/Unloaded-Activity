package lol.zanspace.unloadedactivity.mixin;

import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(GameRules.class)
public interface GameRulesAccessor {
    @Accessor("rules")
    Map<GameRules.Key<?>, GameRules.Value<?>> unloaded_activity$getRules();
}
