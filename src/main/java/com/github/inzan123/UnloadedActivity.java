package com.github.inzan123;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnloadedActivity implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("unloadedactivity");
	public static final MyConfig CONFIG = MyConfig.createAndLoad();
	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");
	}

}
