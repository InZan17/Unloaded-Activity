package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.UnloadedActivity;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(KelpBlock.class)
public abstract class KelpMixin extends GrowingPlantHeadBlock implements LiquidBlockContainer {
    protected KelpMixin(Properties properties, Direction direction, VoxelShape voxelShape, boolean bl, double d) {
        super(properties, direction, voxelShape, bl, d);
    }

    @Override public boolean canSimulateRandTicks(BlockState state, ServerLevel level, BlockPos pos) {
        if (!UnloadedActivity.config.growKelp) return false;
        return super.canSimulateRandTicks(state, level, pos);
    }
}
