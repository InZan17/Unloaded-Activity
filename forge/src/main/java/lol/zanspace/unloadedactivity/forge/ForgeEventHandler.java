package lol.zanspace.unloadedactivity.forge;

import lol.zanspace.unloadedactivity.UnloadedActivity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeEventHandler {
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        UnloadedActivity.registerCommands(event.getDispatcher(), event.getCommandSelection(), event.getBuildContext());
    }
}
