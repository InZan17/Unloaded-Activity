package com.github.inzan17.fabric;

import com.github.inzan17.UnloadedActivity;
import net.fabricmc.api.ModInitializer;

public class UnloadedActivityFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		UnloadedActivity.init();
	}
}
