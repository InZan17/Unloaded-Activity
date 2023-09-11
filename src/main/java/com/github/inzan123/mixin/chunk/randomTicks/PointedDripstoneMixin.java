package com.github.inzan123.mixin.chunk.randomTicks;

import com.github.inzan123.UnloadedActivity;
import com.github.inzan123.Utils;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

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

    @Shadow @Final
    private static int MAX_STALACTITE_GROWTH;

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
    @Override
    public void simulateRandTicks(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        int totalDripGrowth = 0;
        int growthUntilOther = 0;

        BlockState dripstoneBlockState = world.getBlockState(pos.up(1));
        BlockState waterState = world.getBlockState(pos.up(2));
        if (canGrow(dripstoneBlockState, waterState)) {
            BlockPos tipPos = getTipPos(state, world, pos, MAX_STALACTITE_GROWTH, false);
            if (tipPos != null) {
                BlockState tip = world.getBlockState(tipPos);
                if (PointedDripstoneBlock.canDrip(tip) && canGrow(tip, world, tipPos)) {
                    //do occurrences here
                }
            }
        }
    }

}
