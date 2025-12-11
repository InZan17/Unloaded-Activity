package lol.zanspace.unloadedactivity;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lol.zanspace.unloadedactivity.config.UnloadedActivityConfig;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

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

#if MC_VER >= MC_1_21_11
import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;
#endif

public class UnloadedActivityCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder commandBuilder = literal("unloadedactivity").requires(source -> {
            // If it's single player then the host should have access to the commands. Otherwise, only people with permission level 4 have access to them.

            #if MC_VER >= MC_1_21_11
            if (source.getPermissions().hasPermission(new Permission.Level(PermissionLevel.fromLevel(4)))) return true;
            #else
            if (source.hasPermissionLevel(4)) return true;
            #endif

            ServerPlayerEntity player = source.getPlayer();
            if (player == null) return false;
            #if MC_VER >= MC_1_21_10
            return source.getServer().isHost(player.getPlayerConfigEntry());
            #else
            return source.getServer().isHost(player.getGameProfile());
            #endif
        });

        addConfigs(commandBuilder);
        addBenchmark(commandBuilder);

        dispatcher.register(commandBuilder);
    }

    public static void addConfigs(LiteralArgumentBuilder commandBuilder) {
        LiteralArgumentBuilder configBuilder = literal("config");

        for (UnloadedActivityConfig.ConfigOption<?> configOption : UnloadedActivity.config.configOptions) {
            configBuilder.then(literal(configOption.name).executes(context ->
                executeConfigGet(context.getSource(), configOption)
            ).then(argument("value", configOption.argumentType).executes(context ->
                executeConfigSet(context, configOption)
            )));
        }

        commandBuilder.then(configBuilder);
    }

    static <T> int executeConfigSet(CommandContext<ServerCommandSource> context, UnloadedActivityConfig.ConfigOption<T> configOption) {
        T value = context.getArgument("value", configOption.tClass);
        configOption.setter.accept(value);
        UnloadedActivity.saveConfig();
        #if MC_VER >= MC_1_20_1
        context.getSource().sendFeedback(() -> Text.literal(configOption.name + " has been set to: " + value), true);
        #else
        context.getSource().sendFeedback(Text.literal(configOption.name + " has been set to: " + value), true);
        #endif
        return 0;
    }

    static <T> int executeConfigGet(ServerCommandSource source, UnloadedActivityConfig.ConfigOption<T> configOption) {
        #if MC_VER >= MC_1_20_1
        source.sendFeedback(() -> Text.literal(configOption.name + " is currently set to: " + configOption.getter.apply(null)), false);
        #else
        source.sendFeedback(Text.literal(configOption.name + " is currently set to: " + configOption.getter.apply(null)), false);
        #endif
        return 0;
    }

    public static void addBenchmark(LiteralArgumentBuilder commandBuilder) {
        commandBuilder.then(
            literal("benchmark").requires(source ->
            #if MC_VER >= MC_1_21_11
                source.getPermissions().hasPermission(new Permission.Level(PermissionLevel.fromLevel(4)))
            #else
                source.hasPermissionLevel(4)
            #endif
            ).then(
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
        );
    }
}
