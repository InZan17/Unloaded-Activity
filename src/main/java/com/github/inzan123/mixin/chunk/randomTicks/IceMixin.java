package com.github.inzan123.mixin.chunk.randomTicks;


import com.github.inzan123.UnloadedActivity;
import com.github.inzan123.Utils;
import com.github.inzan123.mixin.CropBlockInvoker;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(IceBlock.class)
public abstract class IceMixin extends TransparentBlock {

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
        if (!UnloadedActivity.instance.config.meltIce) return false;
        if (world.getLightLevel(LightType.BLOCK, pos) <= 11 - state.getOpacity(world, pos)) return false;
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
