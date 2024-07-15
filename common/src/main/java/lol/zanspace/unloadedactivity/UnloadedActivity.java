package lol.zanspace.unloadedactivity;

import lol.zanspace.unloadedactivity.config.UnloadedActivityConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class UnloadedActivity {
    public static final String MOD_ID = "unloaded_activity";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final long chunkSimVer = 1;
    public static UnloadedActivityConfig config;

    public static void init() {
        loadConfig();
        LOGGER.info("Nice weather today, huh?");
    }
    public static void loadConfig() {
        LOGGER.info("Loading config.");
        File configFile = new File(ExpectPlatform.getConfigDirectory().toFile(), "unloaded-activity.json");
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

    public static void saveConfig() {
        File configFile = new File(ExpectPlatform.getConfigDirectory().toFile(), "unloaded-activity.json");
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
