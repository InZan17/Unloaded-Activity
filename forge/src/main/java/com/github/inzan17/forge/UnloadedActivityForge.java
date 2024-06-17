package com.github.inzan17.forge;

import com.github.inzan17.config.UnloadedActivityClothScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

import com.github.inzan17.UnloadedActivity;

@Mod(UnloadedActivity.MOD_ID)
public final class UnloadedActivityForge {
    public UnloadedActivityForge() {
        UnloadedActivity.init();

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandler());

        if (ModList.get().isLoaded("cloth_config")) {
            ModLoadingContext.get().registerExtensionPoint(
                    ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory((mc, screen) ->
                            new UnloadedActivityClothScreen().getScreen(screen, mc.world != null))
            );
        }
    }
}
