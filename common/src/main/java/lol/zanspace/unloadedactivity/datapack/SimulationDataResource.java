package lol.zanspace.unloadedactivity.datapack;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;

import java.util.*;

public class SimulationDataResource extends SimpleJsonResourceReloadListener {

    private static final String ID = "simulate_info";

    private static final Gson GSON = new GsonBuilder().create();

    public static final Map<ResourceLocation, SimulationData> TAG_MAP = new HashMap<>();
    public static final Map<ResourceLocation, SimulationData> BLOCK_MAP = new HashMap<>();

    public static final SimulationDataResource INSTANCE = new SimulationDataResource();

    public SimulationDataResource() {
        super(GSON, ID);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {

        TAG_MAP.clear();
        BLOCK_MAP.clear();

        Map<ResourceLocation, List<SimulationData>> tagData = new HashMap<>();
        Map<ResourceLocation, List<SimulationData>> blockData = new HashMap<>();

        object.forEach((key, jsonElement) -> {
            try {
                for (var blockOrTagEntry : jsonElement.getAsJsonObject().entrySet()) {
                    String blockOrTag = blockOrTagEntry.getKey();
                    UnloadedActivity.LOGGER.info(blockOrTag);
                    boolean isTag = blockOrTag.startsWith("#");
                    if (isTag) {
                        blockOrTag = blockOrTag.substring(1);
                    }
                    var id = ResourceLocation.tryParse(blockOrTag);

                    SimulationData simulationData = new SimulationData();

                    for (var propertyEntry : blockOrTagEntry.getValue().getAsJsonObject().entrySet()) {

                        JsonObject jsonSimulationData = propertyEntry.getValue().getAsJsonObject();

                        SimulationData.SimulateProperty simulateProperty = new SimulationData.SimulateProperty();

                        {
                            JsonElement value = jsonSimulationData.get("property_type");
                            if (value != null) {
                                simulateProperty.propertyType = Optional.of(value.getAsString());
                            }
                        }

                        {
                            JsonElement value = jsonSimulationData.get("advance_probability");
                            if (value != null) {
                                simulateProperty.parseAndApplyProbability(value);
                            }
                        }

                        {
                            JsonElement value = jsonSimulationData.get("max_value");
                            if (value != null) {
                                simulateProperty.maxValue = Optional.of(value.getAsInt());
                            }
                        }

                        {
                            JsonElement conditions = jsonSimulationData.get("conditions");
                            if (conditions != null) {
                                JsonArray conditionArray = conditions.getAsJsonArray();
                                for (var conditionValue : conditionArray.asList()) {
                                    simulateProperty.parseAndApplyCondition(conditionValue);
                                }
                            }
                        }

                        String propertyName = propertyEntry.getKey();

                        simulationData.propertyMap.put(propertyName, simulateProperty);
                    }

                    if (isTag) {
                        var list = tagData.computeIfAbsent(id, k -> new ArrayList<>());
                        list.add(simulationData);
                    } else {
                        var list = blockData.computeIfAbsent(id, k -> new ArrayList<>());
                        list.add(simulationData);
                    }
                }

            } catch(Exception e) {
                UnloadedActivity.LOGGER.error("{}\n{}", key, e);
            }
        });

        for (Map.Entry<ResourceLocation, List<SimulationData>> tagEntry : tagData.entrySet()) {
            List<SimulationData> dataList = tagEntry.getValue();

            if (dataList.isEmpty())
                continue;

            ResourceLocation id = tagEntry.getKey();
            SimulationData finalSimulationData = new SimulationData();

            for (SimulationData simulationData : dataList) {
                finalSimulationData.absorb(simulationData);
            }

            TAG_MAP.put(id, finalSimulationData);
        }


        for (Map.Entry<ResourceLocation, List<SimulationData>> blockEntry : blockData.entrySet()) {
            List<SimulationData> dataList = blockEntry.getValue();

            if (dataList.isEmpty())
                continue;

            ResourceLocation id = blockEntry.getKey();
            SimulationData finalSimulationData = new SimulationData();

            for (SimulationData simulationData : dataList) {
                finalSimulationData.absorb(simulationData);
            }

            BLOCK_MAP.put(id, finalSimulationData);
        }

        UnloadedActivity.LOGGER.info("Tag entries: " + TAG_MAP.keySet().toString());
    }
}
