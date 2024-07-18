package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import net.minecraft.block.BlockState;
import net.minecraft.block.PropaguleBlock;
import net.minecraft.block.SaplingBlock;
#if MC_VER >= MC_1_20_4
import net.minecraft.block.SaplingGenerator;
#else
import net.minecraft.block.sapling.SaplingGenerator;
#endif
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(PropaguleBlock.class)
public abstract class PropaguleSaplingMixin extends SaplingBlock {

    protected PropaguleSaplingMixin(SaplingGenerator generator, Settings settings) {
        super(generator, settings);
    }

    @Shadow @Final public static BooleanProperty HANGING;

    @Shadow
    protected static boolean isHanging(BlockState state) {
        return state.get(HANGING);
    }

    @Override
    public boolean canSimulateRandTicks(BlockState state, ServerWorld world, BlockPos pos) {
        if (isHanging(state))
            return false;

        return super.canSimulateRandTicks(state, world, pos);
    }
}
