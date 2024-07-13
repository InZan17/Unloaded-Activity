package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import lol.zanspace.unloadedactivity.mixin.AbstractCauldronBlockInvoker;
import net.minecraft.block.*;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static java.lang.Math.max;
import static java.lang.Math.min;

@Mixin(PointedDripstoneBlock.class)
public abstract class PointedDripstoneMixin extends Block implements LandingBlock, Waterloggable {
    public PointedDripstoneMixin(Settings settings) {
        super(settings);
    }

    @Shadow private static boolean canGrow(BlockState dripstoneBlockState, BlockState waterState) {
        return true;
    }
    @Shadow private static boolean canGrow(BlockState state, ServerWorld world, BlockPos pos) {
        return true;
    }
    @Shadow private static void tryGrow(ServerWorld world, BlockPos pos, Direction direction) {}
    @Shadow private static boolean isHeldByPointedDripstone(BlockState state, WorldView world, BlockPos pos) {
        return true;
    }
    @Shadow
    private static BlockPos getTipPos(BlockState state, WorldAccess world, BlockPos pos, int range, boolean allowMerged) {
        return null;
    }

    @Shadow private static boolean isTip(BlockState state, Direction direction) {
        return true;
    }

    @Shadow private static boolean canPlaceAtWithDirection(WorldView world, BlockPos pos, Direction direction) {
        return true;
    }
    @Shadow private static boolean canDripThrough(BlockView world, BlockPos pos, BlockState state) {
        return true;
    }

    @Shadow private static BlockPos getCauldronPos(World world, BlockPos pos, Fluid fluid) {
        return null;
    }
    @Shadow private static void tryGrowStalagmite(ServerWorld world, BlockPos pos) {}
    @Shadow @Final
    private static int MAX_STALACTITE_GROWTH;
    @Shadow @Final
    private static int STALACTITE_FLOOR_SEARCH_RANGE;

    @Shadow @Final
    private static float WATER_DRIP_CHANCE;
    @Shadow @Final
    private static float LAVA_DRIP_CHANCE;
    private final static int CAULDRON_FLOOR_SEARCH_RANGE = 11;

    @Override
    public double getOdds(ServerWorld world, BlockPos pos) {
        return 0.01137777;
    }

    @Override
    public boolean implementsSimulateRandTicks() {return true;}
    @Override public boolean canSimulateRandTicks(BlockState state, ServerWorld world, BlockPos pos) {
        if (!UnloadedActivity.config.growDripstone && !UnloadedActivity.config.dripstoneFillCauldrons && !UnloadedActivity.config.dripstoneTurnMudToClay) return false;
        if (!isHeldByPointedDripstone(state, world, pos)) return false;
        return true;
    }

    private static int getStalagmiteGrowthDistance(ServerWorld world, BlockPos tipPos) {
        BlockPos.Mutable mutable = tipPos.mutableCopy();

        for(int i = 0; i < STALACTITE_FLOOR_SEARCH_RANGE+MAX_STALACTITE_GROWTH; ++i) {
            mutable.move(Direction.DOWN);
            BlockState blockState = world.getBlockState(mutable);
            if (!blockState.getFluidState().isEmpty()) {
                return -1;
            }

            if (isTip(blockState, Direction.UP) && canGrow(blockState, world, mutable)) {
                return i+1;
            }

            if (canPlaceAtWithDirection(world, mutable, Direction.UP) && !world.isWater(mutable.down())) {
                return i+1;
            }

            if (!canDripThrough(world, mutable, blockState)) {
                return -1;
            }
        }
        return -1;
    }

    private static BlockPos getExtendedCauldronPos(World world, BlockPos pos, Fluid fluid) {
        BlockPos cauldronPos = getCauldronPos(world, pos, fluid);
        if (cauldronPos == null) {
            cauldronPos = getCauldronPos(world, pos.down(CAULDRON_FLOOR_SEARCH_RANGE-1), fluid);
        }
        return cauldronPos;
    }

    private float getCauldronDripOdds(Fluid fluid) {
        if (fluid == Fluids.WATER) {
            return WATER_DRIP_CHANCE;
        } else if (fluid == Fluids.LAVA) {
            return LAVA_DRIP_CHANCE;
        }
        return 0.0f;
    }

