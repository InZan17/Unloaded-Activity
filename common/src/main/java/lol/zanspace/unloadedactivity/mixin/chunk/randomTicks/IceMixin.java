package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;


import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(IceBlock.class)
public abstract class IceMixin extends #if MC_VER >= MC_1_20_4 TranslucentBlock #else TransparentBlock #endif {

    public IceMixin(Settings settings) {
        super(settings);
    }

    @Override
    public double getOdds(ServerWorld world, BlockPos pos) {
        return 1;
    }

    @Shadow protected void melt(BlockState state, World world, BlockPos pos) {}
    @Override
    public boolean implementsSimulateRandTicks() {return true;}
    @Override public boolean canSimulateRandTicks(BlockState state, ServerWorld world, BlockPos pos) {
        if (!UnloadedActivity.config.meltIce) return false;
        #if MC_VER >= MC_1_21_3
        int opacity = state.getOpacity();
        #else
        int opacity = state.getOpacity(world, pos);
        #endif
        if (world.getLightLevel(LightType.BLOCK, pos) <= 11 - opacity) return false;
        return true;
    }

    @Override
    public void simulateRandTicks(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        double pickOdds = Utils.getRandomPickOdds(randomTickSpeed)*this.getOdds(world,pos);

        if (Utils.getOccurrences(timePassed, pickOdds, 1, random) != 0) {
            this.melt(state, world, pos);
        }
    }
}
