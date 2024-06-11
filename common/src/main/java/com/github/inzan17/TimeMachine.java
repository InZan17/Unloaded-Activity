package com.github.inzan17;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;

public class TimeMachine {
    public static long simulateChunk(long timeDifference, ServerWorld world, WorldChunk chunk, int randomTickSpeed) {
        if (!UnloadedActivity.config.enableRandomTicks || !UnloadedActivity.config.enablePrecipitationTicks) return 0;
        
        long now = 0;
        if (UnloadedActivity.config.debugLogs) now = Instant.now().toEpochMilli();

        TimeMachine.simulatePrecipitationTicks(timeDifference, world, chunk);
        TimeMachine.simulateRandomTicks(timeDifference, world, chunk, randomTickSpeed);

        long msTime = 0;

        if (UnloadedActivity.config.debugLogs) {
            msTime = Instant.now().toEpochMilli() - now;
            UnloadedActivity.LOGGER.info(msTime + "ms to simulate random ticks on chunk after " + timeDifference + " ticks.");
        };
        return msTime;
    }

    public static void simulatePrecipitationTicks(long timeDifference, ServerWorld world, WorldChunk chunk) {

        if (!UnloadedActivity.config.enablePrecipitationTicks)
            return;

        double precipitationPickChance = 1.0/4096.0; //1/(16*256). 16 for the chance of the chunk doing the tick and 256 for the chance of a block to be picked.

        WorldWeatherData weatherData = world.getWeatherData();

        long timeInWeather = weatherData.getTimeInWeather(timeDifference,world.getTimeOfDay());

        for (int z=0; z<16;z++)
            for (int x=0; x<16;x++) {
                ChunkPos chunkPos = chunk.getPos();
                BlockPos airPos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, new BlockPos(chunkPos.x*16+x,0,chunkPos.z*16+z));
                BlockPos groundPos = airPos.down();
                BlockState airPosState = chunk.getBlockState(airPos);
                BlockState groundPosState = chunk.getBlockState(groundPos);
                Block airPosBlock = airPosState.getBlock();
                Block groundPosBlock = groundPosState.getBlock();
                Biome biome = world.getBiome(airPos).value();
                if (airPosBlock.implementsSimulatePrecTicks())
                    simulateBlockPrecipitationTick(airPos, world, timeDifference, precipitationPickChance, timeInWeather, biome.getPrecipitation(#if MC_VER >= MC_1_19_4 airPos #endif));

                if (groundPosBlock.implementsSimulatePrecTicks())
                    simulateBlockPrecipitationTick(groundPos, world, timeDifference, precipitationPickChance, timeInWeather, biome.getPrecipitation(#if MC_VER >= MC_1_19_4 groundPos #endif));
        }
    }

    public static void simulateBlockPrecipitationTick(BlockPos pos, ServerWorld world, long timeDifference, double precipitationPickChance, long timeInWeather, Biome.Precipitation precipitation) {
        if (!UnloadedActivity.config.enablePrecipitationTicks)
            return;

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (!block.canSimulatePrecTicks(state, world, pos, timeInWeather, precipitation))
            return;

        block.simulatePrecTicks(state, world, pos, timeInWeather, timeDifference, precipitation, precipitationPickChance);
    }

    public static void simulateRandomTicks(long timeDifference, ServerWorld world, WorldChunk chunk, int randomTickSpeed) {

        if (!UnloadedActivity.config.enableRandomTicks)
            return;

        int minY = world.getBottomY();
        int maxY = world.getTopY();

        ArrayList<BlockPos> blockPosArray = new ArrayList<>();

        ArrayList<Long> newSimulationBlocks = new ArrayList<>();

        if (UnloadedActivity.config.rememberBlockPositions && chunk.getSimulationVersion() == UnloadedActivity.chunkSimVer) {

            ArrayList<Long> currentSimulationBlocks = chunk.getSimulationBlocks();

            if (UnloadedActivity.config.debugLogs)
                UnloadedActivity.LOGGER.info("Looping through "+currentSimulationBlocks.size()+" known positions.");

            boolean removedSomething = false;

            for (long longPos : currentSimulationBlocks) {
                BlockPos pos = BlockPos.fromLong(longPos);
                BlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                if (block.implementsSimulateRandTicks()) {
                    newSimulationBlocks.add(longPos);
                    blockPosArray.add(pos);
                } else {
                    removedSomething = true;
                }
            }
            if (removedSomething) {
                chunk.setSimulationBlocks(newSimulationBlocks);
                if (UnloadedActivity.config.debugLogs)
                    UnloadedActivity.LOGGER.info("Removed "+(currentSimulationBlocks.size()-newSimulationBlocks.size())+" positions.");
            }

        } else {
            if (UnloadedActivity.config.debugLogs)
                UnloadedActivity.LOGGER.info("Looping through entire chunk.");
            for (int z=0; z<16;z++)
                for (int x=0; x<16;x++)
                    for (int y=minY; y<maxY;y++) {
                        BlockPos chunkBlockPos = new BlockPos(x,y,z);
                        ChunkPos chunkPos = chunk.getPos();
                        BlockPos worldBlockPos = chunkBlockPos.add(new BlockPos(chunkPos.x*16,0,chunkPos.z*16));
                        BlockState state = chunk.getBlockState(chunkBlockPos);
                        Block block = state.getBlock();
                        if (block.implementsSimulateRandTicks()) {
                            blockPosArray.add(worldBlockPos);
                            if (UnloadedActivity.config.rememberBlockPositions)
                                newSimulationBlocks.add(worldBlockPos.asLong());
                        }
            }
            if (UnloadedActivity.config.rememberBlockPositions) {
                chunk.setSimulationBlocks(newSimulationBlocks);
                chunk.setSimulationVersion(UnloadedActivity.chunkSimVer);
                chunk.setNeedsSaving(true);
            }
        }

        if (UnloadedActivity.config.randomizeBlockUpdates)
            Collections.shuffle(blockPosArray);

        for (BlockPos blockPos : blockPosArray)
            simulateBlockRandomTicks(blockPos, world, timeDifference, randomTickSpeed);
    }

    public static void simulateBlockRandomTicks(BlockPos pos, ServerWorld world, long timeDifference, int randomTickSpeed) {
        if (!UnloadedActivity.config.enableRandomTicks)
            return;

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (!block.canSimulateRandTicks(state, world, pos))
            return;

        block.simulateRandTicks(state, world, pos, world.random, timeDifference, randomTickSpeed);
    }

    public static <T extends BlockEntity> void simulateBlockEntity(World world, BlockPos pos, BlockState blockState, T blockEntity, long timeDifference) {
        if (!UnloadedActivity.config.enableBlockEntities) return;

        long now = 0;
        if (UnloadedActivity.config.debugLogs) now = Instant.now().toEpochMilli();
        if (!blockEntity.canSimulate()) return;
        blockEntity.simulateTime(world, pos, blockState, blockEntity, timeDifference);
        if (UnloadedActivity.config.debugLogs) UnloadedActivity.LOGGER.info((Instant.now().toEpochMilli() - now) + "ms to simulate ticks on blockEntity after " + timeDifference + " ticks.");
    }

    public static void simulateEntity(Entity entity, long timeDifference) {

        if (!UnloadedActivity.config.enableEntities) return;
        if (!entity.canSimulate()) return;

        long now = 0;
        if (UnloadedActivity.config.debugLogs) now = Instant.now().toEpochMilli();

        entity.simulateTime(entity, timeDifference);
        if (UnloadedActivity.config.debugLogs) UnloadedActivity.LOGGER.info((Instant.now().toEpochMilli() - now) + "ms to simulate ticks on entity after " + timeDifference + " ticks.");
    }
}
