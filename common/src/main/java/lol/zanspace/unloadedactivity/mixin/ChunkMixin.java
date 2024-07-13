package lol.zanspace.unloadedactivity.mixin;

import lol.zanspace.unloadedactivity.interfaces.ChunkTimeData;
import net.minecraft.world.BlockView;
import net.minecraft.world.StructureHolder;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;

@Mixin(Chunk.class)
public abstract class ChunkMixin implements BlockView, BiomeAccess.Storage, StructureHolder, ChunkTimeData {

    // Last time the chunk was ticked.
    long lastTick = 0;

    // If the simulationVersion mismatches with the version in the mod then simulationBlocks will be reset.
    // This is so that if one version of the mod doesn't support a specific block but the next one does,
    // the block will get included once simulationBlocks is reset.
    long simulationVersion = 0;

    // List of block positions that can be simulated.
    ArrayList<Long> simulationBlocks = new ArrayList<>();

    @Override
    public long getLastTick() {
        return this.lastTick;
    }

    @Override
    public void setLastTick(long tick) {
    this.lastTick = tick;
    }

    @Override
    public long getSimulationVersion() {
        return this.simulationVersion;
    }

    @Override
    public void setSimulationVersion(long ver) {
        this.simulationVersion = ver;
    }

    @Override
    public ArrayList<Long> getSimulationBlocks() {
        return simulationBlocks;
    }

    @Override
    public void setSimulationBlocks(ArrayList<Long> positions) {
        this.simulationBlocks = positions;
    }

    @Override
    public void addSimulationBlock(long blockPos) {

        if (this.simulationBlocks.contains(blockPos))
            return;

        this.simulationBlocks.add(blockPos);
    }

    @Override
    public void removeSimulationBlock(long blockPos) {

        int blockPosIndex = this.simulationBlocks.indexOf(blockPos);

        if (blockPosIndex < 0)
            return;

        this.simulationBlocks.remove(blockPosIndex);
    }
}
