package com.github.inzan17.neoforge;

import com.github.inzan17.config.UnloadedActivityClothScreen;
import net.minecraft.client.MinecraftClient;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import com.github.inzan17.UnloadedActivity;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.client.ConfigScreenHandler;

@Mod(UnloadedActivity.MOD_ID)
public final class UnloadedActivityNeoForge {
    public UnloadedActivityNeoForge() {
        UnloadedActivity.init();

        if (ModList.get().isLoaded("cloth_config")) {
            ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, screen) ->
                        new UnloadedActivityClothScreen().getScreen(screen, mc.world != null))
            );
        }
    }
}
