package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.OccurrencesAndDuration;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import lol.zanspace.unloadedactivity.datapack.SimulationData;
import lol.zanspace.unloadedactivity.mixin.AbstractCauldronBlockInvoker;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

#if MC_VER >= MC_1_21_11
import net.minecraft.world.attribute.EnvironmentAttributes;
#endif

import java.util.Optional;

import static java.lang.Math.max;
import static java.lang.Math.min;

@Mixin(PointedDripstoneBlock.class)
public abstract class PointedDripstoneMixin extends Block {

    public PointedDripstoneMixin(Properties properties) {
        super(properties);
    }

    /*

    @Shadow private static boolean canGrow(BlockState dripstoneBlockState, BlockState waterState) {
        return true;
    }
    @Shadow private static boolean canTipGrow(BlockState state, ServerLevel level, BlockPos pos) {
        return true;
    }
    @Shadow private static void grow(ServerLevel level, BlockPos pos, Direction direction) {}
    @Shadow private static boolean isStalactiteStartPos(BlockState state, LevelReader level, BlockPos pos) {
        return true;
    }
    @Shadow
    private static BlockPos findTip(BlockState state, LevelAccessor level, BlockPos pos, int range, boolean allowMerged) {
        return null;
    }

    @Shadow private static boolean isUnmergedTipWithDirection(BlockState state, Direction direction) {
        return true;
    }

    @Shadow private static boolean isValidPointedDripstonePlacement(LevelReader level, BlockPos pos, Direction direction) {
        return true;
    }
    @Shadow private static boolean canDripThrough(BlockGetter blockGetter, BlockPos pos, BlockState state) {
        return true;
    }

    @Shadow private static BlockPos findFillableCauldronBelowStalactiteTip(Level world, BlockPos pos, Fluid fluid) {
        return null;
    }
    @Shadow private static void growStalagmiteBelow(ServerLevel level, BlockPos pos) {}
    @Shadow @Final
    private static int MAX_GROWTH_LENGTH;
    @Shadow @Final
    private static int MAX_STALAGMITE_SEARCH_RANGE_WHEN_GROWING;
    @Shadow @Final
    private static int MAX_SEARCH_LENGTH_BETWEEN_STALACTITE_TIP_AND_CAULDRON;

    @Shadow @Final
    private static float WATER_TRANSFER_PROBABILITY_PER_RANDOM_TICK;
    @Shadow @Final
    private static float LAVA_TRANSFER_PROBABILITY_PER_RANDOM_TICK;

    @Override
    public double getOdds(ServerLevel level, BlockState state, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        return 0.01137777;
    }

    @Override
    public boolean implementsSimulateRandTicks() {return true;}
    @Override public boolean canSimulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName) {
        if (!UnloadedActivity.config.growDripstone && !UnloadedActivity.config.dripstoneFillCauldrons && !UnloadedActivity.config.dripstoneTurnMudToClay) return false;
        if (!isStalactiteStartPos(state, level, pos)) return false;
        return true;
    }

    @Unique
    private static int getStalagmiteGrowthDistance(ServerLevel level, BlockPos tipPos) {
        BlockPos.MutableBlockPos mutable = tipPos.mutable();

        for(int i = 0; i < MAX_STALAGMITE_SEARCH_RANGE_WHEN_GROWING+MAX_GROWTH_LENGTH; ++i) {
            mutable.move(Direction.DOWN);
            BlockState blockState = level.getBlockState(mutable);
            if (!blockState.getFluidState().isEmpty()) {
                return -1;
            }

            if (isUnmergedTipWithDirection(blockState, Direction.UP) && canTipGrow(blockState, level, mutable)) {
                return i+1;
            }

            if (isValidPointedDripstonePlacement(level, mutable, Direction.UP) && !level.isWaterAt(mutable.below())) {
                return i+1;
            }

            if (!canDripThrough(level, mutable, blockState)) {
                return -1;
            }
        }
        return -1;
    }

    @Unique
    private static BlockPos getExtendedCauldronPos(Level world, BlockPos pos, Fluid fluid) {
        BlockPos cauldronPos = findFillableCauldronBelowStalactiteTip(world, pos, fluid);
        if (cauldronPos == null) {
            cauldronPos = findFillableCauldronBelowStalactiteTip(world, pos.below(MAX_SEARCH_LENGTH_BETWEEN_STALACTITE_TIP_AND_CAULDRON-1), fluid);
        }
        return cauldronPos;
    }

    @Unique
    private float getCauldronDripOdds(Fluid fluid) {
        if (fluid == Fluids.WATER) {
            return WATER_TRANSFER_PROBABILITY_PER_RANDOM_TICK;
        } else if (fluid == Fluids.LAVA) {
            return LAVA_TRANSFER_PROBABILITY_PER_RANDOM_TICK;
        }
        return 0.0f;
    }

    @Override
    public Triple<BlockState, OccurrencesAndDuration, BlockPos> simulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulationData.SimulateProperty simulateProperty, String propertyName, RandomSource random, long timePassed, int randomTickSpeed, boolean calculateDuration) {

        BlockPos tipPos = findTip(state, level, pos, 12, false);
        if (tipPos == null)
            return state;

        BlockState tip = level.getBlockState(tipPos);

        BlockState dripstoneBlockState = level.getBlockState(pos.above(1));
        BlockState liquidState = level.getBlockState(pos.above(2));

        int currentLength = pos.getY()-tipPos.getY();
        int lengthDifference = MAX_GROWTH_LENGTH-currentLength;

        double totalGrowOdds = this.getOdds(level, state, pos, simulateProperty, propertyName) * Utils.getRandomPickOdds(randomTickSpeed)*0.5; //somewhere there's a 50/50 chance of growing upper or under.

        int stalagmiteGroundDistance = getStalagmiteGrowthDistance(level, tipPos);

        Fluid dripstoneFluid = liquidState.getFluidState().getType();

        BlockPos cauldronPos = getExtendedCauldronPos(level, tipPos, dripstoneFluid);

        int cauldronGroundDistance = -1;

        if (cauldronPos != null)
            cauldronGroundDistance = tipPos.getY() - cauldronPos.getY();

        int totalUpperDripGrowth = 0;
        int totalLowerDripGrowth = 0;

        int successesUntilReachGround = max(stalagmiteGroundDistance-MAX_STALAGMITE_SEARCH_RANGE_WHEN_GROWING, 0);
        int successesUntilReachCauldron = max(cauldronGroundDistance-MAX_SEARCH_LENGTH_BETWEEN_STALACTITE_TIP_AND_CAULDRON, 0);


        if (UnloadedActivity.config.growDripstone) {
            if (currentLength < MAX_GROWTH_LENGTH) {
                if (canGrow(dripstoneBlockState, liquidState)) {
                    if (PointedDripstoneBlock.canDrip(tip) && canTipGrow(tip, level, tipPos)) {

                        totalUpperDripGrowth = Utils.getOccurrences(timePassed, totalGrowOdds, lengthDifference, random);

                        if (stalagmiteGroundDistance != -1) {
                            if (totalUpperDripGrowth >= successesUntilReachGround) {
                                long leftover = timePassed - Utils.sampleNegativeBinomialWithMax(timePassed, successesUntilReachGround, totalGrowOdds, random);
                                int maxGroundGrowth = min(stalagmiteGroundDistance, MAX_STALAGMITE_SEARCH_RANGE_WHEN_GROWING);
                                totalLowerDripGrowth = Utils.getOccurrences(leftover, totalGrowOdds, maxGroundGrowth, random);
                            }
                        }
                    }
                }
            }
        }

        if (UnloadedActivity.config.dripstoneTurnMudToClay) {
            boolean ultraWarm = #if MC_VER >= MC_1_21_11
                level.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, pos)
            #else
                level.dimensionType().ultraWarm()
            #endif;

            if (liquidState.is(Blocks.MUD) && !ultraWarm) {
                double totalDripOdds = WATER_TRANSFER_PROBABILITY_PER_RANDOM_TICK * Utils.getRandomPickOdds(randomTickSpeed);
                int dripOccurrences = Utils.getOccurrences(timePassed, totalDripOdds, 1, random);
                if (dripOccurrences != 0) {

                    BlockState clay = Blocks.CLAY.defaultBlockState();
                    level.setBlockAndUpdate(pos.above(2), clay);
                    Block.pushEntitiesUp(liquidState, clay, level, pos.above(2));
                }
            }
        }

        if (UnloadedActivity.config.dripstoneFillCauldrons) {
            if (cauldronPos != null && totalUpperDripGrowth >= successesUntilReachCauldron) {
                BlockState cauldronState = level.getBlockState(cauldronPos);
                if (cauldronState.getBlock() instanceof AbstractCauldronBlock cauldronBlock) {

                    AbstractCauldronBlockInvoker abstractCauldronBlockInvoker = (AbstractCauldronBlockInvoker)cauldronBlock;

                    if (!cauldronBlock.isFull(cauldronState) && abstractCauldronBlockInvoker.canReceiveStalactiteDrip(dripstoneFluid)) {
                        double totalDripOdds = getCauldronDripOdds(dripstoneFluid) * Utils.getRandomPickOdds(randomTickSpeed);
                        long leftover = timePassed - Utils.sampleNegativeBinomialWithMax(timePassed, successesUntilReachCauldron, totalGrowOdds, random);
                        int dripOccurrences = Utils.getOccurrences(leftover, totalDripOdds, LayeredCauldronBlock.MAX_FILL_LEVEL, random);
                        while (dripOccurrences > 0) {
                            --dripOccurrences;
                            abstractCauldronBlockInvoker.receiveStalactiteDrip(cauldronState, level, cauldronPos, dripstoneFluid);

                            //The block has changed and so the state and invoker needs updating.
                            cauldronState = level.getBlockState(cauldronPos);
                            abstractCauldronBlockInvoker = (AbstractCauldronBlockInvoker)cauldronState.getBlock();
                        }
                    }
                }

            }
        }

        while (successesUntilReachGround > 0 && totalUpperDripGrowth > 0) {
            --successesUntilReachGround;
            --totalUpperDripGrowth;
            grow(level, tipPos, Direction.DOWN);
            //recalculate tip so if tryGrow fails, we wont grow past any blocking blocks. Simply doing tipPos.down() doesnt account for fail.
            tipPos = findTip(level.getBlockState(pos), level, pos, 12, false);
            if (tipPos == null) return null;
        }

        while (totalUpperDripGrowth+totalLowerDripGrowth > 0) {
            if (totalUpperDripGrowth == 0) {

                if (pos.getY()-tipPos.getY() >= MAX_GROWTH_LENGTH)
                    return null;

                --totalLowerDripGrowth;
                growStalagmiteBelow(level, tipPos);
            } else if (totalLowerDripGrowth == 0) {
                --totalUpperDripGrowth;
                grow(level, tipPos, Direction.DOWN);
                //recalculate tip so if tryGrow fails, we wont grow past any blocking blocks. Simply doing tipPos.down() doesnt account for fail.
                tipPos = findTip(level.getBlockState(pos), level, pos, 12, false);
                if (tipPos == null) return null;

            } else if (random.nextBoolean()) {
                --totalLowerDripGrowth;
                growStalagmiteBelow(level, tipPos);
            } else {
                --totalUpperDripGrowth;
                grow(level, tipPos, Direction.DOWN);
                //recalculate tip so if tryGrow fails, we wont grow past any blocking blocks. Simply doing tipPos.down() doesnt account for fail.
                tipPos = findTip(level.getBlockState(pos), level, pos, 12, false);
                if (tipPos == null) return null;
            }
        }

        return null;
    }

     */

}
