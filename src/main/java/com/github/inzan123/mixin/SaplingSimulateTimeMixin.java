package com.github.inzan123.mixin;

import com.github.inzan123.SimulateTimePassing;
import com.github.inzan123.UnloadedActivity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static java.lang.Math.*;

@Mixin(SaplingBlock.class)
public class SaplingSimulateTimeMixin extends PlantBlock implements SimulateTimePassing {

    public SaplingSimulateTimeMixin(Settings settings) {
        super(settings);
    }

    @Shadow @Final private SaplingGenerator generator;

    @Shadow
    public void generate(ServerWorld world, BlockPos pos, BlockState state, Random random) {
    }

    @Shadow @Final public static IntProperty STAGE;

    @Override
    public double getGrowthOdds(ServerWorld world, BlockPos pos) {
        return 0.14285714285; // 1/7 * canGrow()
    }

    @Override
    public void simulateTime(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        if (!UnloadedActivity.CONFIG.growSaplings()) return;

        if (world.getBaseLightLevel(pos, 0) < 9) return; //if this is false then there isnt enough block lights and sky

        if (world.getLightLevel(LightType.BLOCK, pos.up()) < 9) { // If there isnt enough block lights we will do a calculation on how many ticks the tree could have spent in sunlight.
            long stopGrowTime = 12973;
            long startGrowTime = 23029;
            long offset = 24000 - startGrowTime; // we use this offset to pretend crops start growing at 0 and not 23029

            long growTimeWindow = stopGrowTime+offset;

            long currentTime = floorMod(min(floorMod(world.getTimeOfDay()+offset, 24000)-offset, stopGrowTime), growTimeWindow);

            long previousTime = floorMod(min(floorMod(world.getTimeOfDay()-timePassed+offset, 24000)-offset, stopGrowTime), growTimeWindow);

            long usefulTicks = growTimeWindow*floorDiv(timePassed, 24000);

            long restOfDayTicks = floorMod(currentTime-previousTime, growTimeWindow);

            if (floorMod(timePassed, 24000) > growTimeWindow)
                if (restOfDayTicks == 0)
                    restOfDayTicks = growTimeWindow;

            timePassed = restOfDayTicks + usefulTicks;
        }


        double randomPickChance = 1.0 - pow(1.0 - 1.0 / 4096.0, randomTickSpeed);
        double randomGrowChance = getGrowthOdds(world, pos);
        double totalChance = randomPickChance * randomGrowChance;
        double invertedTotalChance = 1 - totalChance;

        double randomFloat = random.nextDouble();

        int currentAge = (Integer)state.get(STAGE);

        int ageDifference = 2-currentAge;

        double totalProbability = 0;

        for (int i = 0; i<ageDifference;i++) {

            double choose = getChoose(i,timePassed);

            double finalProbability = choose * pow(totalChance, i) * pow(invertedTotalChance, timePassed-i); //Probability of it growing "i" steps

            //UnloadedActivity.LOGGER.info("odds for growing " + i + " times: " + finalProbability);

            totalProbability += finalProbability;

            if (randomFloat < totalProbability) {
                if (i == 0) return;
                this.generate(world, pos, state, random);
                return;
            }
        }

        //UnloadedActivity.LOGGER.info("odds for growing to max age: " + (1 - totalProbability));

        this.generate(world, pos, state, random);
        if (ageDifference == 2) {
            this.generate(world, pos, world.getBlockState(pos), random);
        }

    }
}