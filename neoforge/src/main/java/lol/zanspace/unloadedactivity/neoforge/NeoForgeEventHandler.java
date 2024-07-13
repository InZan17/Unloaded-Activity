package lol.zanspace.unloadedactivity.neoforge;

import lol.zanspace.unloadedactivity.UnloadedActivity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class NeoForgeEventHandler {
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        UnloadedActivity.registerCommands(event.getDispatcher(), event.getCommandSelection(), event.getBuildContext());
    }
}
