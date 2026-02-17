package lol.zanspace.unloadedactivity;

import lol.zanspace.unloadedactivity.datapack.SimulateProperty;
import lol.zanspace.unloadedactivity.datapack.SimulationData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import oshi.util.tuples.Pair;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class TimeMachine {
    public static long simulateChunk(long timeDifference, ServerLevel level, LevelChunk chunk, int randomTickSpeed) {
        if (!UnloadedActivity.config.enableRandomTicks || !UnloadedActivity.config.enablePrecipitationTicks) return 0;
        
        long now = 0;
        if (UnloadedActivity.config.debugLogs) now = Instant.now().toEpochMilli();

        TimeMachine.simulatePrecipitationTicks(timeDifference, level, chunk);
        TimeMachine.simulateRandomTicks(timeDifference, level, chunk, randomTickSpeed);

        long msTime = 0;

        if (UnloadedActivity.config.debugLogs) {
            msTime = Instant.now().toEpochMilli() - now;
            UnloadedActivity.LOGGER.info(msTime + "ms to simulate random ticks on chunk after " + timeDifference + " ticks.");
        };
        return msTime;
    }

    public static void simulatePrecipitationTicks(long timeDifference, ServerLevel level, LevelChunk chunk) {

        if (!UnloadedActivity.config.enablePrecipitationTicks)
            return;

        double precipitationPickChance = 1.0/4096.0; //1/(16*256). 16 for the chance of the chunk doing the tick and 256 for the chance of a block to be picked.

        WorldWeatherData weatherData = level.getWeatherData();

        long timeInWeather = weatherData.getTimeInWeather(timeDifference, level.getDayTime());

        for (int z=0; z<16;z++)
            for (int x=0; x<16;x++) {
                ChunkPos chunkPos = chunk.getPos();
                BlockPos airPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(chunkPos.x*16+x,0,chunkPos.z*16+z));
                BlockPos groundPos = airPos.below();
                BlockState airPosState = chunk.getBlockState(airPos);
                BlockState groundPosState = chunk.getBlockState(groundPos);
                Block airPosBlock = airPosState.getBlock();
                Block groundPosBlock = groundPosState.getBlock();
                Biome biome = level.getBiome(airPos).value();

                if (airPosBlock.implementsSimulatePrecTicks())
                    simulateBlockPrecipitationTick(
                        airPos,
                        level,
                        timeDifference,
                        precipitationPickChance,
                        timeInWeather,
                        #if MC_VER >= MC_1_19_4
                            biome.getPrecipitationAt(airPos
                        #else
                            biome.getPrecipitation(
                        #endif
                        #if MC_VER >= MC_1_21_3 , level.getSeaLevel()#endif
                        )
                    );

                if (groundPosBlock.implementsSimulatePrecTicks())
                    simulateBlockPrecipitationTick(
                        groundPos,
                        level,
                        timeDifference,
                        precipitationPickChance,
                        timeInWeather,
                        #if MC_VER >= MC_1_19_4
                            biome.getPrecipitationAt(groundPos
                        #else
                            biome.getPrecipitation(
                        #endif
                        #if MC_VER >= MC_1_21_3, level.getSeaLevel() #endif
                        )
                    );
        }
    }

    public static void simulateBlockPrecipitationTick(BlockPos pos, ServerLevel level, long timeDifference, double precipitationPickChance, long timeInWeather, Biome.Precipitation precipitation) {
        if (!UnloadedActivity.config.enablePrecipitationTicks)
            return;

        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (!block.canSimulatePrecTicks(state, level, pos, timeInWeather, precipitation))
            return;

        block.simulatePrecTicks(state, level, pos, timeInWeather, timeDifference, precipitation, precipitationPickChance);
    }

    public static void simulateRandomTicks(long timeDifference, ServerLevel level, LevelChunk chunk, int randomTickSpeed) {

        if (!UnloadedActivity.config.enableRandomTicks)
            return;

        #if MC_VER >= MC_1_21_3
        int minY = level.getMinY();
        int maxY = level.getMaxY();
        #else
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();
        #endif

        ArrayList<BlockPos> blockPosArray = new ArrayList<>();

        ArrayList<Long> newSimulationBlocks = new ArrayList<>();

        if (UnloadedActivity.config.rememberBlockPositions && chunk.getSimulationVersion() == UnloadedActivity.chunkSimVer) {

            ArrayList<Long> currentSimulationBlocks = chunk.getSimulationBlocks();

            if (UnloadedActivity.config.debugLogs)
                UnloadedActivity.LOGGER.info("Looping through "+currentSimulationBlocks.size()+" known positions.");

            boolean removedSomething = false;

            for (long longPos : currentSimulationBlocks) {
                BlockPos pos = BlockPos.of(longPos);
                BlockState state = level.getBlockState(pos);
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
                        BlockPos worldBlockPos = chunkBlockPos.offset(chunkPos.x*16,0,chunkPos.z*16);
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
                #if MC_VER >= MC_1_21_3
                chunk.markUnsaved();
                #else
                chunk.setUnsaved(true);
                #endif
            }
        }

        if (UnloadedActivity.config.randomizeBlockUpdates)
            Collections.shuffle(blockPosArray);

        for (BlockPos blockPos : blockPosArray)
            simulateBlockRandomTicks(blockPos, level, timeDifference, randomTickSpeed);
    }

    public static void simulateBlockRandomTicks(BlockPos pos, ServerLevel level, long timeLeft, int randomTickSpeed) {
        if (!UnloadedActivity.config.enableRandomTicks)
            return;

        BlockState state = level.getBlockState(pos);

        boolean blockHasChanged = true;

        while (blockHasChanged) {
            blockHasChanged = false;

            Block block = state.getBlock();

            Map<String, SimulateProperty> pendingProperties = new HashMap<>(block.getSimulationData().propertyMap);

            Map<String, Long> finishedProperties = new HashMap<>();

            Set<String> propertiesWithDependents = new HashSet<>();

            var pendingPropertiesIterator = pendingProperties.entrySet().iterator();
            while (pendingPropertiesIterator.hasNext()) {
                var entry = pendingPropertiesIterator.next();

                String propertyName = entry.getKey();
                var simulateProperty = entry.getValue();

                if (block.isRandTicksFinished(state, level, pos, simulateProperty)) {
                    finishedProperties.put(propertyName, 0L);
                    pendingPropertiesIterator.remove();
                }

                propertiesWithDependents.addAll(simulateProperty.dependencies);
            }

            boolean continueCheck = true;

            while (continueCheck) {
                continueCheck = false;

                var iterator = pendingProperties.entrySet().iterator();

                while (iterator.hasNext()) {
                    var entry = iterator.next();

                    boolean validDependencies = true;
                    long maxDuration = 0;

                    var simulateProperty = entry.getValue();
                    var propertyName = entry.getKey();
                    for (String dependency : simulateProperty.dependencies) {
                        Long dependencyDuration = finishedProperties.get(dependency);

                        if (dependencyDuration == null) {
                            validDependencies = false;
                            break;
                        }

                        maxDuration = Math.max(maxDuration, dependencyDuration);
                    }

                    if (!validDependencies) {
                        continue;
                    }

                    iterator.remove();

                    if (!block.canSimulateRandTicks(state, level, pos, simulateProperty))
                        continue;

                    long simulateTime = timeLeft - maxDuration;

                    assert (simulateTime >= 0);

                    if (simulateTime == 0) {
                        continue;
                    }

                    if (UnloadedActivity.config.debugLogs)
                        UnloadedActivity.LOGGER.info("Simulating property " + propertyName + " on block " + block);

                    var result = block.simulateRandTicks(state, level, pos, simulateProperty, level.random, simulateTime, randomTickSpeed, propertiesWithDependents.contains(propertyName));
                    if (result == null) {
                        continueCheck = false;
                        break;
                    }

                    state = result.getLeft();
                    pos = result.getRight();

                    long duration = result.getMiddle().duration();

                    assert (duration <= simulateTime);

                    long simulationDuration = result.getMiddle().duration() + maxDuration;

                    assert (simulationDuration <= timeLeft);

                    if (state.getBlock() != block) {
                        continueCheck = false;
                        timeLeft -= simulationDuration;
                        if (timeLeft > 0)
                            blockHasChanged = true;
                        break;
                    }

                    if (block.isRandTicksFinished(state, level, pos, simulateProperty)) {
                        continueCheck = true;
                        finishedProperties.put(propertyName, simulationDuration);
                    }
                }
            }
        }
    }

    public static <T extends BlockEntity> void simulateBlockEntity(ServerLevel level, BlockPos pos, BlockState blockState, T blockEntity, long timeDifference) {
        if (!UnloadedActivity.config.enableBlockEntities) return;

        long now = 0;
        if (UnloadedActivity.config.debugLogs) now = Instant.now().toEpochMilli();
        if (!blockEntity.unloaded_activity$canSimulate()) return;
        blockEntity.unloaded_activity$simulateTime(level, pos, blockState, timeDifference);
        if (UnloadedActivity.config.debugLogs) UnloadedActivity.LOGGER.info((Instant.now().toEpochMilli() - now) + "ms to simulate ticks on blockEntity after " + timeDifference + " ticks.");
    }

    public static void simulateEntity(Entity entity, long timeDifference) {

        if (!UnloadedActivity.config.enableEntities) return;
        if (!entity.canSimulate()) return;

        long now = 0;
        if (UnloadedActivity.config.debugLogs) now = Instant.now().toEpochMilli();

        entity.simulateTime(timeDifference);
        if (UnloadedActivity.config.debugLogs) UnloadedActivity.LOGGER.info((Instant.now().toEpochMilli() - now) + "ms to simulate ticks on entity after " + timeDifference + " ticks.");
    }
}
