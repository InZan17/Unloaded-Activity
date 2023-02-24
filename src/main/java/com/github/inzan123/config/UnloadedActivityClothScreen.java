package com.github.inzan123.config;

import com.github.inzan123.UnloadedActivity;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class UnloadedActivityClothScreen {
    public Screen getScreen(Screen parent, boolean isTransparent) {
        UnloadedActivityConfig config = UnloadedActivity.instance.config;
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("text.config.unloaded-activity.title"));

        builder.setDefaultBackgroundTexture(new Identifier("minecraft:textures/block/dark_oak_planks.png"));
        builder.setSavingRunnable(() -> UnloadedActivity.instance.saveConfig());
        ConfigCategory general = builder.getOrCreateCategory(Text.of("general"));
        ConfigEntryBuilder configEntryBuilder = builder.entryBuilder();

        general.addEntry(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.debugLogs"), config.debugLogs)
                        .setDefaultValue(false)
                        .setSaveConsumer(newValue -> config.debugLogs = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.debugLogs.tooltip"))
                        .build()
        );

        general.addEntry(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.randomizeBlockUpdates"), config.randomizeBlockUpdates)
                        .setDefaultValue(false)
                        .setSaveConsumer(newValue -> config.randomizeBlockUpdates = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.randomizeBlockUpdates.tooltip"))
                        .build()
        );

        general.addEntry(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.growSaplings"), config.growSaplings)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.growSaplings = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.growSaplings.tooltip"))
                        .build()
        );

        general.addEntry(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.growCrops"), config.growCrops)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.growCrops = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.growCrops.tooltip"))
                        .build()
        );

        general.addEntry(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.growStems"), config.growStems)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.growStems = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.growStems.tooltip"))
                        .build()
        );

        general.addEntry(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.growSweetBerries"), config.growSweetBerries)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.growSweetBerries = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.growSweetBerries.tooltip"))
                        .build()
        );

        general.addEntry(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.growCocoa"), config.growCocoa)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.growCocoa = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.growCocoa.tooltip"))
                        .build()
        );

        general.addEntry(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.updateFurnace"), config.updateFurnace)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.updateFurnace = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.updateFurnace.tooltip"))
                        .build()
        );

        return builder.setTransparentBackground(isTransparent).build();

    }

}