package lol.zanspace.unloadedactivity.neoforge;

import lol.zanspace.unloadedactivity.config.UnloadedActivityClothScreen;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import lol.zanspace.unloadedactivity.UnloadedActivity;
#if MC_VER <= MC_1_20_4
import net.neoforged.neoforge.client.ConfigScreenHandler;
#else
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
#endif
import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

@Mod(UnloadedActivity.MOD_ID)
public final class UnloadedActivityNeoForge {
    public UnloadedActivityNeoForge() {
        UnloadedActivity.init();

        EVENT_BUS.register(new NeoForgeEventHandler());

        if (ModList.get().isLoaded("cloth_config")) {
            #if MC_VER <= MC_1_20_4
            ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, screen) ->
                    new UnloadedActivityClothScreen().getScreen(screen, mc.world != null))
            );
            #else
            ModLoadingContext.get().registerExtensionPoint(
                IConfigScreenFactory.class,
                () -> (mc, screen) -> new UnloadedActivityClothScreen().getScreen(screen, mc.world != null)
            );
            #endif
        }
    }
}
