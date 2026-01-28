package lol.zanspace.unloadedactivity.datapack;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import lol.zanspace.unloadedactivity.UnloadedActivity;
#if MC_VER >= MC_1_21_11
import net.minecraft.resources.Identifier;
#else
import net.minecraft.resources.ResourceLocation;
#endif
#if MC_VER >= MC_1_21_4
import net.minecraft.resources.FileToIdConverter;
#endif
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.*;

public class SimulationDataResource extends SimpleJsonResourceReloadListener #if MC_VER >= MC_1_21_3
<SimulationData>
#endif {

    private static final String BLOCKS_LOCATION = "simulate_info/blocks";
    private static final String TAGS_LOCATION = "simulate_info/tags";

    public static final #if MC_VER >= MC_1_21_11 Identifier #else ResourceLocation #endif BLOCKS_ID = UnloadedActivity.id("simulate_blocks");
    public static final #if MC_VER >= MC_1_21_11 Identifier #else ResourceLocation #endif TAGS_ID = UnloadedActivity.id("simulate_tags");

    public final boolean isBlocks;

    public static final Map<#if MC_VER >= MC_1_21_11 Identifier #else ResourceLocation #endif, SimulationData> TAG_MAP = new HashMap<>();
    public static final Map<#if MC_VER >= MC_1_21_11 Identifier #else ResourceLocation #endif, SimulationData> BLOCK_MAP = new HashMap<>();

    #if MC_VER >= MC_1_21_3
    public SimulationDataResource(boolean isBlocks) {
        super(
            SimulationData.CODEC,
            #if MC_VER >= MC_1_21_4
            FileToIdConverter.json(isBlocks ? BLOCKS_LOCATION : TAGS_LOCATION)
            #else
            isBlocks ? BLOCKS_ID : TAGS_ID
            #endif
        );
        this.isBlocks = isBlocks;
    }
    #else
    public SimulationDataResource(boolean isBlocks) {
        super(new GsonBuilder().create(), isBlocks ? BLOCKS_ID : TAGS_ID);
        this.isBlocks = isBlocks;
    }
    #endif

    #if MC_VER >= MC_1_21_3
    @Override
    protected void apply(
            Map<#if MC_VER >= MC_1_21_11 Identifier #else ResourceLocation #endif, SimulationData> object,
            ResourceManager resourceManager,
            ProfilerFiller profilerFiller
    )
    #else
    @Override
    protected void apply(
        Map<
            #if MC_VER >= MC_1_21_11
            Identifier
            #else
            ResourceLocation
            #endif,
            JsonElement
        > object,
        ResourceManager resourceManager,
        ProfilerFiller profilerFiller
    )
    #endif
    {

        if (this.isBlocks) {
            BLOCK_MAP.clear();
        } else {
            TAG_MAP.clear();
        }

        Map<#if MC_VER >= MC_1_21_11 Identifier #else ResourceLocation #endif, List<SimulationData>> datas = new HashMap<>();

        object.forEach((key, input) -> {
            try {
                #if MC_VER >= MC_1_21_3
                SimulationData simulationData = input;
                #else
                var result = SimulationData.CODEC.decode(com.mojang.serialization.JsonOps.INSTANCE, input);
                if (result.error().isPresent()) {
                    throw new RuntimeException(result.error().get().message());
                }
                SimulationData simulationData = result.result().get().getFirst();
                #endif

                var list = datas.computeIfAbsent(key, k -> new ArrayList<>());
                list.add(simulationData);
            } catch(Exception e) {
                UnloadedActivity.LOGGER.error("{}\n{}", key, e);
            }
        });

        for (Map.Entry<#if MC_VER >= MC_1_21_11 Identifier #else ResourceLocation #endif, List<SimulationData>> tagEntry : datas.entrySet()) {
            List<SimulationData> dataList = tagEntry.getValue();

            if (dataList.isEmpty())
                continue;

            var id = tagEntry.getKey();
            SimulationData finalSimulationData = new SimulationData();

            for (SimulationData simulationData : dataList) {
                finalSimulationData.absorb(simulationData);
            }

            if (this.isBlocks) {
                BLOCK_MAP.put(id, finalSimulationData);
            } else {
                TAG_MAP.put(id, finalSimulationData);
            }
        }

        UnloadedActivity.LOGGER.info("Data entries: " + TAG_MAP.keySet().toString());
    }
}
