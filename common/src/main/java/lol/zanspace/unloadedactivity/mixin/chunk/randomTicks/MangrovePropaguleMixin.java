package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.datapack.SimulateProperty;
import lol.zanspace.unloadedactivity.datapack.SimulationData;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.block.SaplingBlock;

#if MC_VER >= MC_1_20_4
import net.minecraft.world.level.block.grower.TreeGrower;
#else
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
#endif
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(MangrovePropaguleBlock.class)
public abstract class MangrovePropaguleMixin extends SaplingBlock {

    #if MC_VER >= MC_1_20_4
    protected MangrovePropaguleMixin(TreeGrower treeGrower, Properties properties) {
        super(treeGrower, properties);
    }
    #else
    protected MangrovePropaguleMixin(AbstractTreeGrower abstractTreeGrower, Properties properties) {
        super(abstractTreeGrower, properties);
    }
    #endif

    @Shadow @Final public static BooleanProperty HANGING;


    @Shadow
    private static boolean isHanging(BlockState state) {
        return state.getValue(HANGING);
    }

    @Override
    public boolean canSimulateRandTicks(BlockState state, ServerLevel level, BlockPos pos, SimulateProperty simulateProperty) {
        if (isHanging(state))
            return false;

        return super.canSimulateRandTicks(state, level, pos, simulateProperty);
    }
}
