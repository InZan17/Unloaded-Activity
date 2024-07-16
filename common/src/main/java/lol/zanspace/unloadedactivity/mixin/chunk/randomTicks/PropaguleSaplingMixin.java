package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import net.minecraft.block.BlockState;
import net.minecraft.block.PropaguleBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(PropaguleBlock.class)
public abstract class PropaguleSaplingMixin extends SaplingMixin {
    protected PropaguleSaplingMixin(Settings settings) {
        super(settings);
    }

    @Shadow @Final public static BooleanProperty HANGING;

    @Shadow
    protected static boolean isHanging(BlockState state) {
        return state.get(HANGING);
    }

    @Override
    public void simulateRandTicks(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {
        if (isHanging(state))
            return;

        super.simulateRandTicks(state, world, pos, random, timePassed, randomTickSpeed);
    }
}
