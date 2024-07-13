package lol.zanspace.unloadedactivity.fabric;

import lol.zanspace.unloadedactivity.UnloadedActivity;
import net.fabricmc.api.ModInitializer;

public class UnloadedActivityFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		UnloadedActivity.init();
	}
}