    @Override
    public void simulateRandTicks(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        BlockPos tipPos = getTipPos(state, world, pos, 12, false);
        if (tipPos == null)
            return;

        BlockState tip = world.getBlockState(tipPos);

        BlockState dripstoneBlockState = world.getBlockState(pos.up(1));
        BlockState liquidState = world.getBlockState(pos.up(2));

        int currentLength = pos.getY()-tipPos.getY();
        int lengthDifference = MAX_STALACTITE_GROWTH-currentLength;

        double totalGrowOdds = this.getOdds(world,pos) * Utils.getRandomPickOdds(randomTickSpeed)*0.5; //somewhere there's a 50/50 chance of growing upper or under.

        int stalagmiteGroundDistance = getStalagmiteGrowthDistance(world, tipPos);

        Fluid dripstoneFluid = liquidState.getFluidState().getFluid();

        BlockPos cauldronPos = getExtendedCauldronPos(world, tipPos, dripstoneFluid);

        int cauldronGroundDistance = -1;

        if (cauldronPos != null)
            cauldronGroundDistance = tipPos.getY() - cauldronPos.getY();

        int totalUpperDripGrowth = 0;
        int totalLowerDripGrowth = 0;

        int successesUntilReachGround = max(stalagmiteGroundDistance-STALACTITE_FLOOR_SEARCH_RANGE, 0);
        int successesUntilReachCauldron = max(cauldronGroundDistance-CAULDRON_FLOOR_SEARCH_RANGE, 0);


        if (UnloadedActivity.config.growDripstone) {
            if (currentLength < MAX_STALACTITE_GROWTH) {
                if (canGrow(dripstoneBlockState, liquidState)) {
                    if (PointedDripstoneBlock.canDrip(tip) && canGrow(tip, world, tipPos)) {

                        totalUpperDripGrowth = Utils.getOccurrences(timePassed, totalGrowOdds, lengthDifference, random);

                        if (stalagmiteGroundDistance != -1) {
                            if (totalUpperDripGrowth >= successesUntilReachGround) {
                                long leftover = timePassed - Utils.sampleNegativeBinomialWithMax(timePassed, successesUntilReachGround, totalGrowOdds, random);
                                int maxGroundGrowth = min(stalagmiteGroundDistance, STALACTITE_FLOOR_SEARCH_RANGE);
                                totalLowerDripGrowth = Utils.getOccurrences(leftover, totalGrowOdds, maxGroundGrowth, random);
                            }
                        }
                    }
                }
            }
        }

        if (UnloadedActivity.config.dripstoneTurnMudToClay) {
            if (liquidState.isOf(Blocks.MUD) && !world.getDimension().ultrawarm()) {
                double totalDripOdds = WATER_DRIP_CHANCE * Utils.getRandomPickOdds(randomTickSpeed);
                int dripOccurrences = Utils.getOccurrences(timePassed, totalDripOdds, 1, random);
                if (dripOccurrences != 0) {

                    BlockState clay = Blocks.CLAY.getDefaultState();
                    world.setBlockState(pos.up(2), clay);
                    Block.pushEntitiesUpBeforeBlockChange(liquidState, clay, world, pos.up(2));
                }
            }
        }

        if (UnloadedActivity.config.dripstoneFillCauldrons) {
            if (cauldronPos != null && totalUpperDripGrowth >= successesUntilReachCauldron) {
                BlockState cauldronState = world.getBlockState(cauldronPos);
                if (cauldronState.getBlock() instanceof AbstractCauldronBlock cauldronBlock) {

                    AbstractCauldronBlockInvoker abstractCauldronBlockInvoker = (AbstractCauldronBlockInvoker)cauldronBlock;

                    if (!cauldronBlock.isFull(cauldronState) && abstractCauldronBlockInvoker.canBeFilledByDripstone(dripstoneFluid)) {
                        double totalDripOdds = getCauldronDripOdds(dripstoneFluid) * Utils.getRandomPickOdds(randomTickSpeed);
                        long leftover = timePassed - Utils.sampleNegativeBinomialWithMax(timePassed, successesUntilReachCauldron, totalGrowOdds, random);
                        int dripOccurrences = Utils.getOccurrences(leftover, totalDripOdds, LeveledCauldronBlock.MAX_LEVEL, random);
                        while (dripOccurrences > 0) {
                            --dripOccurrences;
                            abstractCauldronBlockInvoker.fillFromDripstone(cauldronState, world, cauldronPos, dripstoneFluid);

                            //The block has changed and so the state and invoker needs updating.
                            cauldronState = world.getBlockState(cauldronPos);
                            abstractCauldronBlockInvoker = (AbstractCauldronBlockInvoker)cauldronState.getBlock();
                        }
                    }
                }

            }
        }

        while (successesUntilReachGround > 0 && totalUpperDripGrowth > 0) {
            --successesUntilReachGround;
            --totalUpperDripGrowth;
            tryGrow(world, tipPos, Direction.DOWN);
            tipPos = getTipPos(world.getBlockState(pos), world, pos, 12, false);
            if (tipPos == null) return; //recalculate tip so if tryGrow fails, we wont grow past any blocking blocks. Simply doing tipPos.down() doesnt account for fail.
        }

        while (totalUpperDripGrowth+totalLowerDripGrowth > 0) {
            if (totalUpperDripGrowth == 0) {

                if (pos.getY()-tipPos.getY() >= MAX_STALACTITE_GROWTH)
                    return;

                --totalLowerDripGrowth;
                tryGrowStalagmite(world, tipPos);
            } else if (totalLowerDripGrowth == 0) {
                --totalUpperDripGrowth;
                tryGrow(world, tipPos, Direction.DOWN);
                tipPos = getTipPos(world.getBlockState(pos), world, pos, 12, false);
                if (tipPos == null) return; //recalculate tip so if tryGrow fails, we wont grow past any blocking blocks. Simply doing tipPos.down() doesnt account for fail.

            } else if (random.nextBoolean()) {
                --totalLowerDripGrowth;
                tryGrowStalagmite(world, tipPos);
            } else {
                --totalUpperDripGrowth;
                tryGrow(world, tipPos, Direction.DOWN);
                tipPos = getTipPos(world.getBlockState(pos), world, pos, 12, false);
                if (tipPos == null) return; //recalculate tip so if tryGrow fails, we wont grow past any blocking blocks. Simply doing tipPos.down() doesnt account for fail.
            }
        }


    }

}
