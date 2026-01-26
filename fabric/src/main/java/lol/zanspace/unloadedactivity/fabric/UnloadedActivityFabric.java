package lol.zanspace.unloadedactivity.fabric;

import lol.zanspace.unloadedactivity.UnloadedActivity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public class UnloadedActivityFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		UnloadedActivity.init();

		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimulationDataResourceFabric());
	}
}
