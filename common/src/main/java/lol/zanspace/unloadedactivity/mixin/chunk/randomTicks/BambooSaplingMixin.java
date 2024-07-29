package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.OccurrencesAndLeftover;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.floor;

#if MC_VER >= MC_1_20_4
@Mixin(BambooShootBlock.class)
#else
@Mixin(BambooSaplingBlock.class)
#endif
public abstract class BambooSaplingMixin extends Block {
    public BambooSaplingMixin(Settings settings) {
        super(settings);
    }

    @Shadow protected void grow(World world, BlockPos pos) {}

    @Override
    public double getOdds(ServerWorld world, BlockPos pos) {
        return 1d/3d;
    }
    @Override
    public boolean implementsSimulateRandTicks() {return true;}

    @Override public boolean canSimulateRandTicks(BlockState state, ServerWorld world, BlockPos pos) {
        if (!UnloadedActivity.config.growBamboo) return false;
        if (!world.isAir(pos.up())) return false;
        if (world.getBaseLightLevel(pos.up(), 0) < 9) return false;
        return true;
    }
    @Override public int getMaxHeightUA() {
        return 15;
    }

    @Override
    public void simulateRandTicks(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
        double totalOdds = getOdds(world, pos) * randomPickChance;

        OccurrencesAndLeftover growthInfo = Utils.getOccurrencesAndLeftoverTicks(timePassed, totalOdds, 1, random);

        if (growthInfo.occurrences == 0)
            return;

        this.grow(world, pos);
        BlockPos newPos = pos.up();
        BlockState newState = world.getBlockState(newPos);
        if (newState.getBlock() instanceof BambooBlock bamboo) {
            if (bamboo.canSimulateRandTicks(newState, world, newPos) && growthInfo.leftover > 0) {
                bamboo.simulateRandTicks(newState, world, newPos, random, growthInfo.leftover, randomTickSpeed);
            }

        }
    }
}
