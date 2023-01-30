package com.github.inzan123.mixin;

import com.github.inzan123.SimulateTimePassing;
import com.github.inzan123.UnloadedActivity;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static java.lang.Math.pow;

@Mixin(SweetBerryBushBlock.class)
public abstract class BerryBushSimulateTimeMixin extends PlantBlock implements SimulateTimePassing {
    public BerryBushSimulateTimeMixin(Settings settings) {
        super(settings);
    }

    @Shadow @Final public static IntProperty AGE;
    @Shadow @Final public static int MAX_AGE;

    @Override
    public double getGrowthOdds(ServerWorld world, BlockPos pos) {
        return 0.2;
    }
    @Override
    public void simulateTime(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        if (!UnloadedActivity.CONFIG.growSweetBerries()) return;

        int age = (Integer)state.get(AGE);
        if (age >= MAX_AGE || world.getBaseLightLevel(pos.up(), 0) < 9) return;

        double randomPickChance = 1.0-pow(1.0 - 1.0 / 4096.0, randomTickSpeed);

        int ageDifference = MAX_AGE - age;

        double totalOdds = getGrowthOdds(world, pos) * randomPickChance;

        int growthAmount = getOccurrences(timePassed, totalOdds, ageDifference, random);

        if (growthAmount == 0) return;

        BlockState blockState = (BlockState)state.with(AGE, age + growthAmount);
        world.setBlockState(pos, blockState, 2);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(blockState));

    }
}
