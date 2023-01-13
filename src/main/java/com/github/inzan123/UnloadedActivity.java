package com.github.inzan123;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnloadedActivity implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("unloadedactivity");

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");
	}

}
