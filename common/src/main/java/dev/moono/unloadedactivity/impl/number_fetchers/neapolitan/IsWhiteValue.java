package dev.moono.unloadedactivity.impl.number_fetchers.neapolitan;

import com.teamabnormals.neapolitan.core.NeapolitanConfig;
import dev.moono.unloadedactivity.api.context.FixedContext;
import dev.moono.unloadedactivity.api.number_fetcher.FixedNumberFetcher;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class IsWhiteValue implements FixedNumberFetcher {

    final Supplier<Integer> whiteStrawberryMinHeight;

    public IsWhiteValue() {
        try {
            // This is actually a ForgeConfigSpec.ConfigValue<Integer>, but it implements Supplier<Boolean>.
            // I use getField so that if there's eventually a fabric port, it will still work and it won't
            // crash because ForgeConfigSpec.ConfigValue doesn't exist.
            @SuppressWarnings("unchecked")
            Supplier<Integer> integerSupplier = (Supplier<Integer>) NeapolitanConfig.Common.class.getField("whiteStrawberryMinHeight").get(NeapolitanConfig.COMMON);
            this.whiteStrawberryMinHeight = integerSupplier;
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }
    @Override
    public Number evaluate(FixedContext context) {
        Level level = (Level)context.getLevel();
        boolean result = (context.getBlockPos().getY() >= this.whiteStrawberryMinHeight.get() && level.dimension() == Level.OVERWORLD) || level.dimension() == Level.END;
        return result ? 1 : 0;
    }
}
