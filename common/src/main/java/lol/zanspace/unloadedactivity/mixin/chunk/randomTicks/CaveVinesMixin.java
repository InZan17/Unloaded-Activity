package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.datapack.SimulateProperty;
import lol.zanspace.unloadedactivity.datapack.SimulationData;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CaveVinesBlock.class)
public abstract class CaveVinesMixin extends GrowingPlantHeadBlock implements BonemealableBlock, CaveVines {

    protected CaveVinesMixin(Properties properties, Direction direction, VoxelShape voxelShape, boolean bl, double d) {
        super(properties, direction, voxelShape, bl, d);
    }

    @Override public boolean canSimulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulateProperty simulateProperty, String propertyName) {
        if (!UnloadedActivity.config.growGlowBerries) return false;
        return super.canSimulateRandTicks(state, level, pos, simulateProperty, propertyName);
    }
}
