package com.github.inzan17.fabric;

import com.github.inzan17.config.UnloadedActivityClothScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class UnloadedActivityModMenu implements ModMenuApi  {

    UnloadedActivityClothScreen clothScreen = new UnloadedActivityClothScreen();

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> clothScreen.getScreen(parent, MinecraftClient.getInstance().world != null);
    }

}