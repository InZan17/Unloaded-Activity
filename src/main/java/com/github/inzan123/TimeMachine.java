package com.github.inzan123;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;

public class TimeMachine {
    public static void simulateRandomTicks(long timeDifference, ServerWorld world, WorldChunk chunk, int randomTickSpeed) {
        if (!UnloadedActivity.instance.config.enableRandomTicks) return;
        
        long now = 0;
        if (UnloadedActivity.instance.config.debugLogs) now = Instant.now().toEpochMilli();

        int minY = world.getBottomY();
        int maxY = world.getTopY();


        ArrayList<BlockPos> blockPosArray = new ArrayList<>();

        for (int z=0; z<16;z++)
            for (int x=0; x<16;x++)
                for (int y=minY; y<maxY;y++) {
                    BlockPos position = new BlockPos(x,y,z);
                    ChunkPos chunkPos = chunk.getPos();
                    BlockPos notChunkBlockPos = position.add(new BlockPos(chunkPos.x*16,0,chunkPos.z*16));
                    BlockState state = chunk.getBlockState(position);
                    Block block = state.getBlock();
                    if (block.canSimulate(state, world, notChunkBlockPos)) blockPosArray.add(position);
        }

        if (UnloadedActivity.instance.config.randomizeBlockUpdates)
            Collections.shuffle(blockPosArray);

        for (BlockPos blockPos : blockPosArray)
            simulateBlockRandomTicks(blockPos, chunk, world, timeDifference, randomTickSpeed);

        if (UnloadedActivity.instance.config.debugLogs) UnloadedActivity.LOGGER.info((Instant.now().toEpochMilli() - now) + "ms to simulate random ticks on chunk after " + timeDifference + " ticks.");
    }

    public static void simulateBlockRandomTicks(BlockPos position, WorldChunk chunk, ServerWorld world, long timeDifference, int randomTickSpeed) {
        if (!UnloadedActivity.instance.config.enableRandomTicks)
            return;

        BlockState state = chunk.getBlockState(position);
        Block block = state.getBlock();

        ChunkPos chunkPos = chunk.getPos();
        BlockPos notChunkBlockPos = position.add(new BlockPos(chunkPos.x*16,0,chunkPos.z*16));

        if (!block.canSimulate(state, world, notChunkBlockPos))
            return;

        block.simulateTime(state, world, notChunkBlockPos, world.random, timeDifference, randomTickSpeed);
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
