package com.github.inzan123.config;

import com.github.inzan123.UnloadedActivity;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
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
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("text.config.unloaded-activity.category.general"));
        ConfigCategory chunks = builder.getOrCreateCategory(Text.translatable("text.config.unloaded-activity.category.chunks"));
        ConfigCategory blockEntities = builder.getOrCreateCategory(Text.translatable("text.config.unloaded-activity.category.blockEntities"));
        ConfigCategory Entities = builder.getOrCreateCategory(Text.translatable("text.config.unloaded-activity.category.entities"));
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
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.rememberBlockPositions"), config.rememberBlockPositions)
                        .setDefaultValue(false)
                        .setSaveConsumer(newValue -> config.rememberBlockPositions = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.rememberBlockPositions.tooltip"))
                        .build()
        );

        general.addEntry(
                configEntryBuilder
                        .startIntField(Text.translatable("text.config.unloaded-activity.option.tickDifferenceThreshold"), config.tickDifferenceThreshold)
                        .setDefaultValue(100)
                        .setMin(1)
                        .setSaveConsumer(newValue -> config.tickDifferenceThreshold = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.tickDifferenceThreshold.tooltip"))
                        .build()
        );

        chunks.addEntry(
                configEntryBuilder
                        .startIntField(Text.translatable("text.config.unloaded-activity.option.maxChunkUpdates"), config.maxChunkUpdates)
                        .setDefaultValue(8)
                        .setMin(1)
                        .setMax(32767)
                        .setSaveConsumer(newValue -> config.maxChunkUpdates = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.maxChunkUpdates.tooltip"))
                        .build()
        );

        chunks.addEntry(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.multiplyMaxChunkUpdatesPerPlayer"), config.multiplyMaxChunkUpdatesPerPlayer)
                        .setDefaultValue(false)
                        .setSaveConsumer(newValue -> config.multiplyMaxChunkUpdatesPerPlayer = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.multiplyMaxChunkUpdatesPerPlayer.tooltip"))
                        .build()
        );

        chunks.addEntry(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.updateAllChunksWhenSleep"), config.updateAllChunksWhenSleep)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.updateAllChunksWhenSleep = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.updateAllChunksWhenSleep.tooltip"))
                        .build()
        );

        chunks.addEntry(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.enableRandomTicks"), config.enableRandomTicks)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.enableRandomTicks = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.enableRandomTicks.tooltip"))
                        .build()
        );

        SubCategoryBuilder subRandomTicks = configEntryBuilder.startSubCategory(Text.translatable("text.config.unloaded-activity.category.chunks.randomTicks"));

        subRandomTicks.add(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.growSaplings"), config.growSaplings)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.growSaplings = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.growSaplings.tooltip"))
                        .build()
        );

        subRandomTicks.add(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.growCrops"), config.growCrops)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.growCrops = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.growCrops.tooltip"))
                        .build()
        );

        subRandomTicks.add(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.growStems"), config.growStems)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.growStems = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.growStems.tooltip"))
                        .build()
        );

        subRandomTicks.add(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.growSweetBerries"), config.growSweetBerries)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.growSweetBerries = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.growSweetBerries.tooltip"))
                        .build()
        );

        subRandomTicks.add(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.growCocoa"), config.growCocoa)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.growCocoa = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.growCocoa.tooltip"))
                        .build()
        );

        subRandomTicks.add(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.growSugarCane"), config.growSugarCane)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.growSugarCane = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.growSugarCane.tooltip"))
                        .build()
        );

        subRandomTicks.add(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.ageCopper"), config.ageCopper)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.ageCopper = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.ageCopper.tooltip"))
                        .build()
        );

        subRandomTicks.add(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.decayLeaves"), config.decayLeaves)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.decayLeaves = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.decayLeaves.tooltip"))
                        .build()
        );

        subRandomTicks.add(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.growAmethyst"), config.growAmethyst)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.growAmethyst = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.growAmethyst.tooltip"))
                        .build()
        );

        subRandomTicks.add(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.growPlantStems"), config.growPlantStems)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.growPlantStems = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.growPlantStems.tooltip"))
                        .build()
        );

        subRandomTicks.add(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.hatchTurtleEggs"), config.hatchTurtleEggs)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.hatchTurtleEggs = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.hatchTurtleEggs.tooltip"))
                        .build()
        );

        subRandomTicks.add(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.accurateTurtleAgeAfterHatch"), config.accurateTurtleAgeAfterHatch)
                        .setDefaultValue(false)
                        .setSaveConsumer(newValue -> config.accurateTurtleAgeAfterHatch = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.accurateTurtleAgeAfterHatch.tooltip"))
                        .build()
        );

        subRandomTicks.add(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.growBamboo"), config.growBamboo)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.growBamboo = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.growBamboo.tooltip"))
                        .build()
        );

        chunks.addEntry(subRandomTicks.build());

        chunks.addEntry(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.enablePrecipitationTicks"), config.enablePrecipitationTicks)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.enablePrecipitationTicks = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.enablePrecipitationTicks.tooltip"))
                        .build()
        );

        SubCategoryBuilder subPrecipitationTicks = configEntryBuilder.startSubCategory(Text.translatable("text.config.unloaded-activity.category.chunks.precipitationTicks"));

        subPrecipitationTicks.add(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.accumulateSnow"), config.accumulateSnow)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.accumulateSnow = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.accumulateSnow.tooltip"))
                        .build()
        );

        subPrecipitationTicks.add(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.waterFreezing"), config.waterFreezing)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.waterFreezing = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.waterFreezing.tooltip"))
                        .build()
        );

        chunks.addEntry(subPrecipitationTicks.build());

        blockEntities.addEntry(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.enableBlockEntities"), config.enableBlockEntities)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.enableBlockEntities = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.enableBlockEntities.tooltip"))
                        .build()
        );

        blockEntities.addEntry(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.updateFurnace"), config.updateFurnace)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.updateFurnace = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.updateFurnace.tooltip"))
                        .build()
        );

        Entities.addEntry(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.enableEntities"), config.enableEntities)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.enableEntities = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.enableEntities.tooltip"))
                        .build()
        );

        Entities.addEntry(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.ageEntities"), config.ageEntities)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.ageEntities = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.ageEntities.tooltip"))
                        .build()
        );

        return builder.setTransparentBackground(isTransparent).build();

    }

}