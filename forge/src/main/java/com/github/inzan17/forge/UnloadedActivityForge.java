package com.github.inzan17.forge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

import com.github.inzan17.UnloadedActivity;

@Mod(UnloadedActivity.MOD_ID)
public final class UnloadedActivityForge {
    public UnloadedActivityForge() {
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandler());
        UnloadedActivity.init();
    }
}
