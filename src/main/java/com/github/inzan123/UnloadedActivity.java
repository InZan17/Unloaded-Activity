package com.github.inzan123;

import com.github.inzan123.config.UnloadedActivityConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.include.com.google.gson.Gson;
import org.spongepowered.include.com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class UnloadedActivity implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("unloaded-activity");
	public UnloadedActivityConfig config;
	public static UnloadedActivity instance;
	@Override
	public void onInitialize() {
		loadConfig();
		LOGGER.info((this.config != null) + "");
		instance = this;
		LOGGER.info("Hello Fabric world!");
	}

	public void loadConfig() {
		LOGGER.info("Loading config.");
		File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "unloaded-activity.json");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		if (configFile.exists()) {
			try {
				FileReader fileReader = new FileReader(configFile);
				config = gson.fromJson(fileReader, UnloadedActivityConfig.class);
				fileReader.close();
			} catch (IOException e) {
				LOGGER.warn("Error loading UnloadedActivity configs: " + e.getLocalizedMessage());
			}
		}

		if (config == null) {
			config = new UnloadedActivityConfig();
			saveConfig();
		}
	}

	public void saveConfig() {
		File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "unloaded-activity.json");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		if (!configFile.getParentFile().exists())
			configFile.getParentFile().mkdir();

		try {
			FileWriter fileWriter = new FileWriter(configFile);
			gson.toJson(config, fileWriter);
			fileWriter.close();
		} catch (IOException e) {
			LOGGER.warn("Error saving UnloadedActivity configs: " + e.getLocalizedMessage());
		}
	}


}
