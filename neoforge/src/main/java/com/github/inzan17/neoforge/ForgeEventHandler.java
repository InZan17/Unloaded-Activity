package com.github.inzan17.neoforge;

import com.github.inzan17.UnloadedActivity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class ForgeEventHandler {
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        UnloadedActivity.registerCommands(event.getDispatcher(), event.getCommandSelection(), event.getBuildContext());
    }
}
