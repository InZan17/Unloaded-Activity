package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;


import lol.zanspace.unloadedactivity.ExpectPlatform;
import lol.zanspace.unloadedactivity.OccurrencesAndDuration;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import lol.zanspace.unloadedactivity.datapack.SimulationData;
import lol.zanspace.unloadedactivity.mixin.CropBlockInvoker;

#if MC_VER >= MC_1_20_4
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
#endif
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.Optional;

import static java.lang.Math.min;

@Mixin(StemBlock.class)
public abstract class StemMixin extends #if MC_VER >= MC_1_21_5 VegetationBlock #else BushBlock #endif {

    protected StemMixin(Properties properties) {
        super(properties);
    }

    /*

    @Final
    @Shadow
    public static IntegerProperty AGE;

    #if MC_VER >= MC_1_20_4
    @Shadow @Final
    private ResourceKey<Block> fruit;
    @Shadow @Final
    private ResourceKey<Block> attachedStem;
    #else
    @Final
    @Shadow
    private StemGrownBlock fruit;
    #endif

    @Override public int getCurrentAgeUA(BlockState state) {
        return state.getValue(AGE);
    }

    @Override public int getMaxAgeUA() {
        return 7;
    }

    @Override
    public double getOdds(ServerLevel level, BlockState state, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        #if MC_VER >= MC_1_21_1
        float f = ExpectPlatform.getGrowthSpeed(level.getBlockState(pos), level, pos);
        #else
        float f = CropBlockInvoker.invokeGetGrowthSpeed(this, level, pos);
        #endif
        return 1.0/(double)((int)(25.0F / f) + 1);
    }
    @Override
    public boolean implementsSimulateRandTicks() {return true;}
    @Override public boolean canSimulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        if (!UnloadedActivity.config.growStems) return false;
        if (level.getRawBrightness(pos, 0) < 9) return false;
        if (NumOfValidPositions(pos, level) == 0 && this.getCurrentAgeUA(state) == this.getMaxAgeUA()) return false;
        return true;
    }

    @Override
    public Triple<BlockState, OccurrencesAndDuration, BlockPos> simulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName, RandomSource random, long timePassed, int randomTickSpeed, boolean calculateDuration) {

        int currentAge = this.getCurrentAgeUA(state);
        int maxAge = this.getMaxAgeUA();
        int ageDifference = maxAge - currentAge;

        int validPositions = NumOfValidPositions(pos, level);

        double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);

        double randomGrowChance = getOdds(level, state, pos, simulateProperty, propertyName); //chance to grow for every pick

        double totalOdds = randomPickChance * randomGrowChance;

        OccurrencesAndDuration oal = Utils.getOccurrencesAndDurationTicks(timePassed, totalOdds, ageDifference + min(1, validPositions), random);

        int growthAmount = oal.occurrences;
        long leftover = oal.leftover;

        if (growthAmount != 0) {
            state = state.setValue(AGE, min(currentAge + growthAmount, maxAge));
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }

        if (currentAge + growthAmount > maxAge && validPositions != 0) { // it surpasses the max age of crop and has space, try to grow fruit

            double chanceForFreeSpace = 0.25 * validPositions;
            int growsFruit = Utils.getOccurrences(leftover, chanceForFreeSpace, 1, random);

            if (growsFruit == 0)
                return state;

            List<Direction> directions = Direction.Plane.HORIZONTAL.shuffledCopy(random);

            for (int i = 0; i < directions.size(); i++) {

                Direction direction = directions.get(i);

                if (!isValidPosition(direction, pos, level)) continue;

                BlockPos blockPos = pos.relative(direction);
                #if MC_VER >= MC_1_20_4

                #if MC_VER >= MC_1_21_3
                Registry<Block> blockRegistry = level.registryAccess().lookupOrThrow(Registries.BLOCK);
                #else
                Registry<Block> blockRegistry = level.registryAccess().registryOrThrow(Registries.BLOCK);
                #endif
                Optional<Block> fruitBlock = blockRegistry.getOptional(this.fruit);
                Optional<Block> attachedStemBlock = blockRegistry.getOptional(this.attachedStem);

                if (fruitBlock.isPresent() && attachedStemBlock.isPresent()) {
                    level.setBlockAndUpdate(blockPos, fruitBlock.get().defaultBlockState());
                    level.setBlockAndUpdate(pos, attachedStemBlock.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, direction));
                }
                #else
                level.setBlockAndUpdate(blockPos, this.fruit.defaultBlockState());

                returnLeftoverTicks.get()

                state = this.fruit.getAttachedStem().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, direction);
                level.setBlockAndUpdate(pos, state);
                #endif
                break;
            }
        }
        return state;
    }

    @Unique
    private int NumOfValidPositions(BlockPos pos, ServerLevel level) {
        return (isValidPosition(Direction.NORTH, pos, level) ? 1 : 0)
             + (isValidPosition(Direction.EAST, pos, level) ? 1 : 0)
             + (isValidPosition(Direction.SOUTH, pos, level) ? 1 : 0)
             + (isValidPosition(Direction.WEST, pos, level) ? 1 : 0);
    }

    @Unique
    private boolean isValidPosition(Direction direction, BlockPos pos, ServerLevel level) {
        BlockPos blockPos = pos.relative(direction);
        BlockState blockState = level.getBlockState(blockPos.below());
        return level.getBlockState(blockPos).isAir() && (blockState.is(Blocks.FARMLAND) || blockState.is(BlockTags.DIRT));
    }

     */
}
