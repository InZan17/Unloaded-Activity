package com.github.inzan123.mixin.chunk.randomTicks;

import com.github.inzan123.UnloadedActivity;
import com.github.inzan123.Utils;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static java.lang.Math.max;

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
    @Shadow @Final
    private static int MAX_STALACTITE_GROWTH;
    @Shadow @Final
    private static int STALACTITE_FLOOR_SEARCH_RANGE;

    @Override
    public double getOdds(ServerWorld world, BlockPos pos) {
        return 0.01137777;
    }

    @Override
    public boolean implementsSimulateRandTicks() {return true;}
    @Override public boolean canSimulateRandTicks(BlockState state, ServerWorld world, BlockPos pos) {
        if (!UnloadedActivity.instance.config.growDripstone && !UnloadedActivity.instance.config.dripstoneFillCauldrons) return false;
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
                return i+2;
            }

            if (!canDripThrough(world, mutable, blockState)) {
                return -1;
            }
        }
        return -1;
    }

    @Override
    public void simulateRandTicks(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {


        BlockState dripstoneBlockState = world.getBlockState(pos.up(1));
        BlockState waterState = world.getBlockState(pos.up(2));
        if (canGrow(dripstoneBlockState, waterState)) {
            BlockPos tipPos = getTipPos(state, world, pos, MAX_STALACTITE_GROWTH, false);
            if (tipPos != null) {
                BlockState tip = world.getBlockState(tipPos);
                if (PointedDripstoneBlock.canDrip(tip) && canGrow(tip, world, tipPos)) {

                    double totalGrowOdds = this.getOdds(world,pos) * Utils.getRandomPickOdds(randomTickSpeed);

                    int stalagmiteGroundDistance = getStalagmiteGrowthDistance(world, tipPos);
                    int cauldronGroundDistance = getStalagmiteGrowthDistance(world, tipPos);
                    int totalDripGrowth = Utils.getOccurrences(timePassed, totalGrowOdds*0.5, MAX_STALACTITE_GROWTH, random);

                    if (stalagmiteGroundDistance != -1) {
                        int successesUntilReachGround = max(stalagmiteGroundDistance-STALACTITE_FLOOR_SEARCH_RANGE, 0);
                        if (totalDripGrowth >= successesUntilReachGround) {
                            long leftover = Utils.sampleNegativeBinomialWithMax(timePassed, successesUntilReachGround, totalGrowOdds*0.5, random);
                        }
                    }
                }
            }
        }
    }

}
