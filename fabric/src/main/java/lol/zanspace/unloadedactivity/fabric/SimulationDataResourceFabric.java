package lol.zanspace.unloadedactivity.fabric;

import lol.zanspace.unloadedactivity.datapack.SimulationDataResource;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

public class SimulationDataResourceFabric extends SimulationDataResource implements IdentifiableResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return new ResourceLocation("unloaded_activity", "simulate_data");
    }
}
