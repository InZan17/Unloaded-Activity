package com.github.inzan123;

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

import static com.github.inzan123.MyComponents.*;

public class TimeMachine {
    public static long simulateChunk(long timeDifference, ServerWorld world, WorldChunk chunk, int randomTickSpeed) {
        if (!UnloadedActivity.instance.config.enableRandomTicks || !UnloadedActivity.instance.config.enablePrecipitationTicks) return 0;
        
        long now = 0;
        if (UnloadedActivity.instance.config.debugLogs) now = Instant.now().toEpochMilli();

        TimeMachine.simulatePrecipitationTicks(timeDifference, world, chunk);
        TimeMachine.simulateRandomTicks(timeDifference, world, chunk, randomTickSpeed);

        long msTime = 0;

        if (UnloadedActivity.instance.config.debugLogs) {
            msTime = Instant.now().toEpochMilli() - now;
            UnloadedActivity.LOGGER.info(msTime + "ms to simulate random ticks on chunk after " + timeDifference + " ticks.");
        };
        return msTime;
    }

    public static void simulatePrecipitationTicks(long timeDifference, ServerWorld world, WorldChunk chunk) {

        if (!UnloadedActivity.instance.config.enablePrecipitationTicks)
            return;

        double precipitationPickChance = 1.0/4096.0; //1/(16*256). 16 for the chance of the chunk doing the tick and 256 for the chance of a block to be picked.

        WeatherInfoInterface weatherInfo = world.getComponent(WORLDWEATHERINFO);

        long timeInWeather = weatherInfo.getTimeInWeather(timeDifference,world.getTimeOfDay());

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
                    simulateBlockPrecipitationTick(airPos, world, timeDifference, precipitationPickChance, timeInWeather, biome.getPrecipitation(airPos));

                if (groundPosBlock.implementsSimulatePrecTicks())
                    simulateBlockPrecipitationTick(groundPos, world, timeDifference, precipitationPickChance, timeInWeather, biome.getPrecipitation(groundPos));
        }
    }

    public static void simulateBlockPrecipitationTick(BlockPos pos, ServerWorld world, long timeDifference, double precipitationPickChance, long timeInWeather, Biome.Precipitation precipitation) {
        if (!UnloadedActivity.instance.config.enablePrecipitationTicks)
            return;

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (!block.canSimulatePrecTicks(state, world, pos, timeInWeather, precipitation))
            return;

        block.simulatePrecTicks(state, world, pos, timeInWeather, timeDifference, precipitation, precipitationPickChance);
    }

    public static void simulateRandomTicks(long timeDifference, ServerWorld world, WorldChunk chunk, int randomTickSpeed) {

        if (!UnloadedActivity.instance.config.enableRandomTicks)
            return;

        int minY = world.getBottomY();
        int maxY = world.getTopY();


        ArrayList<BlockPos> blockPosArray = new ArrayList<>();

        LongComponent chunkSimVer = chunk.getComponent(CHUNKSIMVER);

        ArrayList<Long> newLongArray = new ArrayList<>();

        if (UnloadedActivity.instance.config.rememberBlockPositions && chunkSimVer.getValue() == UnloadedActivity.chunkSimVer) {


            LongArrayComponent chunkSimBlocks = chunk.getComponent(CHUNKSIMBLOCKS);

            ArrayList<Long> currentLongArray = chunkSimBlocks.getValue();

            if (UnloadedActivity.instance.config.debugLogs)
                UnloadedActivity.LOGGER.info("Looping through "+currentLongArray.size()+" known positions.");

            boolean removedSomething = false;

            for (long longPos : currentLongArray) {
                BlockPos pos = BlockPos.fromLong(longPos);
                BlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                if (block.implementsSimulateRandTicks()) {
                    newLongArray.add(longPos);
                    blockPosArray.add(pos);
                } else {
                    removedSomething = true;
                }
            }
            if (removedSomething) {
                chunkSimBlocks.setValue(newLongArray);
                if (UnloadedActivity.instance.config.debugLogs)
                    UnloadedActivity.LOGGER.info("Removed "+(currentLongArray.size()-newLongArray.size())+" positions.");
            }

        } else {
            if (UnloadedActivity.instance.config.debugLogs)
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
                            if (UnloadedActivity.instance.config.rememberBlockPositions)
                                newLongArray.add(worldBlockPos.asLong());
                        }
            }
            if (UnloadedActivity.instance.config.rememberBlockPositions) {
                LongArrayComponent chunkSimBlocks = chunk.getComponent(CHUNKSIMBLOCKS);
                chunkSimBlocks.setValue(newLongArray);
                chunkSimVer.setValue(UnloadedActivity.chunkSimVer);
                chunk.setNeedsSaving(true);
            }
        }

        if (UnloadedActivity.instance.config.randomizeBlockUpdates)
            Collections.shuffle(blockPosArray);

        for (BlockPos blockPos : blockPosArray)
            simulateBlockRandomTicks(blockPos, world, timeDifference, randomTickSpeed);
    }

    public static void simulateBlockRandomTicks(BlockPos pos, ServerWorld world, long timeDifference, int randomTickSpeed) {
        if (!UnloadedActivity.instance.config.enableRandomTicks)
            return;

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (!block.canSimulateRandTicks(state, world, pos))
            return;

        block.simulateRandTicks(state, world, pos, world.random, timeDifference, randomTickSpeed);
    }

    public static <T extends BlockEntity> void simulateBlockEntity(World world, BlockPos pos, BlockState blockState, T blockEntity, long timeDifference) {
        if (!UnloadedActivity.instance.config.enableBlockEntities) return;

        long now = 0;
        if (UnloadedActivity.instance.config.debugLogs) now = Instant.now().toEpochMilli();
        if (!blockEntity.canSimulate()) return;
        blockEntity.simulateTime(world, pos, blockState, blockEntity, timeDifference);
        if (UnloadedActivity.instance.config.debugLogs) UnloadedActivity.LOGGER.info((Instant.now().toEpochMilli() - now) + "ms to simulate ticks on blockEntity after " + timeDifference + " ticks.");
    }

    public static void simulateEntity(Entity entity, long timeDifference) {

        if (!UnloadedActivity.instance.config.enableEntities) return;
        if (!entity.canSimulate()) return;

        long now = 0;
        if (UnloadedActivity.instance.config.debugLogs) now = Instant.now().toEpochMilli();

        entity.simulateTime(entity, timeDifference);
        if (UnloadedActivity.instance.config.debugLogs) UnloadedActivity.LOGGER.info((Instant.now().toEpochMilli() - now) + "ms to simulate ticks on entity after " + timeDifference + " ticks.");
    }
}
