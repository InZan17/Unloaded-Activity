package lol.zanspace.unloadedactivity.neoforge;

import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.UnloadedActivityCommand;
import lol.zanspace.unloadedactivity.datapack.SimulationDataResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class NeoForgeEventHandler {
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        UnloadedActivityCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(SimulationDataResource.INSTANCE);
    }
}
