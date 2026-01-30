package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.OccurrencesAndDuration;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import lol.zanspace.unloadedactivity.datapack.SimulationData;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(BuddingAmethystBlock.class)
public abstract class BuddingAmethystMixin extends AmethystBlock {

    public BuddingAmethystMixin(Properties properties) {
        super(properties);
    }

    /*

    @Shadow @Final
    public static int GROWTH_CHANCE;

    @Shadow @Final private static Direction[] DIRECTIONS;

    @Shadow @Final public static boolean canClusterGrowAtState(BlockState state) {return true;}
    @Override
    public boolean implementsSimulateRandTicks() {return true;}

    @Override
    public double getOdds(ServerLevel level, BlockState state, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        return 1.0/(GROWTH_CHANCE*DIRECTIONS.length);
    }
    @Override public boolean canSimulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        if (!UnloadedActivity.config.growAmethyst) return false;
        return true;
    }
    @Override
    public Triple<BlockState, OccurrencesAndDuration, BlockPos> simulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName, RandomSource random, long timePassed, int randomTickSpeed, boolean calculateDuration) {

        double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
        double totalOdds = getOdds(level, state, pos, simulateProperty, propertyName) * randomPickChance;

        for(int i=0;i<DIRECTIONS.length;i++) {

            Direction direction = DIRECTIONS[i];
            BlockPos blockPos = pos.relative(direction);
            BlockState blockState = level.getBlockState(blockPos);

            int currentAge;

            if (canClusterGrowAtState(blockState)) {
                currentAge = 0;
            } else if (blockState.is(Blocks.SMALL_AMETHYST_BUD) && blockState.getValue(AmethystClusterBlock.FACING) == direction) {
                currentAge = 1;
            } else if (blockState.is(Blocks.MEDIUM_AMETHYST_BUD) && blockState.getValue(AmethystClusterBlock.FACING) == direction) {
                currentAge = 2;
            } else if (blockState.is(Blocks.LARGE_AMETHYST_BUD) && blockState.getValue(AmethystClusterBlock.FACING) == direction) {
                currentAge = 3;
            } else {
                continue;
            }

            int ageDifference = 4 - currentAge;

            currentAge += Utils.getOccurrences(timePassed, totalOdds, ageDifference, random);

            Block block;

            switch (currentAge) {
                case 1:
                    block = Blocks.SMALL_AMETHYST_BUD;
                    break;
                case 2:
                    block = Blocks.MEDIUM_AMETHYST_BUD;
                    break;
                case 3:
                    block = Blocks.LARGE_AMETHYST_BUD;
                    break;
                case 4:
                    block = Blocks.AMETHYST_CLUSTER;
                    break;
                default:
                    continue;
            }
            BlockState blockState2 = block.defaultBlockState()
                .setValue(AmethystClusterBlock.FACING, direction)
                .setValue(AmethystClusterBlock.WATERLOGGED, blockState.getFluidState().getType() == Fluids.WATER);

            level.setBlockAndUpdate(blockPos, blockState2);
        }
        return state;
    }

     */
}
