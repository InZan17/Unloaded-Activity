package lol.zanspace.unloadedactivity;

import lol.zanspace.unloadedactivity.config.UnloadedActivityConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;

import static com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg;
import static com.mojang.brigadier.arguments.DoubleArgumentType.getDouble;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.LongArgumentType.getLong;
import static com.mojang.brigadier.arguments.LongArgumentType.longArg;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

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

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandManager.RegistrationEnvironment environment, CommandRegistryAccess registryAccess) {
        dispatcher.register(
            literal("unloadedactivity").requires(source -> source.hasPermissionLevel(4)).then(
                literal("benchmark").then(
                    argument("method", string()).then(
                        argument("trials", integer()).then(
                            argument("attempts", longArg()).then(
                                argument("maxOccurrences", integer()).then(
                                    argument("odds", doubleArg()).executes(context -> {

                                        java.lang.reflect.Method method;

                                        try {
                                            method = Utils.class.getMethod(getString(context, "method"), long.class, double.class, int.class, Random.class);
                                        } catch (NoSuchMethodException e) {
                                            context.getSource().sendMessage(Text.literal("No such method."));
                                            return 0;
                                        }

                                        int trials = getInteger(context, "trials");
                                        long attempts = getLong(context, "attempts");
                                        int maxOccurrences = getInteger(context, "maxOccurrences");
                                        double odds = getDouble(context, "odds");

                                        Random random = context.getSource().getWorld().random;

                                        long now = Instant.now().toEpochMilli();

                                        for (int i = 0; i<trials; ++i) {
                                            try {
                                                long newAttempts = attempts > 0L ? attempts : random.nextBetween(10_000, 100_000_000);
                                                int newMaxOccurrences = maxOccurrences > 0 ? maxOccurrences : random.nextBetween(1, 100);
                                                double newOdds = odds > 0.0 ? odds : random.nextDouble();
                                                method.invoke(Utils.class, newAttempts, newOdds, newMaxOccurrences, random);
                                            } catch (IllegalAccessException e) {
                                                throw new RuntimeException(e);
                                            } catch (InvocationTargetException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                        long difference = Instant.now().toEpochMilli() - now;

                                        float avg = (float)difference/(float)trials;
                                        context.getSource().sendMessage(Text.literal("Total: "+difference + "ms\nAverage: " + avg + "ms"));
                                        return 1;
                                    })
                                )
                            )
                        )
                    )
                )
            )
        );
    }
}
