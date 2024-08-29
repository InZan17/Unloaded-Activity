package lol.zanspace.unloadedactivity.config;

import lol.zanspace.unloadedactivity.UnloadedActivity;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.Requirement;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class UnloadedActivityClothScreen {
    public Screen getScreen(Screen parent, boolean isTransparent) {
        UnloadedActivityConfig config = UnloadedActivity.config;
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("text.config.unloaded-activity.title"));

        #if MC_VER >= MC_1_20_6
        isTransparent = true;
        #endif

        #if MC_VER >= MC_1_21_1
        builder.setDefaultBackgroundTexture(Identifier.of("minecraft:textures/block/dark_oak_planks.png"));
        #else
        builder.setDefaultBackgroundTexture(new Identifier("minecraft:textures/block/dark_oak_planks.png"));
        #endif
        builder.setSavingRunnable(() -> UnloadedActivity.saveConfig());
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("text.config.unloaded-activity.category.general"));
        ConfigCategory chunks = builder.getOrCreateCategory(Text.translatable("text.config.unloaded-activity.category.chunks"));
        ConfigCategory blockEntities = builder.getOrCreateCategory(Text.translatable("text.config.unloaded-activity.category.blockEntities"));
        ConfigCategory entities = builder.getOrCreateCategory(Text.translatable("text.config.unloaded-activity.category.entities"));
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
                        .startIntField(Text.translatable("text.config.unloaded-activity.option.maxKnownChunkUpdates"), config.maxKnownChunkUpdates)
                        .setDefaultValue(64)
                        .setMin(1)
                        .setMax(32767)
                        .setSaveConsumer(newValue -> config.maxKnownChunkUpdates = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.maxKnownChunkUpdates.tooltip"))
                        .build()
        );

        chunks.addEntry(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.randomizeBlockUpdates"), config.randomizeBlockUpdates)
                        .setDefaultValue(false)
                        .setSaveConsumer(newValue -> config.randomizeBlockUpdates = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.randomizeBlockUpdates.tooltip"))
                        .build()
        );

        chunks.addEntry(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.rememberBlockPositions"), config.rememberBlockPositions)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.rememberBlockPositions = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.rememberBlockPositions.tooltip"))
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

        BooleanListEntry enableRandomTicks = configEntryBuilder
                .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.enableRandomTicks"), config.enableRandomTicks)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> config.enableRandomTicks = newValue)
                .setTooltip(Text.translatable("text.config.unloaded-activity.option.enableRandomTicks.tooltip"))
                .build();

        chunks.addEntry(enableRandomTicks);

        SubCategoryBuilder subRandomTicks = configEntryBuilder.startSubCategory(Text.translatable("text.config.unloaded-activity.category.chunks.randomTicks")).setRequirement(Requirement.isTrue(enableRandomTicks));

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
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.growCactus"), config.growCactus)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.growCactus = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.growCactus.tooltip"))
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
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.growKelp"), config.growKelp)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.growKelp = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.growKelp.tooltip"))
                        .build()
        );

        subRandomTicks.add(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.growGlowBerries"), config.growGlowBerries)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.growGlowBerries = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.growGlowBerries.tooltip"))
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
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.growBamboo"), config.growBamboo)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.growBamboo = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.growBamboo.tooltip"))
                        .build()
        );

        subRandomTicks.add(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.meltIce"), config.meltIce)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.meltIce = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.meltIce.tooltip"))
                        .build()
        );

        subRandomTicks.add(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.meltSnow"), config.meltSnow)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.meltSnow = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.meltSnow.tooltip"))
                        .build()
        );

        subRandomTicks.add(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.growDripstone"), config.growDripstone)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.growDripstone = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.growDripstone.tooltip"))
                        .build()
        );

        subRandomTicks.add(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.dripstoneFillCauldrons"), config.dripstoneFillCauldrons)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.dripstoneFillCauldrons = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.dripstoneFillCauldrons.tooltip"))
                        .build()
        );

        subRandomTicks.add(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.dripstoneTurnMudToClay"), config.dripstoneTurnMudToClay)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.dripstoneTurnMudToClay = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.dripstoneTurnMudToClay.tooltip"))
                        .build()
        );

        chunks.addEntry(subRandomTicks.build());



        BooleanListEntry enablePrecipitationTicks = configEntryBuilder
            .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.enablePrecipitationTicks"), config.enablePrecipitationTicks)
            .setDefaultValue(true)
            .setSaveConsumer(newValue -> config.enablePrecipitationTicks = newValue)
            .setTooltip(Text.translatable("text.config.unloaded-activity.option.enablePrecipitationTicks.tooltip"))
            .build();

        chunks.addEntry(enablePrecipitationTicks);

        SubCategoryBuilder subPrecipitationTicks = configEntryBuilder.startSubCategory(Text.translatable("text.config.unloaded-activity.category.chunks.precipitationTicks")).setRequirement(Requirement.isTrue(enablePrecipitationTicks));;

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

        subPrecipitationTicks.add(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.weatherFillCauldron"), config.weatherFillCauldron)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.weatherFillCauldron = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.weatherFillCauldron.tooltip"))
                        .build()
        );

        chunks.addEntry(subPrecipitationTicks.build());

        SubCategoryBuilder subAccuracy = configEntryBuilder.startSubCategory(Text.translatable("text.config.unloaded-activity.category.chunks.accuracy")).setRequirement(Requirement.isTrue(enableRandomTicks));

        subAccuracy.add(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.accurateTurtleAgeAfterHatch"), config.accurateTurtleAgeAfterHatch)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.accurateTurtleAgeAfterHatch = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.accurateTurtleAgeAfterHatch.tooltip"))
                        .build()
        );

        chunks.addEntry(subAccuracy.build());

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

        entities.addEntry(
                configEntryBuilder
                        .startBooleanToggle(Text.translatable("text.config.unloaded-activity.option.enableEntities"), config.enableEntities)
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.enableEntities = newValue)
                        .setTooltip(Text.translatable("text.config.unloaded-activity.option.enableEntities.tooltip"))
                        .build()
        );

        entities.addEntry(
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